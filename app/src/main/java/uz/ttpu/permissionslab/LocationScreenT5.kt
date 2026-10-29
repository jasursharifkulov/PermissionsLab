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

private enum class LocationAccess(val label: String) {
    PRECISE("precise"), APPROXIMATE("approximate"), NONE("none")
}

@Composable
fun LocationScreen() {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val fine = Manifest.permission.ACCESS_FINE_LOCATION
    val coarse = Manifest.permission.ACCESS_COARSE_LOCATION
    val prefs = remember(context) {
        context.getSharedPreferences("permission_history", Context.MODE_PRIVATE)
    }
    fun readAccess(): LocationAccess = when {
        ContextCompat.checkSelfPermission(context, fine) == PackageManager.PERMISSION_GRANTED -> LocationAccess.PRECISE
        ContextCompat.checkSelfPermission(context, coarse) == PackageManager.PERMISSION_GRANTED -> LocationAccess.APPROXIMATE
        else -> LocationAccess.NONE
    }
    var access by remember { mutableStateOf(readAccess()) }
    var resultText by remember { mutableStateOf("No permission request on this screen yet") }
    var requesting by remember { mutableStateOf(false) }
    var blocked by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    fun refresh() {
        access = readAccess()
        // A successful grant ends the old denial history. When a one-time grant
        // later expires, allow a fresh request instead of treating it as blocked.
        if (access != LocationAccess.NONE) {
            prefs.edit().putBoolean("location_requested", false).apply()
        }
        val canExplain = activity != null && (
            ActivityCompat.shouldShowRequestPermissionRationale(activity, fine) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity, coarse)
            )
        showRationale = access == LocationAccess.NONE && canExplain
        blocked = access == LocationAccess.NONE && !canExplain &&
            prefs.getBoolean("location_requested", false)
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        prefs.edit().putBoolean("location_requested", true).apply()
        requesting = false
        // Read the contract's Map<String, Boolean>: fine takes precedence over coarse.
        access = when {
            permissions[fine] == true -> LocationAccess.PRECISE
            permissions[coarse] == true -> LocationAccess.APPROXIMATE
            else -> LocationAccess.NONE
        }
        resultText = "FINE=${permissions[fine] == true}, COARSE=${permissions[coarse] == true}"
        Log.d("PermissionsLab", "Task5 result: $resultText -> ${access.label}")
        refresh()
    }
    // One-time access can expire in the background. Read actual grants on EVERY
    // resume, never assume an earlier successful callback still means granted.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        refresh()
        Log.d("PermissionsLab", "Task5 ON_RESUME: location=${access.label}, API=${Build.VERSION.SDK_INT}")
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Task 5 · One-time location", style = MaterialTheme.typography.titleLarge)
        Text(access.label, style = MaterialTheme.typography.headlineLarge)
        Text("Android API ${Build.VERSION.SDK_INT}")
        Text("Last request result: $resultText")
        Text("Choose Precise or Approximate, then 'Only this time'. The screen checks real access whenever you return.")
        if (showRationale) {
            Text("Location access would allow nearby features. Approximate access is sufficient for a general area. You can continue without granting it.")
        }
        if (!blocked && access == LocationAccess.NONE) {
            Button(enabled = !requesting, onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requesting = true
                    launcher.launch(arrayOf(fine, coarse))
                } else refresh()
            }) { Text("Request location") }
        }
        Button(onClick = {
            context.startActivity(Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ))
        }) { Text("Open app Settings") }
        Text("A quick trip to the background may keep one-time access temporarily. After the system revokes it, returning must show 'none'. No coordinates are collected in this lab.")
    }
}
