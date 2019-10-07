package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.notifications.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationItemViewModel @Inject constructor(val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository, cognitoID: String, notification: Notification, signedUrlNew: String) {
    private val _notification = MutableLiveData<Notification>()
    val notification : LiveData<Notification> = _notification

    lateinit var job: Job



    init {
        _notification.value = notification
        job = GlobalScope.launch (Dispatchers.Main){
            //get profile pic from the cache if it exists
            val lastSavedSignedUrlEntry = otherUsersProfilePicUrlRepository.getUserSignedUrlAndProfilePicCount(cognitoID)
            lastSavedSignedUrlEntry?.let {
                notification.signedUrlProfilePicOpponent = it.last_saved_signed_url
                notification.opponentProfilePicCount = it.profile_pic_count
                _notification.value = notification
            }

        }

    }



    fun cancelJob(){
        job.cancel()
    }


}