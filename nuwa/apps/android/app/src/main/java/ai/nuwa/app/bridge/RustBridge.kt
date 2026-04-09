package ai.nuwa.app.bridge

class RustBridge {
    
    external fun initialize(): Boolean
    external fun loadModel(path: String): Long
    external fun generate(modelId: Long, prompt: String): String?
    external fun getResources(): String
    external fun getForegroundApp(): String
    
    companion object {
        init {
            System.loadLibrary("nuwa_bridge")
        }
    }
}
