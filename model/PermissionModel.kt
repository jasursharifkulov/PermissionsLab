// Standalone Kotlin Playground exercise. This is a simplified denial model,
// not an Android permission-state detector.
class PermissionModel {
    var granted: Boolean = false
        private set
    val rationale: Boolean
        get() = !granted && denials == 1
    var dialogShows: Boolean = false
        private set
    private var denials = 0

    fun request(userAllows: Boolean) {
        dialogShows = false
        when {
            granted -> println("already granted; no dialog")
            denials >= 2 -> println("no dialog, denied immediately")
            else -> {
                dialogShows = true
                if (userAllows) granted = true else denials++
                println(if (userAllows) "dialog shown, allowed" else "dialog shown, denied")
            }
        }
    }
}

fun main() {
    val permission = PermissionModel()
    val actions = listOf(
        "1. deny" to false,
        "2. deny again" to false,
        "3. request after two denials" to false,
        "4. try to allow after two denials" to true
    )
    for ((label, answer) in actions) {
        println(label)
        permission.request(answer)
        println("granted=${permission.granted}, rationale=${permission.rationale}, dialogShows=${permission.dialogShows}")
        println()
    }
    // The last true is ignored: without a dialog the user cannot grant access.
    // On Android, the user can change a blocked permission in app Settings.
}
