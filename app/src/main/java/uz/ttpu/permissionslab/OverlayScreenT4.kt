package uz.ttpu.permissionslab

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun OverlayScreen() {
    val context = LocalContext.current
    var canDraw by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var error by remember { mutableStateOf<String?>(null) }

    // Settings has no reliable boolean permission result callback: re-read the
    // authoritative state whenever the screen resumes, after enable OR disable.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        canDraw = Settings.canDrawOverlays(context)
        Log.d("PermissionsLab", "Task4 resume: canDrawOverlays=$canDraw")
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Task 4 · Display over other apps", style = MaterialTheme.typography.titleLarge)
        Text("This special permission allows floating content above other apps. This lab checks the permission without displaying an overlay.")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Switch(
                checked = canDraw,
                onCheckedChange = {
                    // Do not optimistically flip the switch: only Settings can
                    // grant/revoke this access. ON_RESUME reads the real result.
                    error = null
                    try {
                        context.startActivity(Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ))
                    } catch (_: ActivityNotFoundException) {
                        error = "Overlay settings are not available on this device."
                    }
                }
            )
            Text(if (canDraw) "Allowed" else "Not allowed")
        }
        Text("Tap the switch to open Settings. If a list appears, select PermissionsLab. Change 'Allow display over other apps', then return here.")
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
