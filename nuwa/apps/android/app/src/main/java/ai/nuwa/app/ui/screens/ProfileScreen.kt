package ai.nuwa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ai.nuwa.app.data.repository.OperationLogEntry
import ai.nuwa.app.data.repository.UserStats
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentModelName: String? = null,
    learnedSkillsCount: Int = 0,
    operationLogs: List<OperationLogEntry> = emptyList(),
    userStats: UserStats = UserStats(),
    appVersion: String = "1.0.0",
    onNavigateToSettings: () -> Unit = {},
    onNavigateToModelManager: () -> Unit = {},
    onNavigateToGrowth: () -> Unit = {},
    onNavigateToStorage: () -> Unit = {},
    onRefreshLogs: () -> Unit = {}
) {
    var selectedSection by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        onRefreshLogs()
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("我的") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        when (selectedSection) {
            "logs" -> OperationLogsContent(
                logs = operationLogs,
                onBack = { selectedSection = null }
            )
            "help" -> HelpContent(onBack = { selectedSection = null })
            "about" -> AboutContent(onBack = { selectedSection = null })
            else -> ProfileMainContent(
                currentModelName = currentModelName,
                learnedSkillsCount = learnedSkillsCount,
                userStats = userStats,
                appVersion = appVersion,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToModelManager = onNavigateToModelManager,
                onNavigateToGrowth = onNavigateToGrowth,
                onNavigateToStorage = onNavigateToStorage,
                onNavigateToLogs = { selectedSection = "logs" },
                onNavigateToHelp = { selectedSection = "help" },
                onNavigateToAbout = { selectedSection = "about" }
            )
        }
    }
}

@Composable
fun ProfileMainContent(
    currentModelName: String? = null,
    learnedSkillsCount: Int = 0,
    userStats: UserStats = UserStats(),
    appVersion: String = "1.0.0",
    onNavigateToSettings: () -> Unit = {},
    onNavigateToModelManager: () -> Unit = {},
    onNavigateToGrowth: () -> Unit = {},
    onNavigateToStorage: () -> Unit = {},
    onNavigateToLogs: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ProfileHeader(
                learnedSkillsCount = learnedSkillsCount,
                level = userStats.level,
                completionRate = if (userStats.totalTasks > 0) userStats.completedTasks.toFloat() / userStats.totalTasks else 0f
            )
        }
        
        item { Spacer(modifier = Modifier.height(8.dp)) }
        
        item {
            ProfileSection(title = "能力成长") {
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "成长中心",
                    subtitle = "等级 ${userStats.level} | 经验 ${userStats.experience}",
                    onClick = onNavigateToGrowth
                )
                ProfileMenuItem(
                    icon = Icons.Filled.Psychology,
                    title = "模型管理",
                    subtitle = if (currentModelName != null) "当前使用 $currentModelName" else "请先导入模型",
                    onClick = onNavigateToModelManager
                )
            }
        }
        
        item {
            ProfileSection(title = "数据与日志") {
                ProfileMenuItem(
                    icon = Icons.Default.History,
                    title = "操作日志",
                    subtitle = "查看所有操作记录",
                    onClick = onNavigateToLogs
                )
                ProfileMenuItem(
                    icon = Icons.Filled.Storage,
                    title = "存储管理",
                    subtitle = "管理缓存和模型文件",
                    onClick = onNavigateToStorage
                )
            }
        }
        
        item {
            ProfileSection(title = "支持") {
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "设置",
                    subtitle = "无障碍权限和其他设置",
                    onClick = onNavigateToSettings
                )
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "帮助与反馈",
                    subtitle = "使用遇到问题",
                    onClick = onNavigateToHelp
                )
                ProfileMenuItem(
                    icon = Icons.Filled.Info,
                    title = "关于我们",
                    subtitle = "版本 $appVersion",
                    onClick = onNavigateToAbout
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(
    learnedSkillsCount: Int = 0,
    level: Int = 1,
    completionRate: Float = 0f
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "娲",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "女娲",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "你的智能伙伴",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Lv.$level") },
                        leadingIcon = { Icon(Icons.Filled.Star, null, Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text("已学 $learnedSkillsCount 项技能") }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationLogsContent(
    logs: List<OperationLogEntry> = emptyList(),
    onBack: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("操作日志") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无操作日志",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "执行任务后会记录操作历史",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs.size) { index ->
                    LogItem(log = logs[index])
                }
            }
        }
    }
}

