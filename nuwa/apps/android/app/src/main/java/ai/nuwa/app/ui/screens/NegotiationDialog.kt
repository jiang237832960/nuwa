package ai.nuwa.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ai.nuwa.app.data.model.NegotiationRequest
import ai.nuwa.app.data.model.NegotiationResponse
import ai.nuwa.app.data.model.NegotiationActionType
import ai.nuwa.app.data.model.NegotiationExamples

@Composable
fun NegotiationDialog(
    request: NegotiationRequest,
    onDismiss: () -> Unit,
    onResponse: (NegotiationResponse) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "娲",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = request.title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = request.content,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (request.suggestion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = request.suggestion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    request.actions.forEachIndexed { index, action ->
                        val isPrimary = index == 0
                        when (action.type) {
                            NegotiationActionType.Accept -> {
                                if (isPrimary) {
                                    Button(
                                        onClick = {
                                            onResponse(
                                                NegotiationResponse(
                                                    requestId = request.id,
                                                    action = action
                                                )
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(action.label)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            onResponse(
                                                NegotiationResponse(
                                                    requestId = request.id,
                                                    action = action
                                                )
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(action.label)
                                    }
                                }
                            }
                            NegotiationActionType.Reject -> {
                                OutlinedButton(
                                    onClick = {
                                        onResponse(
                                            NegotiationResponse(
                                                requestId = request.id,
                                                action = action
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(action.label)
                                }
                            }
                            NegotiationActionType.Delay -> {
                                TextButton(
                                    onClick = {
                                        onResponse(
                                            NegotiationResponse(
                                                requestId = request.id,
                                                action = action
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(action.label)
                                }
                            }
                            NegotiationActionType.Custom -> {
                                if (isPrimary) {
                                    Button(
                                        onClick = {
                                            onResponse(
                                                NegotiationResponse(
                                                    requestId = request.id,
                                                    action = action
                                                )
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(action.label)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            onResponse(
                                                NegotiationResponse(
                                                    requestId = request.id,
                                                    action = action
                                                )
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(action.label)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NegotiationDialogDemo() {
    var showDialog by remember { mutableStateOf(true) }
    var lastResponse by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("协商弹窗演示", style = MaterialTheme.typography.titleLarge)
            
            Button(onClick = { showDialog = true }) {
                Text("显示资源不足协商")
            }
            
            Button(onClick = { 
                showDialog = true
            }) {
                Text("显示权限请求协商")
            }
            
            lastResponse?.let {
                Text("上次选择: $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        if (showDialog) {
            NegotiationDialog(
                request = NegotiationExamples.getResourceInsufficientExample(),
                onDismiss = { showDialog = false },
                onResponse = { response ->
                    lastResponse = response.action.label
                    showDialog = false
                }
            )
        }
    }
}
