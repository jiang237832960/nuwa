package ai.nuwa.app.ui.screens

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var storageInfo by remember { mutableStateOf<StorageInfo?>(null) }
    var cacheSize by remember { mutableStateOf(0L) }
    var modelSize by remember { mutableStateOf(0L) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        storageInfo = getStorageInfo()
        cacheSize = getCacheSize(context)
        modelSize = getModelSize(context)
    }
    
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("清除缓存") },
            text = { Text("确定要清除所有缓存吗？这不会影响已导入的模型。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearCache(context)
                        cacheSize = 0L
                        showClearCacheDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("存储管理") },
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
                StorageOverviewCard(storageInfo = storageInfo)
            }
            
            item {
                StorageSection(title = "存储使用详情") {
                    StorageDetailItem(
                        icon = Icons.Default.Psychology,
                        title = "AI模型",
                        size = modelSize,
                        description = "已导入的GGUF模型文件"
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    StorageDetailItem(
                        icon = Icons.Default.Memory,
                        title = "应用缓存",
                        size = cacheSize,
                        description = "临时文件和缓存数据"
                    )
                }
            }
            
            item {
                StorageSection(title = "操作") {
                    StorageActionItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "清除缓存",
                        subtitle = "释放存储空间",
                        onClick = { showClearCacheDialog = true }
                    )
                }
            }
            
            item {
                StorageSection(title = "存储说明") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "本地存储",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "女娲的所有数据都保存在设备本地，不会上传到任何服务器。模型文件存储在应用私有目录中。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StorageOverviewCard(storageInfo: StorageInfo?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Storage,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "存储概览",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (storageInfo != null) {
                LinearProgressIndicator(
                    progress = storageInfo.usedPercentage / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "已使用",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatSize(storageInfo.usedBytes),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "可用",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatSize(storageInfo.availableBytes),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Text(
                    text = "正在获取存储信息...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun StorageSection(
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
fun StorageDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    size: Long,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatSize(size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun StorageActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class StorageInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val availableBytes: Long,
    val usedPercentage: Int
)

fun getStorageInfo(): StorageInfo {
    val path = Environment.getDataDirectory()
    val stat = StatFs(path.path)
    val totalBytes = stat.blockSizeLong * stat.blockCountLong
    val availableBytes = stat.blockSizeLong * stat.availableBlocksLong
    val usedBytes = totalBytes - availableBytes
    val usedPercentage = ((usedBytes.toFloat() / totalBytes.toFloat()) * 100).toInt()
    
    return StorageInfo(
        totalBytes = totalBytes,
        usedBytes = usedBytes,
        availableBytes = availableBytes,
        usedPercentage = usedPercentage
    )
}

fun getCacheSize(context: android.content.Context): Long {
    var size = 0L
    try {
        val cacheDir = context.cacheDir
        size = getDirSize(cacheDir)
        
        val externalCacheDir = context.externalCacheDir
        if (externalCacheDir != null && externalCacheDir.exists()) {
            size += getDirSize(externalCacheDir)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return size
}

fun getModelSize(context: android.content.Context): Long {
    var size = 0L
    try {
        val modelsDir = File(context.filesDir, "models")
        if (modelsDir.exists()) {
            size = getDirSize(modelsDir)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return size
}

fun getDirSize(dir: File): Long {
    var size = 0L
    try {
        val files = dir.listFiles()
        if (files != null) {
            for (file in files) {
                size += if (file.isDirectory) {
                    getDirSize(file)
                } else {
                    file.length()
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return size
}

fun clearCache(context: android.content.Context) {
    try {
        val cacheDir = context.cacheDir
        deleteDir(cacheDir)
        
        val externalCacheDir = context.externalCacheDir
        if (externalCacheDir != null && externalCacheDir.exists()) {
            deleteDir(externalCacheDir)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun deleteDir(dir: File): Boolean {
    try {
        if (dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (child in children) {
                    deleteDir(File(dir, child))
                }
            }
        }
        return dir.delete()
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}