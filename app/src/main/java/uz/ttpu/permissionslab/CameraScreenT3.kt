package uz.ttpu.permissionslab

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

private data class CameraAccess(
    val granted: Boolean,
    val rationale: Boolean,
    val requestedBefore: Boolean
)

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val activity = LocalActivity.current
    // Request history distinguishes first use from a denial with no rationale.
    // It is NOT a cached grant: actual access is checked again on every resume.
    val prefs = remember(context) {
        context.getSharedPreferences("permission_history", Context.MODE_PRIVATE)
    }
    fun readAccess(): CameraAccess {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) prefs.edit().putBoolean("camera_requested", false).apply()
        val rationale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null &&
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
        return CameraAccess(granted, rationale, prefs.getBoolean("camera_requested", false))
    }
    var access by remember { mutableStateOf(readAccess()) }
    var requesting by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // Save history after the result, so a resume while the first dialog is
        // opening cannot be mistaken for a permanent denial.
        prefs.edit().putBoolean("camera_requested", true).apply()
        requesting = false
        access = readAccess()
        Log.d("PermissionsLab", "Task3 result granted=$granted, rationale=${access.rationale}")
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        access = readAccess()
        Log.d("PermissionsLab", "Task3 resume: $access")
    }

    fun requestCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requesting = true
            launcher.launch(Manifest.permission.CAMERA)
        } else {
            // On pre-23 devices this manifest permission is install-time granted.
            access = readAccess()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Task 3 · Camera permission", style = MaterialTheme.typography.titleLarge)
        when {
            access.granted -> {
                Text("Permission granted", style = MaterialTheme.typography.headlineSmall)
                Text("Camera access is allowed. This lab checks permission; it does not open a camera preview.")
            }
            access.rationale -> {
                Text("Why camera access?", style = MaterialTheme.typography.headlineSmall)
                Text("Camera access would let you take photos inside the app. It is optional; other screens still work without it.")
                Button(enabled = !requesting, onClick = { requestCamera() }) { Text("Request again") }
            }
            access.requestedBefore -> {
                Text("Camera permission denied", style = MaterialTheme.typography.headlineSmall)
                Text("The system may no longer show the request dialog. You can allow camera access in app Settings.")
                Button(onClick = {
                    context.startActivity(Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    ))
                }) { Text("Open Settings") }
            }
            else -> {
                Text("No camera permission", style = MaterialTheme.typography.headlineSmall)
                Text("Request access when you want to use the camera feature.")
                Button(enabled = !requesting, onClick = { requestCamera() }) { Text("Request camera") }
            }
        }
    }
}
