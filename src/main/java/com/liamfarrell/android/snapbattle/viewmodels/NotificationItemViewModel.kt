package com.liamfarrell.android.snapbattle.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.notifications.Notification
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers.io
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationItemViewModel @Inject constructor(val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository, cognitoID: String, notification: Notification, signedUrlNew: String) {
    private val _notification = MutableLiveData<Notification>()
    val notification : LiveData<Notification> = _notification

    private val disposable : Disposable


    init {
            _notification.value = notification

            //get profile pic from the cache if it exists
            val lastSavedSignedUrlEntry = otherUsersProfilePicUrlRepository.getUserSignedUrlAndProfilePicCountRx(cognitoID)

             disposable = lastSavedSignedUrlEntry.subscribeOn(io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        it?.let {
                            notification.signedUrlProfilePicOpponent = it.last_saved_signed_url
                            notification.opponentProfilePicCount = it.profile_pic_count
                            _notification.value = notification
                        }
                    }
    }




    fun cancelJob(){
        disposable.dispose()
    }


}