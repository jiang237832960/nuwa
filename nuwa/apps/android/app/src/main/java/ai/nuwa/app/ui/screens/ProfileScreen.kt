package ai.nuwa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentModelName: String? = null,
    learnedSkillsCount: Int = 0,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToModelManager: () -> Unit = {},
    onNavigateToGrowth: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("我的") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ProfileHeader(
                    learnedSkillsCount = learnedSkillsCount
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                ProfileSection(title = "能力成长") {
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        title = "成长中心",
                        subtitle = "查看学习进度和能力提升",
                        onClick = onNavigateToGrowth,
                        enabled = false
                    )
                    ProfileMenuItem(
                        icon = Icons.Outlined.Psychology,
                        title = "模型管理",
                        subtitle = if (currentModelName != null) "当前使用 $currentModelName" else "请先导入模型",
                        onClick = onNavigateToModelManager
                    )
                }
            }
            
            item {
                ProfileSection(title = "设置") {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Settings,
                        title = "基本设置",
                        subtitle = "主题、语言、通知",
                        onClick = onNavigateToSettings,
                        enabled = false
                    )
                    ProfileMenuItem(
                        icon = Icons.Outlined.Security,
                        title = "权限管理",
                        subtitle = "无障碍、通知、存储",
                        onClick = onNavigateToSettings,
                        enabled = false
                    )
                    ProfileMenuItem(
                        icon = Icons.Filled.History,
                        title = "操作日志",
                        subtitle = "查看所有操作记录",
                        onClick = onNavigateToSettings,
                        enabled = false
                    )
                }
            }
            
            item {
                ProfileSection(title = "关于") {
                    ProfileMenuItem(
                        icon = Icons.Filled.Info,
                        title = "关于女娲",
                        subtitle = "版本 0.1.0",
                        onClick = onNavigateToSettings,
                        enabled = false
                    )
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "帮助与反馈",
                        subtitle = "使用遇到问题",
                        onClick = onNavigateToSettings,
                        enabled = false
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    learnedSkillsCount: Int = 0
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Lv.1") },
                        leadingIcon = { Icon(Icons.Default.Star, null, Modifier.size(16.dp)) }
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
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (enabled) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = if (enabled) 
                MaterialTheme.colorScheme.onSurfaceVariant 
            else 
                MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.outline
            )
            Text(
                text = if (enabled) subtitle else "暂未实现",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (enabled) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
