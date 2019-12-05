package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.NotificationsRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.NotificationsDatabaseResult
import com.liamfarrell.android.snapbattle.notifications.NotificationDb
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.schedulers.Schedulers.single
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * The ViewModel used in [NotificationsListFragment].
 */
class NotificationsViewModel @Inject constructor(private val context: Application, private val notificationsRepository: NotificationsRepository, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelLaunch() {

    private val notificationsResult = MutableLiveData<NotificationsDatabaseResult>()
    private val _noMoreOlderNotifications =  notificationsRepository.isNoMoreOlderNotifications
    private val _loadingMoreNotifications = notificationsRepository.isLoadingMoreNotifications

    private val cogntioIdSignedUrlServerCheckList = mutableListOf<String>()

    val notifications: LiveData<PagedList<NotificationDb>> = Transformations.switchMap(notificationsResult) {
        it.data }
    private val networkErrors: LiveData<Throwable> = Transformations.switchMap(notificationsResult) { it ->
        it.networkErrors
    }

    val errorMessage : LiveData<String> = Transformations.map(networkErrors){
        it.printStackTrace()
        it?.let{ getErrorMessage(context, it) }
    }

    val noMoreOlderNotifications : LiveData<Boolean> = _noMoreOlderNotifications
    val isLoadingMoreNotifications : LiveData<Boolean> = _loadingMoreNotifications
    val isNoNotifications = Transformations.map(_noMoreOlderNotifications) {
        it && notifications.value?.size == 0
    }

    init {
        _spinner.value = true
        notificationsResult.value = notificationsRepository.loadAllNotifications(compositeDisposable)
        _spinner.value = false
        checkForUpdates()
    }



    private fun checkForUpdates(){
        compositeDisposable.add( notificationsRepository.checkForUpdates()
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { //onComplete
                        },
                        {onError : Throwable ->
                            _snackBarMessage.value = getErrorMessage(context, onError)
                        }
                ))

    }

    fun updateSeenAllBattles(){
        compositeDisposable.add( notificationsRepository.updateSeenAllBattles()
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { //onComplete
                        },
                        {onError : Throwable ->
                            _snackBarMessage.value = getErrorMessage(context, onError)
                        }
                ))
    }

    fun getProfilePic(cognitoId: String){
        if (!cogntioIdSignedUrlServerCheckList.contains(cognitoId)){
            cogntioIdSignedUrlServerCheckList.add(cognitoId)

                val responseSingle = otherUsersProfilePicUrlRepository.getSignedUrlsFromServerRx(listOf(cognitoId))
                compositeDisposable.add(
                        responseSingle.subscribeOn(io())
                        .observeOn(single())
                        .subscribe(
                                { onSuccessResponse ->
                                    val updatedSignedUrl  =  onSuccessResponse.newSignedUrls[0]
                                    //Insert into database
                                    otherUsersProfilePicUrlRepository.insertOtherUsersProfilePicOnlyIfProfilePicCountDifferentRx(updatedSignedUrl.cognitoId, updatedSignedUrl.profilePicCount, updatedSignedUrl.newSignedUrl)
                                },
                                {onError : Throwable ->
                                    onError.printStackTrace()
                                }
                        ))

        }
    }







}