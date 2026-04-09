package ai.nuwa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ai.nuwa.app.ui.NuwaApp
import ai.nuwa.app.ui.theme.NuwaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            NuwaTheme {
                NuwaApp()
            }
        }
    }
}
