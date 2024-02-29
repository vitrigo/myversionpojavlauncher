package pixelmon

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.util.Log
import net.kdt.pojavlaunch.fragments.MainMenuFragment
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import java.io.File

class MinecraftAssets(val context: Context) {
    companion object {
        val TAG = "MinecraftAssets.kt"
    }
    fun moveFiles(directory: String) {
        if (context?.assets != null) {
            val assets = context?.assets!!
            assets.list(directory)?.let {
                try {
                    for (dir in it) {
                        if (dir.isFile() || directory.isObjectsDir()) {
                            putFilesInData(assets = assets, name =  directory + "/" + dir)
                        } else {
                            val newDirectory = "$directory/$dir"
                            File(context?.getExternalFilesDir(null), "." + newDirectory).mkdir()
                            moveFiles(newDirectory)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        LauncherPreferences.DEFAULT_PREF.edit().putBoolean("first_installation", false).commit()
        Log.w(TAG, "the value of first_installation is ${LauncherPreferences.PREF_FIRST_INSTALLATION}")
    }

    /** Object is a directory containing the assets need to run minecraft
     */
    fun String.isObjectsDir(): Boolean {
        return this.takeLast(2).matches(Regex("([a-f]|\\d){2}"))
    }
    fun String.isFile(): Boolean {
        return Regex("\\.([A-Za-z])+").containsMatchIn(this)
    }

    private fun putFilesInData(name: String, assets: AssetManager) {
        val outFile = File(context?.getExternalFilesDir(null), "." + name)
        Log.d(MainMenuFragment.TAG, "Attempting to write to: " + outFile.absolutePath)
        val copiedFile = assets.open(name)
        outFile.writeBytes(copiedFile.readBytes())
        copiedFile.close()
    }
}