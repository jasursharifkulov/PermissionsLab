package uz.ttpu.permissionslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uz.ttpu.permissionslab.ui.theme.PermissionsLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PermissionsLabTheme {
                var screen by rememberSaveable { mutableIntStateOf(0) }
                Scaffold(Modifier.fillMaxSize()) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                        Text("PermissionsLab", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { screen = 0 }) { Text("2. Network") }
                        }
                        Spacer(Modifier.height(16.dp))
                        when (screen) {
                            0 -> NetworkScreen()
                        }
                    }
                }
            }
        }
    }
}
