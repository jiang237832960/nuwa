package ai.nuwa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import ai.nuwa.app.ui.MainViewModel
import ai.nuwa.app.ui.screens.MainScreen
import ai.nuwa.app.ui.theme.NuwaTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        setContent {
            NuwaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val messages by viewModel.messages.collectAsState()
                    val currentTask by viewModel.currentTask.collectAsState()
                    val serviceStatus by viewModel.serviceStatus.collectAsState()
                    
                    MainScreen(
                        messages = messages,
                        currentTask = currentTask,
                        serviceStatus = serviceStatus,
                        onSendMessage = { viewModel.sendMessage(it) },
                        onCancelTask = { viewModel.cancelTask() },
                        onRefreshState = { viewModel.refreshWorldState() }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.checkServiceStatus()
    }
}
