package ai.nuwa.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    var darkMode by remember { mutableStateOf(false) }
    var autoLearn by remember { mutableStateOf(true) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("设置") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                SettingsSection(title = "女娲身份") {
                    SettingsItem(
                        title = "名字",
                        subtitle = "女娲",
                        onClick = { }
                    )
                    SettingsItem(
                        title = "称呼方式",
                        subtitle = "直接称呼",
                        onClick = { }
                    )
                    SettingsItem(
                        title = "性格选择",
                        subtitle = "温柔型",
                        onClick = { }
                    )
                }
            }
            
            item {
                SettingsSection(title = "通用") {
                    SettingsSwitchItem(
                        title = "深色模式",
                        subtitle = "跟随系统",
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                    SettingsItem(
                        title = "语言",
                        subtitle = "简体中文",
                        onClick = { }
                    )
                    SettingsItem(
                        title = "通知",
                        subtitle = "任务完成时通知",
                        onClick = { }
                    )
                }
            }
            
            item {
                SettingsSection(title = "成长设置") {
                    SettingsSwitchItem(
                        title = "自动学习",
                        subtitle = "充电时自动整理经验",
                        checked = autoLearn,
                        onCheckedChange = { autoLearn = it }
                    )
                    SettingsItem(
                        title = "学习时间",
                        subtitle = "充电时",
                        onClick = { }
                    )
                    SettingsItem(
                        title = "存储预算",
                        subtitle = "1 GB",
                        onClick = { }
                    )
                }
            }
            
            item {
                SettingsSection(title = "权限") {
                    SettingsItem(
                        title = "无障碍权限",
                        subtitle = "已授权",
                        onClick = { },
                        trailing = {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    SettingsItem(
                        title = "通知权限",
                        subtitle = "已授权",
                        onClick = { },
                        trailing = {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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
    trailing: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
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
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.Default.ArrowForward,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
