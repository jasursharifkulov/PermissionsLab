# Practice 8 — Permissions: Part A Answers

Student: Jasur Sharifkulov  
Group: ____________________  
Date: ____________________

Answers are written in English using the worksheet, Android documentation and our lab observations. Lecture 10 slides were not provided, so the workflow wording and model explanations below are reconstructions rather than quotations from the lecture. Code snippets assume the relevant Android, AndroidX and Compose imports.

## 1. Permission workflow

The four main decisions are: (1) can the feature work without a permission; (2) if not, which permission must be declared; (3) is it granted at installation or does it need a runtime request; (4) is it special access that must be handled through Settings? For runtime access, check the current grant, explain the need when appropriate, and request only when the user invokes the feature.

For example, an app can delegate photo capture to an installed camera app instead of directly controlling camera hardware. Avoiding unnecessary permissions reduces access to private data and user interruptions. The four-part wording is a reconstruction of the permission workflow, not a verified transcription of the slides. [Source](https://developer.android.com/guide/topics/permissions/overview).

## 2. Three categories

| Category | How it is granted | Application code |
|---|---|---|
| Install-time | Automatically at installation, if eligibility conditions are met | Manifest declaration; signature permissions also require an accepted signing identity |
| Runtime | Through a system prompt during app use | Declaration, grant check, request and denial handling |
| Special | Usually through a dedicated Settings page | Declaration, permission-specific check, Settings intent and return-time check |

The category determines the access flow; a single manifest declaration is not sufficient for all categories. [Source](https://developer.android.com/guide/topics/permissions/overview).

## 3. Normal permissions

Normal permissions do not require a runtime prompt or a repeated runtime permission check. INTERNET and ACCESS_NETWORK_STATE are granted at installation when declared. Three examples are INTERNET, ACCESS_NETWORK_STATE and VIBRATE; the exact third example used in the unavailable lecture cannot be confirmed.

Without INTERNET, network access is prohibited. The worksheet expects a SecurityException; depending on the networking implementation, a permission-related failure can also be wrapped in another exception. Our test button records the actual exception instead of inventing a result. [Source](https://developer.android.com/develop/connectivity/network-ops/connecting).

## 4. Signature permission

A signature permission is normally granted to a requesting app signed with the same certificate as the app declaring it. Bank declares:

```xml
<permission
    android:name="uz.ttpu.bank.permission.READ_ACCOUNTS"
    android:protectionLevel="signature" />
```

Wallet must request it using uses-permission. If Wallet has the matching signature, it can receive the permission and bind to the protected service, assuming other component conditions allow it. A stranger app signed differently does not receive it. No user dialog can override the signature requirement. [Source](https://developer.android.com/guide/topics/manifest/permission-element).

## 5. Install-time permissions

An app store can show install-time permission information on the app details page. The system grants eligible permissions during installation, not through a runtime prompt. This category includes normal permissions and signature permissions; signature permissions still require the correct signature. [Source](https://developer.android.com/guide/topics/permissions/overview).

## 6. API 22 versus modern Android

Runtime permissions were introduced in Android 6.0, API 23. On an API 22 device, declared dangerous permissions such as CAMERA follow the old install-time model. A modern-targeting app on Android 14 requests CAMERA during use.

A simplified granted(kind, sdk) model therefore treats a runtime-kind permission as install-time granted below 23. At 23 and above, its result must depend on the actual user grant. The model must not automatically grant signature or special permissions merely because the SDK is old. [Source](https://developer.android.com/training/permissions/requesting).

## 7. Denial states

For the worksheet's ordinary camera-denial scenario on API 30+:

| State | Granted | shouldShowRationale | Action |
|---|---|---|---|
| Not requested yet | false | false | Offer a request button |
| Denied once | false | true | Explain the benefit; offer another request |
| Denied twice | false | false | Offer app Settings; do not repeatedly launch a blocked prompt |

False alone cannot distinguish first use from a blocked request. Combine the current grant and rationale with request history. Open Settings.ACTION_APPLICATION_DETAILS_SETTINGS with the package URI when the user chooses Settings. Earlier Android versions can have a separate “don't ask again” choice. [Sources: Android 11 behavior](https://developer.android.com/about/versions/11/privacy/permissions), [request flow](https://developer.android.com/training/permissions/requesting).

## 8. Accompanist branches

isGranted displays the permitted feature. shouldShowRationale displays an explanation and an optional retry button. The remaining branch displays a request UI, but needs request history to distinguish first use from a case requiring Settings.

rememberPermissionState remembers its state holder across recompositions and exposes permission status; it does not make the permission permanent. ExperimentalPermissionsApi requires an opt-in because the library API may change. Launch requests from a callback such as onClick, not directly while composing the UI. [Source](https://google.github.io/accompanist/permissions/).

## 9. Request without an additional permission library

Use rememberLauncherForActivityResult with ActivityResultContracts.RequestPermission. Its callback receives a Boolean. Calling launcher.launch(Manifest.permission.CAMERA) requests access.

ContextCompat.checkSelfPermission reads the actual grant. ActivityCompat.shouldShowRequestPermissionRationale decides whether an explanation is appropriate. Activity Result API needs more explicit state handling; Accompanist wraps common state and request operations but has the same platform limitations. Our project uses Activity Result API. [Sources: request API](https://developer.android.com/training/permissions/requesting), [Accompanist](https://google.github.io/accompanist/permissions/).

## 10. Multiple location permissions

RequestMultiplePermissions returns Map<String, Boolean>. Read the FINE_LOCATION entry first: true means precise. If only COARSE_LOCATION is true, access is approximate. If neither is true, access is none.

Requesting both together lets the system present one location interface where the user chooses accuracy and access duration. If both are denied, continue without location-dependent functionality; do not block unrelated screens. [Source](https://developer.android.com/develop/sensors-and-location/location/permissions/runtime).

## 11. Special access checks

Special access uses permission-specific checks and usually Settings, rather than the ordinary RequestPermission dialog.

| Permission | What it allows | Check |
|---|---|---|
| SYSTEM_ALERT_WINDOW | Draw over other applications | Settings.canDrawOverlays(context) |
| WRITE_SETTINGS | Modify supported system settings | Settings.System.canWrite(context) |
| MANAGE_EXTERNAL_STORAGE | Broad shared-storage access, subject to platform restrictions | Environment.isExternalStorageManager(), API 30+ |
| REQUEST_INSTALL_PACKAGES | Request APK installation from this source; installation still requires user action | context.packageManager.canRequestPackageInstalls(), API 26+ |
| ACCESS_BACKGROUND_LOCATION | Location access while the app is in the background | ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION), API 29+ |

ACCESS_BACKGROUND_LOCATION appears in the worksheet's table but is technically a dangerous runtime permission, not a special-protection permission. Modern Android handles its grant through a separate background-location flow. [Sources: special access](https://developer.android.com/training/permissions/requesting-special), [background location](https://developer.android.com/develop/sensors-and-location/location/permissions/background).

## 12. Refreshing overlay access

A value initialized inside remember is not automatically refreshed by changes in Settings. The flow is check, open Settings, and check again after returning. There is no reliable ordinary permission-result Boolean from this Settings activity; read the authoritative check on resume.

```kotlin
@Composable
fun OverlayScreen() {
    val context = LocalContext.current
    var canDraw by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        canDraw = Settings.canDrawOverlays(context)
    }
    Switch(
        checked = canDraw,
        onCheckedChange = {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
            )
        }
    )
}
```

Do not flip the switch optimistically: only the system grant determines its value. [Sources: Settings workflow](https://developer.android.com/training/permissions/requesting-special), [lifecycle effects](https://developer.android.com/topic/libraries/architecture/lifecycle).

## 13. Background location order

Step 1 requests foreground location, using fine and coarse together when precise access is needed. Step 2 is considered only after foreground access is granted and a feature actually needs background access.

The SDK_INT >= Q guard is needed because ACCESS_BACKGROUND_LOCATION was introduced in API 29. Android 10 can offer “Allow all the time” in the permission flow. On Android 11+ for modern-targeting apps, users enable background access in Settings. Requesting foreground and background together is not a valid replacement for this incremental flow. [Source](https://developer.android.com/develop/sensors-and-location/location/permissions/background).

## 14. Permission groups

A permission group collects related permissions and helps Android reduce prompt count. Examples are LOCATION, CONTACTS and CAMERA. The worksheet's five requests can therefore lead to three interfaces rather than five individual dialogs.

An app must check each required permission explicitly. Group membership and prompt behavior can change across Android versions; granting one permission is not a promise that every permission in its group is granted. [Source](https://developer.android.com/training/permissions/requesting).

## 15. Manifest declaration and maxSdkVersion

Requesting CAMERA without declaring uses-permission does not grant access; the runtime request cannot create a missing manifest entitlement. Install-time permissions need declaration, while runtime and special access also require their appropriate grant process.

maxSdkVersion states the highest device API level at which the declaration applies. For example, writing to the app's own external-files directory required WRITE_EXTERNAL_STORAGE on API 18 and below, but not from API 19 onward:

```xml
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="18" />
```

This example is specifically about app-owned external directories, not unrestricted access to arbitrary shared files. [Source](https://developer.android.com/guide/topics/manifest/uses-permission-element).

## 16. Optional hardware and store filtering

required=true tells Google Play that the hardware is necessary. required=false allows distribution to devices without that feature, subject to other requirements.

| Camera required? | Device has camera? | Visible under this feature rule? |
|---|---|---|
| true | true | yes |
| true | false | no |
| false | true | yes |
| false | false | yes |

For the declared rear-camera feature, check packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA). If any camera is sufficient, declare/check FEATURE_CAMERA_ANY instead. Disable or replace camera functionality when hardware is absent. Hardware availability and permission grant are separate checks. The table reconstructs the visibleOn rule; it does not claim execution of the unavailable lecture implementation. [Source](https://developer.android.com/guide/topics/manifest/uses-feature-element).

## 17. One-time access and stale state

Android 11/API 30 introduced “Only this time” for camera, microphone and location. Access is temporary: it can continue briefly after backgrounding, and its duration depends on app behavior. Never treat an old granted value as permanent.

```kotlin
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    fun currentlyGranted() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    var hasCameraAccess by remember { mutableStateOf(currentlyGranted()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hasCameraAccess = currentlyGranted()
    }
    Text(if (hasCameraAccess) "Permission granted" else "No camera permission")
}
```

Also check access before a protected operation. In our API 37 location experiment, precise access survived a quick return but became none after approximately two minutes in the background. That observed delay is not a guaranteed timeout. [Sources: one-time access](https://developer.android.com/about/versions/11/privacy/permissions), [lifecycle integration](https://developer.android.com/topic/libraries/architecture/lifecycle).

## 18. Auto-reset and voluntary revocation

Auto-reset removes sensitive runtime grants from eligible apps unused for months. When the user returns, check access and request again only when the relevant feature is invoked. Users can disable unused-permission removal in app Settings. On supported systems, the lecture's Settings.ACTION_AUTO_REVOKE_PERMISSIONS intent with the package URI opens the related management screen; AndroidX IntentCompat.createManageUnusedAppRestrictionsIntent provides a compatibility approach.

On API 33+, revokeSelfPermissionOnKill(permissionName) lets an app surrender an unnecessary runtime permission, such as READ_CONTACTS. Revocation is asynchronous and involves terminating the app's processes when safe; it is not an immediate in-place update. Giving up unused access reduces exposure of private data. [Sources: unused-app permissions](https://developer.android.com/topic/performance/app-hibernation), [self-revocation](https://developer.android.com/training/permissions/requesting).

## 19. Version summary and notifications

| API | Main permission change |
|---|---|
| 23 / Android 6 | Dangerous permissions moved to runtime requests for modern-targeting apps. |
| 30 / Android 11 | One-time grants and unused-app runtime-permission reset were introduced. |
| 33 / Android 13 | POST_NOTIFICATIONS became a runtime permission for non-exempt notifications. |

Declare POST_NOTIFICATIONS and request it only when SDK_INT >= 33. Below 33 there is no such runtime prompt, although users can still disable notifications in Settings and notification channels may be blocked. Exemptions and notification settings remain relevant even after a permission grant. [Source](https://developer.android.com/develop/ui/compose/notifications/notification-permission).
