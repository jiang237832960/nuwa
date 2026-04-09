package ai.nuwa.app.data.model

import java.io.Serializable

data class Intent(
    val rawText: String,
    val intentType: IntentType,
    val targetApp: String? = null,
    val entities: List<Entity> = emptyList(),
    val confidence: Float = 0f
) : Serializable

enum class IntentType {
    SendMessage,
    SearchContact,
    Navigate,
    OpenFile,
    SaveImage,
    QueryBill,
    OpenApp,
    Unknown
}

data class Entity(
    val name: String,
    val value: String,
    val entityType: EntityType
) : Serializable

enum class EntityType {
    Person,
    Location,
    App,
    Content,
    Time,
    Number
}

data class Task(
    val id: String,
    val intent: Intent,
    val steps: List<Step>,
    val status: TaskStatus = TaskStatus.Pending,
    val currentStep: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

data class Step(
    val id: Int,
    val action: Action,
    val target: Target? = null,
    val fallback: Step? = null,
    val status: StepStatus = StepStatus.Pending,
    val message: String? = null
) : Serializable

enum class Action {
    OpenApp,
    Click,
    Input,
    Swipe,
    Wait,
    Scroll,
    Navigate,
    CloseApp
}

data class Target(
    val selector: Selector,
    val text: String? = null,
    val bounds: Bounds? = null
) : Serializable

data class Selector(
    val by: SelectorType,
    val value: String
) : Serializable

enum class SelectorType {
    Text,
    ContentDesc,
    ResourceId,
    ClassName,
    PackageName
}

data class Bounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) : Serializable {
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
}

enum class StepStatus {
    Pending,
    Running,
    Success,
    Failed,
    Skipped
}

enum class TaskStatus {
    Idle,
    Running,
    Paused,
    Completed,
    Failed
}
