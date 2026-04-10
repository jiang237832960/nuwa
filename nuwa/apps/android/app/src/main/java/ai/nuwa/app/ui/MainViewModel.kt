package ai.nuwa.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ai.nuwa.app.data.model.*
import ai.nuwa.app.data.repository.ModelRepository
import ai.nuwa.app.bridge.NuwaBridge
import ai.nuwa.app.inference.InferenceResult
import ai.nuwa.app.inference.ModelManager
import ai.nuwa.app.inference.ModelManagerHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val modelRepository = ModelRepository(application)
    private val bridge = NuwaBridge(modelRepository)
    private val modelManager = ModelManagerHolder.getInstance(modelRepository)
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask.asStateFlow()
    
    private val _serviceStatus = MutableStateFlow("未连接")
    val serviceStatus: StateFlow<String> = _serviceStatus.asStateFlow()
    
    private val _worldState = MutableStateFlow(WorldState())
    val worldState: StateFlow<WorldState> = _worldState.asStateFlow()
    
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()
    
    private val _modelStatus = MutableStateFlow<String?>(null)
    val modelStatus: StateFlow<String?> = _modelStatus.asStateFlow()
    
    init {
        checkServiceStatus()
        checkModelStatus()
    }
    
    private fun checkModelStatus() {
        val hasModels = modelRepository.hasModels()
        val currentModel = modelRepository.getCurrentModel()
        
        when {
            !hasModels -> {
                _modelStatus.value = "请先导入AI模型"
                _isModelLoaded.value = false
            }
            currentModel != null && currentModel.path.isNotEmpty() -> {
                _modelStatus.value = "已加载: ${currentModel.name}"
                _isModelLoaded.value = modelManager.isModelLoaded()
            }
            else -> {
                _modelStatus.value = "请选择一个模型"
                _isModelLoaded.value = false
            }
        }
    }
    
    fun loadCurrentModel() {
        viewModelScope.launch {
            val currentModel = modelRepository.getCurrentModel()
            if (currentModel != null && currentModel.path.isNotEmpty()) {
                addMessage("正在加载模型...", false)
                val result = modelManager.loadModel(currentModel)
                result.fold(
                    onSuccess = {
                        _isModelLoaded.value = true
                        _modelStatus.value = "已加载: ${currentModel.name}"
                        addMessage("模型加载成功", false)
                    },
                    onFailure = { e ->
                        _isModelLoaded.value = false
                        _modelStatus.value = "模型加载失败"
                        addMessage("模型加载失败: ${e.message}", false)
                    }
                )
            } else {
                addMessage("没有可用的模型，请先导入AI模型", false)
            }
        }
    }
    
    fun checkServiceStatus() {
        viewModelScope.launch {
            val connected = bridge.isServiceConnected()
            _serviceStatus.value = if (connected) "已连接" else "未连接"
        }
    }
    
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        addMessage(text, true)
        
        viewModelScope.launch {
            try {
                if (!modelManager.isModelLoaded()) {
                    addMessage("请先导入并加载AI模型才能使用对话功能", false)
                    return@launch
                }
                
                addMessage("正在分析你的意图...", false)
                
                val intent = bridge.parseIntent(text)
                
                val intentDescription = describeIntent(intent)
                addMessage("已理解: $intentDescription", false)
                
                val task = bridge.planTask(intent)
                _currentTask.value = task
                
                addMessage("任务已规划，共 ${task.steps.size} 个步骤", false)
                
                executeTask(task)
                
            } catch (e: Exception) {
                addMessage("处理失败: ${e.message}", false, e.message)
            }
        }
    }
    
    private fun describeIntent(intent: Intent): String {
        return when (intent.intentType) {
            IntentType.SendMessage -> {
                val contact = intent.entities.find { it.entityType == EntityType.Person }?.value
                if (contact != null) "发送微信消息给 $contact" else "发送微信消息"
            }
            IntentType.Navigate -> {
                val location = intent.entities.find { it.entityType == EntityType.Location }?.value
                if (location != null) "导航到 $location" else "使用导航"
            }
            IntentType.OpenApp -> "打开应用"
            IntentType.OpenFile -> "打开文档"
            IntentType.QueryBill -> "查询账单"
            IntentType.SearchContact -> "搜索联系人"
            IntentType.SaveImage -> "保存图片"
            IntentType.Unknown -> "未知任务"
        }
    }
    
    private suspend fun executeTask(task: Task) {
        var currentTask = task.copy(status = TaskStatus.Running)
        _currentTask.value = currentTask
        
        var executionFailed = false
        
        for ((index, step) in currentTask.steps.withIndex()) {
            if (executionFailed) break
            
            addMessage("执行步骤 ${index + 1}/${currentTask.steps.size}: ${step.message}", false)
            
            currentTask = currentTask.copy(
                currentStep = index,
                steps = currentTask.steps.toMutableList().apply {
                    this[index] = step.copy(status = StepStatus.Running)
                }
            )
            _currentTask.value = currentTask
            
            val result = withContext(Dispatchers.Default) {
                try {
                    val r = bridge.executeStep(step)
                    currentTask = currentTask.copy(
                        steps = currentTask.steps.toMutableList().apply {
                            this[index] = step.copy(
                                status = if (r) StepStatus.Success else StepStatus.Failed,
                                message = if (r) "成功" else "失败"
                            )
                        }
                    )
                    _currentTask.value = currentTask
                    
                    if (r) {
                        addMessage("步骤 ${index + 1} 完成", false)
                    } else {
                        addMessage("步骤 ${index + 1} 失败", false, "执行失败")
                    }
                    r
                } catch (e: Exception) {
                    currentTask = currentTask.copy(
                        status = TaskStatus.Failed,
                        steps = currentTask.steps.toMutableList().apply {
                            this[index] = step.copy(
                                status = StepStatus.Failed,
                                message = e.message
                            )
                        }
                    )
                    _currentTask.value = currentTask
                    addMessage("步骤 ${index + 1} 出错: ${e.message}", false, e.message)
                    executionFailed = true
                    false
                }
            }
            
            if (!result) {
                executionFailed = true
            }
        }
        
        if (currentTask.steps.all { it.status == StepStatus.Success }) {
            currentTask = currentTask.copy(status = TaskStatus.Completed)
            _currentTask.value = currentTask
            addMessage("任务完成！", false)
        } else {
            currentTask = currentTask.copy(status = TaskStatus.Failed)
            _currentTask.value = currentTask
            val failedStep = currentTask.steps.indexOfFirst { it.status == StepStatus.Failed }
            addMessage("任务失败，步骤 ${failedStep + 1} 未完成", false, "部分步骤失败")
        }
    }
    
    fun cancelTask() {
        _currentTask.value?.let { task ->
            _currentTask.value = task.copy(status = TaskStatus.Failed)
            _messages.value = _messages.value + ChatMessage(
                content = "任务已取消",
                isUser = false
            )
        }
    }
    
    fun refreshWorldState() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val state = bridge.getWorldState()
                    _worldState.value = state
                    _serviceStatus.value = if (bridge.isServiceConnected()) "已连接" else "未连接"
                } catch (e: Exception) {
                    _serviceStatus.value = "获取状态失败: ${e.message}"
                }
            }
        }
    }
    
    private fun addMessage(content: String, isUser: Boolean, error: String? = null) {
        val message = ChatMessage(
            id = "msg_${System.currentTimeMillis()}_${(Math.random() * 1000).toInt()}",
            content = content,
            isUser = isUser,
            timestamp = System.currentTimeMillis(),
            error = error
        )
        _messages.value = _messages.value + message
    }
}
