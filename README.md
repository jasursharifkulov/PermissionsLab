# PermissionsLab — Practice 8

Package: `uz.ttpu.permissionslab`. Kotlin + Jetpack Compose. Minimum SDK: 24. Tested manually by the student on a Pixel 7 emulator reporting Android API 37 (Android Studio device label: API 37.2).

## Task files

| Task | File | Purpose |
|---|---|---|
| 1 | `model/PermissionModel.kt` | Standalone Kotlin Playground denial-state model |
| 2 | `NetworkScreenT2.kt` | Normal permissions and live network state |
| 3 | `CameraScreenT3.kt` | Camera request, rationale and Settings |
| 4 | `OverlayScreenT4.kt` | Special overlay permission |
| 5 | `LocationScreenT5.kt` | Precise/approximate/none and one-time access |

Screen files are in `app/src/main/java/uz/ttpu/permissionslab/`. `MainActivity.kt` hosts the navigation menu. File names include task numbers; their composable function names remain `NetworkScreen`, `CameraScreen`, `OverlayScreen`, and `LocationScreen`.

## Run

Open this folder in Android Studio, complete Gradle Sync and run `app`. Scroll the top menu horizontally to reach all screens. No permission is requested automatically at startup.

## Task 1 — Kotlin Playground

Paste `model/PermissionModel.kt` into https://play.kotlinlang.org/ and press Run. The student ran it in Playground and supplied the four-step output. It was also compiled and executed locally with Kotlin. The output is saved in `evidence/permission-model-output.txt`.

The model prints granted, rationale and dialogShows. After one denial rationale is true; after two it is false and further requests print `no dialog, denied immediately`. The final `userAllows=true` has no effect because no dialog is available to accept that answer. This is a teaching model, not a complete Android permission detector. Lecture slide 13 was unavailable, so no direct comparison with its output is claimed.

## Task 2 — Normal permissions

The manifest declares INTERNET and ACCESS_NETWORK_STATE. These are normal permissions granted at install time; they do not require a runtime dialog. ConnectivityManager callbacks update the Online/Offline label, and disposal unregisters the callback. Online means the active network reports INTERNET and VALIDATED capabilities.

The test button performs a real HTTPS request away from the main thread and logs failures. The student reported that the screen and connection-state changes work. If airplane mode leaves Wi-Fi enabled, turn Wi-Fi off too for the offline test.

Still to perform: temporarily remove INTERNET, rebuild/reinstall, attempt a request and record the actual access failure; restore the manifest afterward. The worksheet expects SecurityException, while some networking stacks may expose a wrapped error.

## Task 3 — Camera

Uses ActivityResultContracts.RequestPermission, ContextCompat.checkSelfPermission and shouldShowRequestPermissionRationale. The screen displays first-request, rationale, denied/Settings and granted states. It re-checks access on ON_RESUME, including after Settings. Request history distinguishes an initial request from a denial without a rationale; history is cleared after a successful grant so a later expired one-time grant can be requested again.

The student reported successful tests of denial, repeated denial, opening Settings and returning to a granted state. Camera preview/capture is outside this permission exercise. Camera hardware is optional in the manifest. The app's minimum API is 24; request code still includes an API 23 guard for clarity.

## Task 4 — Overlay

SYSTEM_ALERT_WINDOW is declared in the manifest. Tapping the switch opens ACTION_MANAGE_OVERLAY_PERMISSION with the package URI. Some Android versions show the app list first; choose PermissionsLab. The switch changes only when the authoritative Settings.canDrawOverlays result changes. LifecycleEventEffect(ON_RESUME) refreshes it after returning. No floating overlay is created in this exercise.

The student reported successful operation. A screenshot of the system's app list is included. Screenshots of the enabled and disabled app switch still need to be added.

## Task 5 — One-time location: observation

On API 37, the student selected Precise and Only this time. The screen showed `precise` with FINE=true and COARSE=true. A quick return from the background retained access. After approximately two minutes in the background, reopening the app showed `none`. The included screenshots show the granted and expired states. This observed delay is not a guaranteed platform timeout. The screen checks real permissions on every ON_RESUME rather than trusting an old callback. The expired-state screenshot also shows that the in-memory request-result text had reset; process recreation is possible when permissions are revoked.

The RequestMultiplePermissions callback reads Map<String, Boolean>. Fine access takes precedence; otherwise coarse gives `approximate`, and neither gives `none`. The “Last request result” text is historical; the large status label is the current permission state. The app does not collect coordinates. Approximate-only selection still needs a separate screenshot/test.

## Part A — Theory

[Part-A-Answers.md](Part-A-Answers.md) contains English answers to all 19 questions, including tables and corrected Compose examples. Sources and lecture-specific limitations are identified in the file.

## Evidence and remaining submission items

Included actual screenshots:

- `evidence/overlay-settings-list.png`
- `evidence/location-precise-api37.png`
- `evidence/location-expired-none-api37.png`

Included model output: `evidence/permission-model-output.txt`.

Still add before final submission:

- Camera screenshots: first request/no access, rationale, granted; also denied/Settings and the Settings screen.
- Overlay switch screenshots: allowed and not allowed.
- Kotlin Playground code/output screenshot if required by the instructor.
- Approximate-only location test/evidence.
- Actual missing-INTERNET test result after restoring the declaration.
- Review the 19 English Part A answers in `Part-A-Answers.md` and adapt them into your own words. Lecture slides were unavailable; lecture-specific wording is identified as reconstructed.

Tasks 1–5 are implemented. Bonus Task 6 (notifications) is not implemented. No API 32/API 34 comparison is claimed. Git history is organized by task, with documentation/evidence in a separate commit.

## References

- https://developer.android.com/training/permissions/requesting
- https://developer.android.com/training/permissions/requesting-special
- https://developer.android.com/develop/sensors-and-location/location/permissions/runtime
- https://developer.android.com/develop/connectivity/network-ops/connecting
