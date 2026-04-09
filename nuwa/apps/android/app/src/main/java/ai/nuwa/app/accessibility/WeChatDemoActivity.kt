package ai.nuwa.app.accessibility

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ai.nuwa.app.ui.theme.NuwaTheme
import kotlinx.coroutines.*

class WeChatDemoActivity : ComponentActivity() {
    
    private val accessibilityService get() = NuwaAccessibilityService.instance
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            NuwaTheme {
                WeChatDemoScreen(
                    onCheckService = { checkAccessibilityService() },
                    onOpenWeChat = { openWeChat() },
                    onGetUiTree = { getCurrentUiTree() },
                    onFindContact = { name -> findAndClickContact(name) },
                    onSendMessage = { message -> sendMessage(message) }
                )
            }
        }
    }
    
    private fun checkAccessibilityService(): Boolean {
        return accessibilityService != null
    }
    
    private fun openWeChat(): Boolean {
        return accessibilityService?.openWeChat() ?: false
    }
    
    private fun getCurrentUiTree(): String {
        val tree = accessibilityService?.getCurrentUiTree()
        return tree?.toString() ?: "无UI树数据"
    }
    
    private fun findAndClickContact(name: String): Boolean {
        val service = accessibilityService ?: return false
        
        val searchNode = service.findNodeByText("搜索")
        if (searchNode != null) {
            service.performClick(searchNode)
            searchNode.recycle()
            scope.launch {
                delay(500)
                val searchInput = service.findNodeByResourceId("com.tencent.mm:id/con")
                if (searchInput != null) {
                    service.performInput(searchInput, name)
                    searchInput.recycle()
                    delay(500)
                    val contactNode = service.findClickableNode(name)
                    if (contactNode != null) {
                        service.performClick(contactNode)
                        contactNode.recycle()
                    }
                }
            }
            return true
        }
        return false
    }
    
    private fun sendMessage(message: String): Boolean {
        val service = accessibilityService ?: return false
        
        scope.launch {
            delay(300)
            val inputNode = service.findNodeByResourceId("com.tencent.mm:id/aq0")
            if (inputNode != null) {
                service.performInput(inputNode, message)
                inputNode.recycle()
                delay(300)
                val sendNode = service.findNodeByText("发送")
                if (sendNode != null) {
                    service.performClick(sendNode)
                    sendNode.recycle()
                }
            }
        }
        return true
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeChatDemoScreen(
    onCheckService: () -> Boolean,
    onOpenWeChat: () -> Boolean,
    onGetUiTree: () -> String,
    onFindContact: (String) -> Boolean,
    onSendMessage: (String) -> Boolean
) {
    var serviceStatus by remember { mutableStateOf("检查中...") }
    var uiTree by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        serviceStatus = if (onCheckService()) "已连接" else "未连接"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("微信操作 Demo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("服务状态", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Accessibility Service: $serviceStatus")
                }
            }
            
            Button(
                onClick = {
                    serviceStatus = if (onCheckService()) "已连接" else "未连接"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("检查服务状态")
            }
            
            Button(
                onClick = {
                    val success = onOpenWeChat()
                    Toast.makeText(context, if (success) "打开微信成功" else "打开微信失败", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = serviceStatus == "已连接"
            ) {
                Text("打开微信")
            }
            
            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text("联系人名称") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = {
                    if (contactName.isNotBlank()) {
                        val success = onFindContact(contactName)
                        Toast.makeText(context, if (success) "查找联系人: $contactName" else "查找失败", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = serviceStatus == "已连接" && contactName.isNotBlank()
            ) {
                Text("查找联系人")
            }
            
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("发送消息") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        val success = onSendMessage(messageText)
                        Toast.makeText(context, if (success) "发送消息: $messageText" else "发送失败", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = serviceStatus == "已连接" && messageText.isNotBlank()
            ) {
                Text("发送消息")
            }
            
            Button(
                onClick = {
                    uiTree = onGetUiTree()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = serviceStatus == "已连接"
            ) {
                Text("获取当前UI树")
            }
            
            if (uiTree.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = uiTree.take(2000),
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
