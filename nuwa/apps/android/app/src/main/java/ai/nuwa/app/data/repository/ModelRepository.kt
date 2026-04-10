package ai.nuwa.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.OpenableColumns
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
                ?: return@withContext Result.failure(Exception("无法打开文件，请选择有效的模型文件"))
            
            val fileName = getFileName(uri) ?: "model_${System.currentTimeMillis()}.gguf"
            
            val lowerName = fileName.lowercase()
            if (!lowerName.endsWith(".gguf") && !lowerName.endsWith(".bin")) {
                inputStream.close()
                return@withContext Result.failure(Exception("请选择 GGUF 或 BIN 格式的模型文件"))
            }
            
            val outputFile = File(getModelsDir(), fileName)
            
            if (outputFile.exists()) {
                outputFile.delete()
            }
            
            inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
            
            val modelInfo = parseModelInfo(outputFile, fileName)
            saveModel(modelInfo)
            
            Result.success(modelInfo)
        } catch (e: SecurityException) {
            Result.failure(Exception("文件访问权限不足，请重试"))
        } catch (e: OutOfMemoryError) {
            Result.failure(Exception("模型文件过大，内存不足"))
        } catch (e: Exception) {
            Result.failure(Exception("导入失败: ${e.message}"))
        }
    }
    
    private fun getFileName(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        cursor.getString(nameIndex)
                    } else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseModelInfo(file: File, originalName: String): ModelInfo {
        val size = file.length()
        val quantization = detectQuantization(originalName)
        val name = originalName
            .removeSuffix(".gguf")
            .removeSuffix(".bin")
            .replace("_", " ")
            .replace("-", " ")
        
        return ModelInfo(
            id = "model_${System.currentTimeMillis()}",
            name = name.ifBlank { "Imported Model" },
            path = file.absolutePath,
            size = size,
            quantization = quantization
        )
    }
    
    private fun detectQuantization(fileName: String): String {
        val lower = fileName.lowercase()
        return when {
            lower.contains("q8_0") || lower.contains("q8") -> "Q8_0"
            lower.contains("q6_k") || lower.contains("q6") -> "Q6_K"
            lower.contains("q5_k_m") || lower.contains("q5") -> "Q5_K_M"
            lower.contains("q4_k_m") || lower.contains("q4") -> "Q4_K_M"
            lower.contains("q3_k_m") || lower.contains("q3") -> "Q3_K_M"
            lower.contains("q2_k") || lower.contains("q2") -> "Q2_K"
            lower.contains("q4_0") -> "Q4_0"
            lower.contains("q5_0") -> "Q5_0"
            lower.contains("q6_0") -> "Q6_0"
            else -> "Q4_K_M"
        }
    }
    
    private fun saveModel(model: ModelInfo) {
        val models = getImportedModels().toMutableList()
        models.add(model)
        saveModels(models)
    }
    
    fun getImportedModels(): List<ModelInfo> {
        val json = prefs.getString(KEY_MODELS, null) ?: return emptyList()
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
            emptyList()
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
        val currentId = prefs.getString(KEY_CURRENT_MODEL, null) ?: return null
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
    
    fun hasModels(): Boolean = getImportedModels().isNotEmpty()
}
