package ai.nuwa.app.inference

import ai.nuwa.app.data.repository.ModelInfo
import ai.nuwa.app.data.repository.ModelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ModelManager(
    private val modelRepository: ModelRepository
) {
    private var currentEngine: InferenceEngine? = null
    private var currentModelInfo: ModelInfo? = null
    
    suspend fun loadModel(modelInfo: ModelInfo): Result<InferenceEngine> = withContext(Dispatchers.IO) {
        try {
            if (modelInfo.path.isEmpty()) {
                return@withContext Result.failure(Exception("模型文件不存在"))
            }
            
            val file = File(modelInfo.path)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("模型文件未找到: ${modelInfo.path}"))
            }
            
            unloadCurrentModel()
            
            currentEngine = InferenceEngine.load(modelInfo.path)
            currentModelInfo = modelInfo
            
            Result.success(currentEngine!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun generate(prompt: String): InferenceResult = withContext(Dispatchers.IO) {
        val engine = currentEngine
        if (engine == null) {
            return@withContext InferenceResult.Error("没有加载的模型")
        }
        
        engine.generate(prompt)
    }
    
    fun unloadCurrentModel() {
        currentEngine?.unload()
        currentEngine = null
        currentModelInfo = null
    }
    
    fun getCurrentModel(): ModelInfo? = currentModelInfo
    
    fun getCurrentEngine(): InferenceEngine? = currentEngine
    
    fun isModelLoaded(): Boolean = currentEngine?.isModelLoaded() == true
    
    fun getMetadata(): ModelMetadata? = currentEngine?.getMetadata()
}

object ModelManagerHolder {
    private var modelManager: ModelManager? = null
    
    fun getInstance(repository: ModelRepository): ModelManager {
        if (modelManager == null) {
            modelManager = ModelManager(repository)
        }
        return modelManager!!
    }
}
