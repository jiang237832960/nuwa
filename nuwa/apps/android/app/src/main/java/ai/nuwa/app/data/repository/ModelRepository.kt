package ai.nuwa.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class ModelInfo(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val quantization: String,
    val isLoaded: Boolean = false
)

class ModelRepository(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "nuwa_model_prefs"
        private const val KEY_MODELS = "imported_models"
        private const val KEY_CURRENT_MODEL = "current_model_id"
        private const val MODEL_DIR = "models"
    }
    
    fun getModelsDir(): File {
        val dir = File(context.filesDir, MODEL_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    suspend fun importModel(uri: Uri): Result<ModelInfo> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("无法打开文件"))
            
            val fileName = getFileName(uri) ?: "model_${System.currentTimeMillis()}.gguf"
            val outputFile = File(getModelsDir(), fileName)
            
            FileOutputStream(outputFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            
            val modelInfo = parseModelInfo(outputFile, fileName)
            saveModel(modelInfo)
            
            Result.success(modelInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            if (nameIndex >= 0) it.getString(nameIndex) else null
        }
    }
    
    private fun parseModelInfo(file: File, originalName: String): ModelInfo {
        return try {
            val size = file.length()
            val quantization = detectQuantization(file)
            val name = originalName.removeSuffix(".gguf")
                .replace("_", " ")
                .replace("-", " ")
            
            ModelInfo(
                id = "model_${System.currentTimeMillis()}",
                name = name.ifBlank { "Qwen GGUF Model" },
                path = file.absolutePath,
                size = size,
                quantization = quantization
            )
        } catch (e: Exception) {
            ModelInfo(
                id = "model_${System.currentTimeMillis()}",
                name = originalName.removeSuffix(".gguf"),
                path = file.absolutePath,
                size = file.length(),
                quantization = "Unknown"
            )
        }
    }
    
    private fun detectQuantization(file: File): String {
        return try {
            val buffer = ByteArray(32)
            file.inputStream().use { stream ->
                stream.read(buffer, 0, 32)
            }
            
            val magic = String(buffer, 0, 4)
            if (magic == "GGUF") {
                val version = buffer[4].toInt() and 0xFF
                "GGUF v$version"
            } else {
                "Q4_K_M"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun saveModel(model: ModelInfo) {
        val models = getImportedModels().toMutableList()
        models.add(model)
        saveModels(models)
    }
    
    fun getImportedModels(): List<ModelInfo> {
        val json = prefs.getString(KEY_MODELS, null) ?: return getDefaultModels()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                ModelInfo(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    path = obj.getString("path"),
                    size = obj.getLong("size"),
                    quantization = obj.getString("quantization"),
                    isLoaded = obj.optBoolean("isLoaded", false)
                )
            }
        } catch (e: Exception) {
            getDefaultModels()
        }
    }
    
    private fun saveModels(models: List<ModelInfo>) {
        val array = JSONArray()
        models.forEach { model ->
            val obj = JSONObject().apply {
                put("id", model.id)
                put("name", model.name)
                put("path", model.path)
                put("size", model.size)
                put("quantization", model.quantization)
                put("isLoaded", model.isLoaded)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_MODELS, array.toString()).apply()
    }
    
    fun getCurrentModel(): ModelInfo? {
        val currentId = prefs.getString(KEY_CURRENT_MODEL, null) ?: return getDefaultModels().firstOrNull()
        return getImportedModels().find { it.id == currentId }
    }
    
    fun setCurrentModel(modelId: String) {
        prefs.edit().putString(KEY_CURRENT_MODEL, modelId).apply()
        
        val models = getImportedModels().map {
            it.copy(isLoaded = it.id == modelId)
        }
        saveModels(models)
    }
    
    fun deleteModel(modelId: String): Boolean {
        val models = getImportedModels().toMutableList()
        val model = models.find { it.id == modelId } ?: return false
        
        val file = File(model.path)
        if (file.exists()) {
            file.delete()
        }
        
        models.removeAll { it.id == modelId }
        saveModels(models)
        
        if (prefs.getString(KEY_CURRENT_MODEL, null) == modelId) {
            prefs.edit().putString(KEY_CURRENT_MODEL, models.firstOrNull()?.id).apply()
        }
        
        return true
    }
    
    private fun getDefaultModels(): List<ModelInfo> {
        return listOf(
            ModelInfo(
                id = "default_qwen",
                name = "Qwen 2B GGUF",
                path = "",
                size = 1_610_612_736,
                quantization = "Q4_K_M",
                isLoaded = true
            )
        )
    }
}
