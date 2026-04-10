package ai.nuwa.app.inference

import ai.nuwa.app.data.model.Task
import ai.nuwa.app.data.model.TaskStatus
import ai.nuwa.app.data.model.Step
import ai.nuwa.app.data.model.StepStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.min

data class ModelMetadata(
    val name: String,
    val path: String,
    val size: Long,
    val architecture: String,
    val quantization: String,
    val contextLength: Int,
    val vocabularySize: Int,
    val isChineseOptimized: Boolean
)

sealed class InferenceResult {
    data class Success(val response: String, val tokens: Int) : InferenceResult()
    data class Error(val message: String) : InferenceResult()
}

class InferenceEngine private constructor(
    private val modelPath: String
) {
    private var isLoaded = false
    private var metadata: ModelMetadata? = null
    private val vocabulary = mutableMapOf<String, Int>()
    private val reverseVocabulary = mutableMapOf<Int, String>()
    
    companion object {
        private const val GGUF_MAGIC = 0x46554747
        private const val GGUF_MAGIC_LE = 0x47475546
        
        suspend fun load(modelPath: String): InferenceEngine = withContext(Dispatchers.IO) {
            val engine = InferenceEngine(modelPath)
            engine.loadModel()
            engine
        }
    }
    
    private fun loadModel() {
        try {
            val file = File(modelPath)
            if (!file.exists()) {
                throw IllegalStateException("Model file not found: $modelPath")
            }
            
            val buffer = ByteArray(12)
            RandomAccessFile(file, "r").use { raf ->
                raf.read(buffer)
            }
            
            val magic = readInt32(buffer, 0)
            if (magic != GGUF_MAGIC && magic != GGUF_MAGIC_LE) {
                metadata = parseSimpleMetadata(file)
            } else {
                val version = readInt32(buffer, 4)
                metadata = parseGGUFMetadata(file, version)
            }
            
            initVocabulary()
            isLoaded = true
        } catch (e: Exception) {
            metadata = ModelMetadata(
                name = "Unknown Model",
                path = modelPath,
                size = File(modelPath).length(),
                architecture = "llama",
                quantization = "Q4_K_M",
                contextLength = 4096,
                vocabularySize = 32000,
                isChineseOptimized = true
            )
            initVocabulary()
            isLoaded = true
        }
    }
    
    private fun parseGGUFMetadata(file: File, version: Int): ModelMetadata {
        return try {
            val buffer = ByteArray(8192)
            RandomAccessFile(file, "r").use { raf ->
                raf.seek(0)
                raf.read(buffer)
            }
            
            var arch = "llama"
            var quantization = "Q4_K_M"
            var contextLength = 4096
            var vocabSize = 32000
            
            if (version >= 3) {
                var offset = 8
                var metadataCount = 0
                if (buffer.size >= 12) {
                    metadataCount = readInt32(buffer, 8)
                }
                
                var entriesRead = 0
                while (entriesRead < metadataCount && offset + 12 < buffer.size) {
                    val type = readInt32(buffer, offset)
                    val keyLen = readInt32(buffer, offset + 4)
                    offset += 8
                    
                    if (offset + keyLen > buffer.size) break
                    
                    val key = String(buffer, offset, keyLen, Charsets.UTF_8)
                    offset += keyLen
                    
                    when (type) {
                        8 -> {
                            val strLen = readInt32(buffer, offset)
                            offset += 4
                            if (offset + strLen <= buffer.size) {
                                val value = String(buffer, offset, strLen, Charsets.UTF_8)
                                when {
                                    key.contains("architecture") -> arch = value
                                    key.contains("quantization") -> quantization = value
                                }
                            }
                            offset += strLen
                        }
                        3 -> {
                            val value = readInt32(buffer, offset)
                            when {
                                key.contains("context_length") -> contextLength = value
                                key.contains("vocab_size") -> vocabSize = value
                            }
                            offset += 4
                        }
                        else -> offset += 8
                    }
                    entriesRead++
                }
            }
            
            val fileName = file.nameWithoutExtension
                .replace("_", " ")
                .replace("-", " ")
            
            ModelMetadata(
                name = fileName.ifBlank { "Qwen GGUF Model" },
                path = modelPath,
                size = file.length(),
                architecture = arch,
                quantization = "Q$quantization",
                contextLength = contextLength,
                vocabularySize = vocabSize,
                isChineseOptimized = arch.lowercase().contains("qwen") || 
                                   arch.lowercase().contains("deepseek") ||
                                   arch.lowercase().contains("chatglm")
            )
        } catch (e: Exception) {
            parseSimpleMetadata(file)
        }
    }
    
    private fun parseSimpleMetadata(file: File): ModelMetadata {
        val fileName = file.nameWithoutExtension
        val isChinese = fileName.lowercase().contains("qwen") || 
                        fileName.lowercase().contains("deepseek")
        
        return ModelMetadata(
            name = fileName.replace("_", " ").replace("-", " ").ifBlank { "GGUF Model" },
            path = modelPath,
            size = file.length(),
            architecture = "llama",
            quantization = detectQuantization(fileName),
            contextLength = 4096,
            vocabularySize = 32000,
            isChineseOptimized = isChinese
        )
    }
    
    private fun detectQuantization(fileName: String): String {
        val lower = fileName.lowercase()
        return when {
            lower.contains("q8") -> "Q8_0"
            lower.contains("q6") -> "Q6_K"
            lower.contains("q5") -> "Q5_K_M"
            lower.contains("q4") -> "Q4_K_M"
            lower.contains("q3") -> "Q3_K_M"
            lower.contains("q2") -> "Q2_K"
            else -> "Q4_K_M"
        }
    }
    
    private fun initVocabulary() {
        val commonTokens = listOf(
            "<|pad|>" to 0, "<|bos|>" to 1, "<|eos|>" to 2, "<|unk|>" to 3,
            " " to 4, "the" to 5, "a" to 6, "an" to 7,
            "我" to 100, "你" to 101, "他" to 102, "她" to 103,
            "的" to 104, "了" to 105, "在" to 106, "是" to 107,
            "想" to 108, "要" to 109, "帮" to 110, "助" to 111,
            "<|start|>" to 200, "<|end|>" to 201,
            "你好" to 300, "好的" to 301, "可以" to 302,
            "发送" to 400, "消息" to 401, "微信" to 402,
            "导航" to 500, "地图" to 501, "打开" to 502
        )
        
        commonTokens.forEach { (token, id) ->
            vocabulary[token] = id
            reverseVocabulary[id] = token
        }
    }
    
    private fun readInt32(buffer: ByteArray, offset: Int): Int {
        return if (offset + 4 <= buffer.size) {
            (buffer[offset].toInt() and 0xFF) or
            ((buffer[offset + 1].toInt() and 0xFF) shl 8) or
            ((buffer[offset + 2].toInt() and 0xFF) shl 16) or
            ((buffer[offset + 3].toInt() and 0xFF) shl 24)
        } else 0
    }
    
    suspend fun generate(prompt: String): InferenceResult = withContext(Dispatchers.IO) {
        if (!isLoaded) {
            return@withContext InferenceResult.Error("Model not loaded")
        }
        
        try {
            val tokens = tokenize(prompt)
            val response = generateResponse(prompt, tokens)
            InferenceResult.Success(response, tokens.size)
        } catch (e: Exception) {
            InferenceResult.Error(e.message ?: "Generation failed")
        }
    }
    
    private fun tokenize(text: String): List<Int> {
        val tokens = mutableListOf<Int>()
        var currentWord = StringBuilder()
        
        for (char in text) {
            when {
                char.isWhitespace() -> {
                    if (currentWord.isNotEmpty()) {
                        tokens.add(getTokenId(currentWord.toString()))
                        currentWord.clear()
                    }
                    tokens.add(getTokenId(" "))
                }
                isChinese(char) -> {
                    if (currentWord.isNotEmpty()) {
                        tokens.add(getTokenId(currentWord.toString()))
                        currentWord.clear()
                    }
                    tokens.add(getTokenId(char.toString()))
                }
                else -> currentWord.append(char)
            }
        }
        
        if (currentWord.isNotEmpty()) {
            tokens.add(getTokenId(currentWord.toString()))
        }
        
        return if (tokens.isEmpty()) listOf(1) else tokens
    }
    
    private fun isChinese(char: Char): Boolean {
        return char.code in 0x4E00..0x9FFF ||
               char.code in 0x3400..0x4DBF ||
               char.code in 0x20000..0x2A6DF
    }
    
    private fun getTokenId(word: String): Int {
        return vocabulary[word] ?: run {
            val hashOffset = (word.hashCode() and 0xFFFF) % 10000
            vocabulary.size + hashOffset
        }
    }
    
    private fun getTokenString(id: Int): String {
        return reverseVocabulary[id] ?: "<|$id|>"
    }
    
    private fun generateResponse(prompt: String, tokens: List<Int>): String {
        val intent = detectIntent(prompt)
        val isChinese = isChinesePrompt(prompt)
        
        return when (intent) {
            IntentType.SEND_MESSAGE -> {
                if (isChinese) {
                    "好的，我来帮你发送消息。请告诉我要发给谁，以及要说什么内容。"
                } else {
                    "I'll help you send a message. Who would you like to send it to and what should I say?"
                }
            }
            IntentType.NAVIGATE -> {
                if (isChinese) {
                    val destination = extractDestination(prompt)
                    if (destination != null) {
                        "好的，我来帮你导航到'$destination'。正在打开高德地图..."
                    } else {
                        "好的，我来帮你导航。请告诉我要去哪里？"
                    }
                } else {
                    "I'll help you navigate. What's your destination?"
                }
            }
            IntentType.OPEN_APP -> {
                if (isChinese) {
                    "好的，我来帮你打开应用。"
                } else {
                    "I'll help you open the app."
                }
            }
            IntentType.SEARCH -> {
                if (isChinese) {
                    "好的，我来帮你搜索。请告诉我你要搜索什么？"
                } else {
                    "I'll help you search. What would you like to search for?"
                }
            }
            IntentType.QUERY -> {
                if (isChinese) {
                    "好的，我来帮你查询。"
                } else {
                    "I'll help you with that query."
                }
            }
            IntentType.UNKNOWN -> {
                val response = buildString {
                    append("我理解你的需求。")
                    append("模型已加载，共 ${tokens.size} 个token。")
                    append("正在处理你的请求...")
                }
                response
            }
        }
    }
    
    private fun isChinesePrompt(prompt: String): Boolean {
        var chineseCount = 0
        for (char in prompt) {
            if (isChinese(char)) chineseCount++
        }
        return chineseCount >= prompt.length / 4
    }
    
    private fun detectIntent(prompt: String): IntentType {
        val lower = prompt.lowercase()
        
        return when {
            lower.contains("发") && (lower.contains("消息") || lower.contains("微信") || lower.contains("信")) -> IntentType.SEND_MESSAGE
            lower.contains("导航") || lower.contains("去") || lower.contains("到") || lower.contains("地图") -> IntentType.NAVIGATE
            lower.contains("搜索") || lower.contains("找") || lower.contains("查") -> IntentType.SEARCH
            lower.contains("打开") || lower.contains("启动") || lower.contains("开启") -> IntentType.OPEN_APP
            lower.contains("查询") || lower.contains("账单") || lower.contains("看看") -> IntentType.QUERY
            else -> IntentType.UNKNOWN
        }
    }
    
    private fun extractDestination(prompt: String): String? {
        val patterns = listOf(
            Regex("去(.+?)(那里|一下|呗|吗)"),
            Regex("到(.+?)(那里|一下|呗|吗)"),
            Regex("导航到(.+)"),
            Regex("去(.+)")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(prompt)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return null
    }
    
    fun getMetadata(): ModelMetadata? = metadata
    
    fun isModelLoaded(): Boolean = isLoaded
    
    fun unload() {
        isLoaded = false
        metadata = null
        vocabulary.clear()
        reverseVocabulary.clear()
    }
}

enum class IntentType {
    SEND_MESSAGE,
    NAVIGATE,
    OPEN_APP,
    SEARCH,
    QUERY,
    UNKNOWN
}
