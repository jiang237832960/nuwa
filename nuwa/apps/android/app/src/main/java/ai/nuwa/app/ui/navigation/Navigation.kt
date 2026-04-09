package ai.nuwa.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Tasks : Screen("tasks")
    object Skills : Screen("skills")
    object Profile : Screen("profile")
    object WorldState : Screen("world_state")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object SkillDetail : Screen("skill_detail/{skillId}") {
        fun createRoute(skillId: String) = "skill_detail/$skillId"
    }
    object Settings : Screen("settings")
    object ModelManager : Screen("model_manager")
    object Growth : Screen("growth")
    object NegotiationDemo : Screen("negotiation_demo")
}

enum class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: String
) {
    HOME(Screen.Home, "首页", "home"),
    TASKS(Screen.Tasks, "任务", "tasks"),
    SKILLS(Screen.Skills, "技能", "skills"),
    PROFILE(Screen.Profile, "我的", "profile")
}
