package ai.nuwa.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    onBack: () -> Unit = {},
    totalTasks: Int = 0,
    completedTasks: Int = 0,
    failedTasks: Int = 0,
    level: Int = 1,
    experience: Int = 0,
    learnedSkillsCount: Int = 0,
    usedDays: Int = 1,
    completionRate: Float = 0f
) {
    val nextLevelExp = getNextLevelExperience(level)
    val currentLevelExp = getCurrentLevelExperience(level)
    val progressToNext = if (nextLevelExp > currentLevelExp) {
        (experience - currentLevelExp).toFloat() / (nextLevelExp - currentLevelExp)
    } else 1f
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("成长中心") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LevelCard(
                    level = level,
                    experience = experience,
                    progressToNext = progressToNext,
                    nextLevelExp = nextLevelExp,
                    currentLevelExp = currentLevelExp
                )
            }
            
            item {
                StatsOverviewCard(
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    failedTasks = failedTasks,
                    completionRate = completionRate,
                    usedDays = usedDays
                )
            }
            
            item {
                AbilityRadarCard(
                    learnedSkillsCount = learnedSkillsCount,
                    completionRate = completionRate,
                    level = level
                )
            }
            
            item {
                GrowthTipsCard()
            }
        }
    }
}

@Composable
fun LevelCard(
    level: Int,
    experience: Int,
    progressToNext: Float,
    nextLevelExp: Int,
    currentLevelExp: Int
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
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                    
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f * progressToNext,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$level",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Lv",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "等级 ${getLevelTitle(level)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = progressToNext,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "$experience / $nextLevelExp 经验值",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun StatsOverviewCard(
    totalTasks: Int,
    completedTasks: Int,
    failedTasks: Int,
    completionRate: Float,
    usedDays: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "任务统计",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "$totalTasks",
                    label = "总任务",
                    icon = Icons.Default.Assignment
                )
                StatItem(
                    value = "$completedTasks",
                    label = "已完成",
                    icon = Icons.Default.CheckCircle,
                    valueColor = Color(0xFF4CAF50)
                )
                StatItem(
                    value = "$failedTasks",
                    label = "失败",
                    icon = Icons.Default.Cancel,
                    valueColor = Color(0xFFFF5722)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "${(completionRate * 100).toInt()}%",
                    label = "成功率",
                    icon = Icons.Default.TrendingUp
                )
                StatItem(
                    value = "$usedDays",
                    label = "使用天数",
                    icon = Icons.Default.CalendarToday
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            null,
            tint = valueColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AbilityRadarCard(
    learnedSkillsCount: Int,
    completionRate: Float,
    level: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "能力雷达",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AbilityItem(
                    label = "执行",
                    value = minOf(100, (completionRate * 100 + level * 5).toInt()),
                    color = Color(0xFF2196F3)
                )
                AbilityItem(
                    label = "学习",
                    value = minOf(100, learnedSkillsCount * 10 + level * 3),
                    color = Color(0xFF9C27B0)
                )
                AbilityItem(
                    label = "理解",
                    value = minOf(100, (completionRate * 80 + 20).toInt()),
                    color = Color(0xFFFF9800)
                )
                AbilityItem(
                    label = "规划",
                    value = minOf(100, (completionRate * 90 + 10).toInt()),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun AbilityItem(
    label: String,
    value: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GrowthTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lightbulb,
                    null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "成长建议",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val tips = listOf(
                "多使用女娲完成任务可以获得更多经验值",
                "成功完成任务比失败获得更多经验",
                "随着等级提升，女娲会学习新的技能",
                "定期使用可以保持最佳性能"
            )
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

fun getLevelTitle(level: Int): String {
    return when (level) {
        1 -> "初学者"
        2 -> "入门"
        3 -> "学徒"
        4 -> "熟练"
        5 -> "精通"
        6 -> "专家"
        7 -> "大师"
        8 -> "宗师"
        9 -> "传奇"
        10 -> "至尊"
        else -> "初学者"
    }
}

fun getCurrentLevelExperience(level: Int): Int {
    return when (level) {
        1 -> 0
        2 -> 50
        3 -> 150
        4 -> 300
        5 -> 500
        6 -> 800
        7 -> 1200
        8 -> 1700
        9 -> 2300
        10 -> 3000
        else -> 0
    }
}

fun getNextLevelExperience(level: Int): Int {
    return when (level) {
        1 -> 50
        2 -> 150
        3 -> 300
        4 -> 500
        5 -> 800
        6 -> 1200
        7 -> 1700
        8 -> 2300
        9 -> 3000
        10 -> 99999
        else -> 50
    }
}