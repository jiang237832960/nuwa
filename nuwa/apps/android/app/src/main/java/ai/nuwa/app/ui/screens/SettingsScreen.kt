package ai.nuwa.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isAccessibilityEnabled: Boolean = false,
    onRequestAccessibility: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("设置") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                SettingsSection(title = "权限（必须）") {
                    SettingsItem(
                        title = "无障碍权限",
                        subtitle = if (isAccessibilityEnabled) "已授权" else "需要开启才能使用任务执行",
                        onClick = onRequestAccessibility,
                        trailing = {
                            if (isAccessibilityEnabled) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                FilledTonalButton(
                                    onClick = onRequestAccessibility,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("开启")
                                }
                            }
                        }
                    )
                }
            }
            
            item {
                SettingsSection(title = "通用设置") {
                    SettingsItem(
                        title = "深色模式",
                        subtitle = "暂未实现",
                        onClick = { },
                        enabled = false
                    )
                    SettingsItem(
                        title = "语言",
                        subtitle = "暂未实现",
                        onClick = { },
                        enabled = false
                    )
                }
            }
            
            item {
                SettingsSection(title = "成长设置") {
                    SettingsItem(
                        title = "自动学习",
                        subtitle = "暂未实现",
                        onClick = { },
                        enabled = false
                    )
                    SettingsItem(
                        title = "学习时间",
                        subtitle = "暂未实现",
                        onClick = { },
                        enabled = false
                    )
                }
            }
            
            item {
                SettingsSection(title = "关于") {
                    SettingsItem(
                        title = "关于女娲",
                        subtitle = "版本 0.1.0",
                        onClick = { }
                    )
                    SettingsItem(
                        title = "帮助与反馈",
                        subtitle = "暂未实现",
                        onClick = { },
                        enabled = false
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
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
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                content = { content() }
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = if (enabled) onClick else { {} },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailing != null) {
                trailing()
            } else if (enabled) {
                Icon(
                    Icons.Default.ArrowForward,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
