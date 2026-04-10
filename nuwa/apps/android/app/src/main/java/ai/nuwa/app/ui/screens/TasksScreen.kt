package ai.nuwa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ai.nuwa.app.data.model.Task
import ai.nuwa.app.data.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<Task> = emptyList(),
    onTaskClick: (Task) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("进行中", "已完成", "已失败")
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("任务中心") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        val filteredTasks = remember(tasks, selectedTab) {
            when (selectedTab) {
                0 -> tasks.filter { it.status == TaskStatus.Running || it.status == TaskStatus.Idle }
                1 -> tasks.filter { it.status == TaskStatus.Completed }
                2 -> tasks.filter { it.status == TaskStatus.Failed }
                else -> tasks
            }
        }
        
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        when (selectedTab) {
                            0 -> Icons.Default.PlayCircle
                            1 -> Icons.Default.CheckCircle
                            else -> Icons.Default.Warning
                        },
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (selectedTab) {
                            0 -> "暂无进行中的任务"
                            1 -> "暂无已完成的任务"
                            else -> "暂无失败的任务"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedTab == 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "在首页发送指令来创建任务",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTasks) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onTaskClick(task) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when (task.status) {
                                    TaskStatus.Running -> MaterialTheme.colorScheme.primaryContainer
                                    TaskStatus.Completed -> MaterialTheme.colorScheme.tertiaryContainer
                                    TaskStatus.Failed -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (task.status) {
                                TaskStatus.Running -> Icons.Default.PlayArrow
                                TaskStatus.Completed -> Icons.Default.CheckCircle
                                TaskStatus.Failed -> Icons.Default.Warning
                                else -> Icons.Default.Pending
                            },
                            contentDescription = null,
                            tint = when (task.status) {
                                TaskStatus.Running -> MaterialTheme.colorScheme.primary
                                TaskStatus.Completed -> MaterialTheme.colorScheme.tertiary
                                TaskStatus.Failed -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = task.intent.rawText.take(30),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = formatTimestamp(task.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                when (task.status) {
                    TaskStatus.Running -> {
                        TextButton(onClick = onClick) {
                            Text("继续")
                        }
                    }
                    TaskStatus.Completed -> {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    TaskStatus.Failed -> {
                        TextButton(onClick = onClick) {
                            Text("重试")
                        }
                    }
                    else -> {}
                }
            }
            
            if (task.status == TaskStatus.Running) {
                Spacer(modifier = Modifier.height(12.dp))
                val completedSteps = task.steps.count { it.status == ai.nuwa.app.data.model.StepStatus.Success }
                val totalSteps = task.steps.size
                val progress = if (totalSteps > 0) completedSteps.toFloat() / totalSteps else 0f
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "$completedSteps/$totalSteps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                task.steps.getOrNull(task.currentStep)?.message?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        else -> SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
