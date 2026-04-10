package ai.nuwa.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ai.nuwa.app.data.repository.ModelInfo
import ai.nuwa.app.data.repository.ModelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class StatusType { Info, Success, Error }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen(
    onBack: () -> Unit = {},
    repository: ModelRepository? = null
) {
    val context = LocalContext.current
    val modelRepository = remember { repository ?: ModelRepository(context) }
    val scope = rememberCoroutineScope()
    
    var showDeleteDialog by remember { mutableStateOf<ModelInfo?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusType by remember { mutableStateOf(StatusType.Info) }
    var isImporting by remember { mutableStateOf(false) }
    
    var models by remember { mutableStateOf<List<ModelInfo>>(emptyList()) }
    var currentModel by remember { mutableStateOf<ModelInfo?>(null) }
    
    LaunchedEffect(Unit) {
        models = modelRepository.getImportedModels()
        currentModel = modelRepository.getCurrentModel()
    }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            statusMessage = "正在导入模型..."
            statusType = StatusType.Info
            
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        modelRepository.importModel(uri)
                    }
                    
                    result.fold(
                        onSuccess = { model ->
                            statusMessage = "导入成功: ${model.name}"
                            statusType = StatusType.Success
                            models = modelRepository.getImportedModels()
                        },
                        onFailure = { error ->
                            statusMessage = error.message ?: "导入失败"
                            statusType = StatusType.Error
                        }
                    )
                } catch (e: Exception) {
                    statusMessage = "导入失败: ${e.message}"
                    statusType = StatusType.Error
                } finally {
                    isImporting = false
                }
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("模型管理") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        if (statusMessage != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = when (statusType) {
                    StatusType.Success -> MaterialTheme.colorScheme.primaryContainer
                    StatusType.Error -> MaterialTheme.colorScheme.errorContainer
                    StatusType.Info -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            when (statusType) {
                                StatusType.Success -> Icons.Default.CheckCircle
                                StatusType.Error -> Icons.Default.Error
                                StatusType.Info -> Icons.Default.Info
                            },
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = statusMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isImporting) {
                        IconButton(
                            onClick = { statusMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HelpCard()
            }
            
            if (models.isEmpty()) {
                item {
                    EmptyModelCard(
                        onImportClick = { 
                            filePickerLauncher.launch(arrayOf(
                                "*/*"
                            ))
                        }
                    )
                }
            } else {
                item {
                    Text(
                        text = "已导入的模型",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(models) { model ->
                    ImportedModelCard(
                        model = model,
                        isSelected = model.id == currentModel?.id,
                        onSelect = {
                            modelRepository.setCurrentModel(model.id)
                            currentModel = model
                            statusMessage = "已选择: ${model.name}"
                            statusType = StatusType.Success
                        },
                        onDelete = {
                            showDeleteDialog = model
                        }
                    )
                }
                
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { 
                            filePickerLauncher.launch(arrayOf(
                                "*/*"
                            ))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("导入更多模型")
                        }
                    }
                }
            }
        }
    }
    
    showDeleteDialog?.let { model ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除模型") },
            text = { Text("确定要删除模型 \"${model.name}\" 吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        modelRepository.deleteModel(model.id)
                        models = modelRepository.getImportedModels()
                        currentModel = modelRepository.getCurrentModel()
                        statusMessage = "已删除: ${model.name}"
                        statusType = StatusType.Success
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun HelpCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "关于AI模型",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "女娲需要AI模型才能工作。请导入GGUF格式的模型文件，如Qwen、LLaMA等。模型文件会保存在本地设备上，不会上传到任何服务器。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "推荐使用4-8B参数的中文优化模型，如Qwen2-4B-GGUF。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyModelCard(onImportClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Memory,
                null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无模型",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请导入一个GGUF格式的AI模型文件开始使用",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(onClick = onImportClick) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("选择模型文件")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportedModelCard(
    model: ModelInfo,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Memory,
                        null,
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.onSecondary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row {
                        Text(
                            text = formatFileSize(model.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " · ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = model.quantization,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Row {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size >= 1_073_741_824 -> String.format("%.1f GB", size / 1_073_741_824.0)
        size >= 1_048_576 -> String.format("%.1f MB", size / 1_048_576.0)
        size >= 1024 -> String.format("%.1f KB", size / 1024.0)
        else -> "$size B"
    }
}
