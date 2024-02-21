package pixelmon

import android.content.Context
import android.nfc.Tag
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.runBlocking
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import kotlin.concurrent.thread

class Pixelmon(private val context: Context, popStack: () -> Boolean) {
    companion object {
        @JvmStatic
        private val TAG = "Pixelmon.kt"
    }

    private val forgerInstaller = ForgerInstaller(context, popStack = popStack)
    fun start() {
        Log.i(TAG, "start forge installation")
        forgerInstaller.install()
        Log.i(TAG, "select the correct option")
        LauncherPreferences.DEFAULT_PREF.edit()
            .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "0")
            .apply()
        Log.i(TAG, "Start the game")
        ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
    }
}