@Composable
fun LogItem(log: OperationLogEntry) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val formattedDate = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (log.status) {
                    ai.nuwa.app.data.repository.OperationStatus.Success -> Icons.Filled.CheckCircle
                    ai.nuwa.app.data.repository.OperationStatus.Failed -> Icons.Filled.Warning
                    ai.nuwa.app.data.repository.OperationStatus.Running -> Icons.Filled.HourglassTop
                },
                null,
                tint = when (log.status) {
                    ai.nuwa.app.data.repository.OperationStatus.Success -> MaterialTheme.colorScheme.primary
                    ai.nuwa.app.data.repository.OperationStatus.Failed -> MaterialTheme.colorScheme.error
                    ai.nuwa.app.data.repository.OperationStatus.Running -> MaterialTheme.colorScheme.tertiary
                },
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.action,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = log.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                text = when (log.status) {
                    ai.nuwa.app.data.repository.OperationStatus.Success -> "成功"
                    ai.nuwa.app.data.repository.OperationStatus.Failed -> "失败"
                    ai.nuwa.app.data.repository.OperationStatus.Running -> "进行中"
                },
                style = MaterialTheme.typography.labelMedium,
                color = when (log.status) {
                    ai.nuwa.app.data.repository.OperationStatus.Success -> MaterialTheme.colorScheme.primary
                    ai.nuwa.app.data.repository.OperationStatus.Failed -> MaterialTheme.colorScheme.error
                    ai.nuwa.app.data.repository.OperationStatus.Running -> MaterialTheme.colorScheme.tertiary
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpContent(onBack: () -> Unit) {
    val faqs = listOf(
        FaqItem("如何导入AI模型？", "在\"我的\"页面点击\"模型管理\"，然后点击右下角的\"+\"按钮选择GGUF格式的模型文件。推荐使用4-8B参数的中文优化模型。"),
        FaqItem("无障碍权限有什么用？", "无障碍权限让女娲能够读取屏幕内容和执行点击操作，是完成任务的基础。请在系统设置中开启女娲的无障碍权限。"),
        FaqItem("为什么任务执行失败？", "可能原因：1. 无障碍权限未开启 2. 目标应用界面结构不识别 3. 网络问题。请查看任务详情中的错误信息。"),
        FaqItem("模型文件存放在哪里？", "模型文件保存在应用私有目录，不会上传到任何服务器，完全本地运行。"),
        FaqItem("如何删除已导入的模型？", "在\"模型管理\"页面，长按要删除的模型，点击删除按钮即可。")
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("帮助与反馈") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Lightbulb,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "常见问题解答",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            
            items(faqs) { faq ->
                FaqItem(faq = faq)
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "联系我们",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "如果你有其他问题或建议，欢迎通过以下方式联系我们：",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Icon(
                                Icons.Filled.Email,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "support@nuwa.ai",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

data class FaqItem(
    val question: String,
    val answer: String
)

@Composable
fun FaqItem(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutContent(onBack: () -> Unit) {
    val context = LocalContext.current
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    
    val versionName = packageInfo?.versionName ?: "未知"
    val buildDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("关于我们") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "娲",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "女娲",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Nuwa - 端侧自主智能体系统",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AssistChip(
                onClick = { },
                label = { Text("版本 $versionName") }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AboutItem("版本", versionName)
                    AboutItem("构建日期", buildDate)
                    AboutItem("技术栈", "Rust + Kotlin + Compose")
                    AboutItem("许可证", "Apache 2.0")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "核心特性",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "端侧AI推理，保护隐私\n无障碍服务控制\n本地GGUF模型支持\n中文场景优化\n任务执行与学习",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "女娲致力于打造用户主权、结果可追溯的智能助手",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AboutItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
