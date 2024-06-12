package net.kdt.pojavlaunch

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.kdt.mcgui.ProgressLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import pixelmon.Loading
import pixelmon.MinecraftAssets
import pixelmon.PixelmonProfile
import pixelmon.Tools.Timberly
import pixelmon.download.Downloader
import timber.log.Timber

class LauncherViewModel(
    private val context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * Is not possible to modify inside the class because here I don't have a observe for call the
     * function setLoadingState
     */
    val loadingState = MutableLiveData<Loading>()
    val bottomButtonsVisible = MutableLiveData<Boolean>()
    val callPixelmonLoading = MutableLiveData(false)
    val downloadedOneDotSixteen = MutableLiveData(LauncherPreferences.DOWNLOAD_ONE_DOT_SIXTEEN)

    val mDownloader by lazy {
        MutableLiveData(Downloader(context, this))
    }
    private val loadingStateObserver = Observer<Loading> { newLoading ->
        this.setLoadingState(newLoading)
    }
    private val downloadedOneDotSixteenObserver = Observer<Boolean> { downloaded ->
        LauncherPreferences.loadPreferences(context)
    }

    init {
        loadingState.observeForever(loadingStateObserver)
        downloadedOneDotSixteen.observeForever(downloadedOneDotSixteenObserver)
    }

    fun setLoadingState(loading: Loading) {
        when (loading) {
            Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE -> {
                Timber.tag(Timberly.downloadProblem).d("start the download of mods 1.12")
                ProgressLayout.setProgress(
                    ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE,
                    0,
                    Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE.messageLoading
                )
                val downloadScope = CoroutineScope(Dispatchers.IO)
                downloadScope.launch {
                    // espera ate que o download dos mods seja completo
                    // para que a próxima parte do código começe a rodar
                    mDownloader.value?.downloadModsOneDotTwelve()?.join()
                    LauncherPreferences.DEFAULT_PREF.edit()
                        .putBoolean("download_mod_one_dot_twelve", true).commit()
                    Timber.tag("downloadProblem").d("finish the download of mods 1.12")
                    withContext(Dispatchers.Main) {
                        loadingState.value = Loading.DOWNLOAD_TEXTURE
                    }
                }
                return
            }

            Loading.MOVING_FILES -> {
                Timber.tag(Timberly.downloadProblem).d("start the moving of files")
                LauncherPreferences.DEFAULT_PREF.edit().putBoolean("get_one_dot_twelve", true)
                    .commit()

                ProgressLayout.setProgress(
                    ProgressLayout.MOVING_FILES,
                    0,
                    Loading.MOVING_FILES.messageLoading
                )
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        MinecraftAssets(context, this@LauncherViewModel).moveImportantAssets()
                    }
                }
                return
            }

            Loading.DOWNLOAD_MOD_ONE_DOT_SIXTEEN -> TODO()
            Loading.DOWNLOAD_ONE_DOT_SIXTEEN -> {

            }
            Loading.SHOW_PLAY_BUTTON -> {
                ExtraCore.setValue(ExtraConstants.SHOW_PLAY_BUTTON, true)
            }
            Loading.DOWNLOAD_TEXTURE -> {
                CoroutineScope(Dispatchers.Default).launch {
                    mDownloader.value?.downloadTexture()?.await()
                    LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_texture", true)
                        .commit()
                }
                return
            }
        }

    }

    fun changeProfile(ctx: Context, pixelmonProfile: PixelmonProfile) {
        Log.w(TAG, "select the correct option")
        LauncherPreferences.DEFAULT_PREF.edit()
            .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, pixelmonProfile.key)
            .commit()
        LauncherPreferences.loadPreferences(ctx)
        val profile = LauncherPreferences.DEFAULT_PREF.getString(
            LauncherPreferences.PREF_KEY_CURRENT_PROFILE,
            ""
        )
        Log.w(TAG, "The current profile is $profile")
        LauncherProfiles.load()
    }

     fun setupPixelmonLoading() {
        if (callPixelmonLoading.value == false) {
            val getOneDotTwelve = LauncherPreferences.DOWNLOAD_MOD_ONE_DOT_TWELVE
            Timber.d("the value of getOneDotTwelve is " + getOneDotTwelve)
            if (getOneDotTwelve) {
                ExtraCore.setValue(ExtraConstants.SHOW_PLAY_BUTTON, true)
            } else {
                this.loadingState.value = Loading.MOVING_FILES
            }
            callPixelmonLoading.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        loadingState.removeObserver(loadingStateObserver)
    }
    companion object {
        private const val TAG = "LauncherActivityViewModel"
        fun provideFactory(
            context: Context,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return LauncherViewModel(context, handle) as T
                }
            }
    }
}
