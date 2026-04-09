package ai.nuwa.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.nuwa.app.data.model.*
import ai.nuwa.app.bridge.NuwaBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    
    private val bridge = NuwaBridge()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask.asStateFlow()
    
    private val _serviceStatus = MutableStateFlow("未连接")
    val serviceStatus: StateFlow<String> = _serviceStatus.asStateFlow()
    
    private val _worldState = MutableStateFlow(WorldState())
    val worldState: StateFlow<WorldState> = _worldState.asStateFlow()
    
    init {
        checkServiceStatus()
    }
    
    fun checkServiceStatus() {
        viewModelScope.launch {
            _serviceStatus.value = if (bridge.isServiceConnected()) "已连接" else "未连接"
        }
    }
    
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            val userMessage = ChatMessage(content = text, isUser = true)
            _messages.value = _messages.value + userMessage
            
            _messages.value = _messages.value + ChatMessage(
                content = "正在理解您的意图...",
                isUser = false
            )
            
            withContext(Dispatchers.Default) {
                try {
                    val intent = bridge.parseIntent(text)
                    
                    _messages.value = _messages.value.dropLast(1) + ChatMessage(
                        content = "已识别意图: ${intent.intentType.name}",
                        isUser = false
                    )
                    
                    val task = bridge.planTask(intent)
                    _currentTask.value = task
                    
                    _messages.value = _messages.value + ChatMessage(
                        content = "任务已创建，共 ${task.steps.size} 个步骤",
                        isUser = false
                    )
                    
                    executeTask(task)
                    
                } catch (e: Exception) {
                    _messages.value = _messages.value.dropLast(1) + ChatMessage(
                        content = "处理失败: ${e.message}",
                        isUser = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun executeTask(task: Task) {
        viewModelScope.launch {
            var currentTask = task.copy(status = TaskStatus.Running)
            _currentTask.value = currentTask
            
            var executionFailed = false
            for ((index, step) in currentTask.steps.withIndex()) {
                if (executionFailed) break
                
                _messages.value = _messages.value + ChatMessage(
                    content = "执行步骤 ${index + 1}: ${step.action.name}",
                    isUser = false
                )
                
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

                        _messages.value = _messages.value + ChatMessage(
                            content = "步骤 ${index + 1} 完成: ${if (r) "成功" else "失败"}",
                            isUser = false
                        )
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

                        _messages.value = _messages.value + ChatMessage(
                            content = "步骤 ${index + 1} 失败: ${e.message}",
                            isUser = false,
                            error = e.message
                        )
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
                _messages.value = _messages.value + ChatMessage(
                    content = "任务完成！",
                    isUser = false
                )
            } else {
                currentTask = currentTask.copy(status = TaskStatus.Failed)
                _messages.value = _messages.value + ChatMessage(
                    content = "任务失败",
                    isUser = false,
                    error = "部分步骤未完成"
                )
            }
            
            _currentTask.value = currentTask
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
                } catch (e: Exception) {
                    _serviceStatus.value = "获取状态失败: ${e.message}"
                }
            }
        }
    }
}
