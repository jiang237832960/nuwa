package ai.nuwa.app.data.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val task: Task? = null,
    val error: String? = null
)

data class WorldState(
    val foregroundApp: String = "",
    val uiTree: UiTreeNode? = null,
    val deviceResources: DeviceResources = DeviceResources(),
    val taskState: TaskStateInfo? = null
)

data class UiTreeNode(
    val id: String?,
    val className: String?,
    val text: String?,
    val contentDesc: String?,
    val bounds: Bounds?,
    val clickable: Boolean = false,
    val focusable: Boolean = false,
    val enabled: Boolean = true,
    val scrollable: Boolean = false,
    val children: List<UiTreeNode> = emptyList()
)

data class DeviceResources(
    val cpuUsage: Float = 0f,
    val memoryUsed: Long = 0,
    val memoryTotal: Long = 0,
    val batteryLevel: Float = 1f,
    val isCharging: Boolean = false,
    val screenOn: Boolean = true
)

data class TaskStateInfo(
    val taskId: String?,
    val status: TaskStatus = TaskStatus.Idle,
    val currentStep: Int = 0,
    val totalSteps: Int = 0
)

data class AuditRecord(
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: EventType,
    val details: String
)

enum class EventType {
    TaskStart,
    TaskComplete,
    TaskFail,
    ActionExecute,
    ActionResult,
    ResourceUpdate,
    Error
}
