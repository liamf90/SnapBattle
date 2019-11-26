package com.liamfarrell.android.snapbattle.viewmodels

import android.content.Context
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.amazonaws.mobile.auth.core.IdentityManager
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.UsersBattleRepository
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Video
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class BattleVideoItemViewModel(val context: Context, val battle: Battle, _video: Video, val usersBattleRepository: UsersBattleRepository) : ViewModel(){

    private val _error  = MutableLiveData<Exception>()
    val errorMessage : LiveData<String> = Transformations.map(_error) { exception ->
        getErrorMessage(context, exception)
    }

    val video = ObservableField<Video>(_video)
    val isCurrentUser = ObservableField<Boolean>(_video.isCurrentUser(IdentityManager.getDefaultIdentityManager().cachedUserID))
    val videosUploaded = ObservableInt(battle.videosUploaded)
    val videoStatus = ObservableField<String>(getVideoStatus(battle.videosUploaded, _video, context))
    val displayRecordButton = ObservableField<Boolean>(_video.displayRecordButton(battle.videosUploaded, IdentityManager.getDefaultIdentityManager().cachedUserID))
    val displayPlayButton = ObservableField<Boolean>(_video.displayPlayButton(context))
    val displaySubmitButton = ObservableField<Boolean>(_video.displaySubmitButton(context, battle.videosUploaded, IdentityManager.getDefaultIdentityManager().cachedUserID))
    val nameText = ObservableField<String>(videoStatus.get())
    val submitButtonEnabled = ObservableField<Boolean>(true)


    suspend fun uploadVideo() {
        withContext(Dispatchers.Main) {
            nameText.set(context.resources.getString(R.string.uploading))
            submitButtonEnabled.set(false)

            var orientationLock: String? = null
            val videosUploaded = battle.videosUploaded

            val videoNLastUploaded = videosUploaded?.let { battle.videos?.get(videosUploaded) }
            if (battle.videosUploaded == 0) {
                orientationLock = Video.orientationHintToLock(Integer.parseInt(Video.getVideoRotation(context, videoNLastUploaded)))
            }

            val asyncResult = video.get()?.let {
                usersBattleRepository.uploadVideo(context, battle, it.videoFilename,
                        battle.getOpponentCognitoID(IdentityManager.getDefaultIdentityManager().cachedUserID), it.videoID, orientationLock)
            }
            if (asyncResult?.error != null) {
                _error.value = asyncResult.error
            }
        }

    }


     fun getVideoStatus(videosUploaded: Int, video: Video, context: Context) : String {
        return when (video.getVideoStatus(videosUploaded, IdentityManager.getDefaultIdentityManager().cachedUserID)) {
            Video.videoStatus.RECEIVED -> context.resources.getString(R.string.received, video.getTimeSinceUploaded(context))
            Video.videoStatus.SENT -> context.resources.getString(R.string.sent, video.getTimeSinceUploaded(context))
            Video.videoStatus.YOUR_TURN -> context.resources.getString(R.string.your_turn)
            Video.videoStatus.OPPONENT_TURN -> context.resources.getString(R.string.opponent_turn)
            Video.videoStatus.OPPONENT_FUTURE -> ""
            Video.videoStatus.YOUR_FUTURE -> ""
            Video.videoStatus.ERROR -> context.resources.getString(R.string.error)
            else -> context.resources.getString(R.string.error) }
    }

}