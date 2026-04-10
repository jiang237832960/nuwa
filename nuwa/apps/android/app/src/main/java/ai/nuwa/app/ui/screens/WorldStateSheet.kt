package ai.nuwa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ai.nuwa.app.data.model.UiTreeNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldStateSheet(
    isAccessibilityEnabled: Boolean = false,
    foregroundApp: String = "",
    uiTree: UiTreeNode? = null,
    onRequestAccessibility: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "世界状态",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (!isAccessibilityEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "需要无障碍权限",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "请在设置中开启女娲的无障碍权限，才能查看世界状态",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FilledTonalButton(onClick = onRequestAccessibility) {
                            Text("去设置")
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatusCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Apps,
                        title = "当前应用",
                        value = foregroundApp.ifEmpty { "未知" },
                        subtitle = foregroundApp.ifEmpty { "无法获取" }
                    )
                    StatusCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BatteryFull,
                        title = "电量",
                        value = "--",
                        subtitle = "无法获取"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatusCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Memory,
                        title = "内存",
                        value = "--",
                        subtitle = "无法获取"
                    )
                    StatusCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Storage,
                        title = "存储",
                        value = "--",
                        subtitle = "无法获取"
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "界面预览",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CropOriginal,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "需要无障碍权限",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "识别到的元素",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (uiTree != null) {
                    UiTreeDisplay(node = uiTree)
                } else {
                    Text(
                        text = "开启无障碍权限后自动显示界面元素",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun UiTreeDisplay(node: UiTreeNode) {
    val elements = remember { flattenUiTree(node).take(10) }
    
    elements.forEach { element ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (element.enabled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = if (element.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = element.text?.take(20) ?: element.className ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = element.className?.take(15) ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class DisplayElement(
    val text: String?,
    val className: String?,
    val enabled: Boolean
)

private fun flattenUiTree(node: UiTreeNode, depth: Int = 0): List<DisplayElement> {
    if (depth > 3) return emptyList()
    
    val result = mutableListOf<DisplayElement>()
    if (node.text != null || node.className != null) {
        result.add(DisplayElement(
            text = node.text,
            className = node.className,
            enabled = node.enabled
        ))
    }
    
    for (child in node.children) {
        result.addAll(flattenUiTree(child, depth + 1))
    }
    
    return result
}

@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
