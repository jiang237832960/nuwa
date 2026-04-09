package ai.nuwa.app.data.model

enum class NegotiationScenario {
    ResourceInsufficient,
    PermissionRequired,
    SkillLearning,
    TimeDelay,
    RiskWarning
}

data class NegotiationRequest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val scenario: NegotiationScenario,
    val title: String,
    val content: String,
    val suggestion: String,
    val actions: List<NegotiationAction>,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: Int = 0
)

data class NegotiationAction(
    val label: String,
    val type: NegotiationActionType,
    val value: String? = null
)

enum class NegotiationActionType {
    Accept,
    Delay,
    Reject,
    Custom
}

data class NegotiationResponse(
    val requestId: String,
    val action: NegotiationAction,
    val timestamp: Long = System.currentTimeMillis()
)

object NegotiationExamples {
    fun getResourceInsufficientExample(): NegotiationRequest {
        return NegotiationRequest(
            scenario = NegotiationScenario.ResourceInsufficient,
            title = "我想快一些",
            content = "我现在也能继续帮你做，但会慢一些，复杂任务也更容易出错。",
            suggestion = "如果你愿意让我在充电的时候整理一下最近的经验，下次做这类事会更快更稳。",
            actions = listOf(
                NegotiationAction("今晚处理", NegotiationActionType.Accept, "tonight"),
                NegotiationAction("暂时不需要", NegotiationActionType.Reject)
            )
        )
    }

    fun getPermissionRequiredExample(): NegotiationRequest {
        return NegotiationRequest(
            scenario = NegotiationScenario.PermissionRequired,
            title = "需要一点权限",
            content = "要完成这个任务，我需要读取屏幕上内容的权限。",
            suggestion = "这个权限让我知道当前屏幕上有什么，更好地帮你操作。",
            actions = listOf(
                NegotiationAction("去设置", NegotiationActionType.Accept, "settings"),
                NegotiationAction("下次再说", NegotiationActionType.Reject)
            )
        )
    }

    fun getSkillLearningExample(): NegotiationRequest {
        return NegotiationRequest(
            scenario = NegotiationScenario.SkillLearning,
            title = "我想学一个新技能",
            content = "我发现你经常需要我帮你发微信消息，如果让我专门学习一下这个流程，下次执行会更准确。",
            suggestion = "学习大概需要10分钟，期间我会在后台整理经验，不影响你正常使用手机。",
            actions = listOf(
                NegotiationAction("好的，开始学习", NegotiationActionType.Accept),
                NegotiationAction("现在不方便", NegotiationActionType.Delay, "later")
            )
        )
    }

    fun getTimeDelayExample(): NegotiationRequest {
        return NegotiationRequest(
            scenario = NegotiationScenario.TimeDelay,
            title = "这个任务需要一些时间",
            content = "这个任务比较复杂，我需要多操作几步才能完成。",
            suggestion = "大概需要2-3分钟，我会一步步来，确保每一步都做对。",
            actions = listOf(
                NegotiationAction("好的，慢慢来", NegotiationActionType.Accept),
                NegotiationAction("简化流程", NegotiationActionType.Custom, "simplify")
            )
        )
    }
}
