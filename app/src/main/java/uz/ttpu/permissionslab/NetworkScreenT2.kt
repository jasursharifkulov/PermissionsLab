package uz.ttpu.permissionslab

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun NetworkScreen() {
    val context = LocalContext.current
    val manager = remember(context) { context.getSystemService(ConnectivityManager::class.java) }
    var online by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("No request yet") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // INTERNET and ACCESS_NETWORK_STATE are normal permissions, granted at install
    // time, so there is no runtime permission check or request dialog.
    DisposableEffect(manager) {
        val handler = Handler(Looper.getMainLooper())
        var disposed = false
        fun refresh() {
            handler.post {
                if (!disposed) {
                    val caps = manager.getNetworkCapabilities(manager.activeNetwork)
                    online = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    Log.d("PermissionsLab", "Task2 network: ${if (online) "Online" else "Offline"}")
                }
            }
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = refresh()
            override fun onLost(network: Network) = refresh()
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = refresh()
        }
        manager.registerDefaultNetworkCallback(callback)
        refresh()
        onDispose {
            disposed = true
            manager.unregisterNetworkCallback(callback)
            handler.removeCallbacksAndMessages(null)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Task 2 · Normal permissions", style = MaterialTheme.typography.titleLarge)
        Text(if (online) "Online" else "Offline", style = MaterialTheme.typography.headlineLarge)
        Text("Uses INTERNET and ACCESS_NETWORK_STATE. No permission dialog is needed.")
        Text("Toggle airplane mode to test. If Wi-Fi stays enabled, turn it off too.")
        Button(enabled = !loading, onClick = {
            scope.launch {
                loading = true
                result = "Connecting…"
                try {
                    result = withContext(Dispatchers.IO) {
                        val connection = URL("https://example.com").openConnection() as HttpURLConnection
                        try {
                            connection.connectTimeout = 5_000
                            connection.readTimeout = 5_000
                            "HTTP ${connection.responseCode}"
                        } finally {
                            connection.disconnect()
                        }
                    }
                    Log.d("PermissionsLab", "Task2 network request: $result")
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    result = "${error.javaClass.simpleName}: ${error.message}"
                    Log.e("PermissionsLab", "Task2 request failed", error)
                } finally {
                    loading = false
                }
            }
        }) { Text(if (loading) "Connecting…" else "Test Internet request") }
        Text(result)
    }
}
