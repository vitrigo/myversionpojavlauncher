package pixelmon

import android.content.Context
import android.util.Log
import com.kdt.mcgui.mcVersionSpinner
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles

class Pixelmon(
    private val context: Context,
    private val versionSpinner: mcVersionSpinner,
    popStack: () -> Boolean
) {
    companion object {
        @JvmStatic
        private val TAG = "Pixelmon.kt"
        fun launchGame() {
            Log.w(TAG, "Start the game")
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
        }
    }
    fun start() {
        LauncherPreferences.loadPreferences(context)
        val profile = LauncherProfiles.getCurrentProfile()
        Log.w(
            TAG, """
            The current profile have
            gameDIr: ${profile.gameDir}
            name : ${profile.name}
            last used: ${profile.lastUsed}
        """.trimIndent()
        )
//        val modDownloader = Downloader(context)
//        modDownloader.downloadModOneDotSixteen()
//        modDownloader.downloadModsOneDotTwelve(exclude = listOf("Pixelmon"))
//        val forgerDownload = ForgerDownload(context)
//        forgerDownload.run()
    }
}