package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.NotificationsRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.NotificationsDatabaseResult
import com.liamfarrell.android.snapbattle.notifications.NotificationDb
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * The ViewModel used in [NotificationsListFragment].
 */
class NotificationsViewModel @Inject constructor(private val notificationsRepository: NotificationsRepository, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelBase() {

    private val notificationsResult = MutableLiveData<NotificationsDatabaseResult>()
    private val _noMoreOlderNotifications =  notificationsRepository.isNoMoreOlderNotifications
    private val _loadingMoreNotifications = notificationsRepository.isLoadingMoreNotifications

    private val cogntioIdSignedUrlServerCheckList = mutableListOf<String>()

    val notifications: LiveData<PagedList<NotificationDb>> = Transformations.switchMap(notificationsResult) {
        //getProfilePics()
        it.data }
    val networkErrors: LiveData<String> = Transformations.switchMap(notificationsResult) { it ->
        it.networkErrors
    }
    val noMoreOlderNotifications : LiveData<Boolean> = _noMoreOlderNotifications
    val isLoadingMoreNotifications : LiveData<Boolean> = _loadingMoreNotifications
    val isNoNotifications = Transformations.map(_noMoreOlderNotifications) {
        it && notifications.value?.size == 0
    }

    init {
        _spinner.value = true
        notificationsResult.value = notificationsRepository.loadAllNotifications(viewModelScope)
        _spinner.value = false
        viewModelScope.launch {notificationsRepository.checkForUpdates()}
    }

    fun updateSeenAllBattles(){
        awsLambdaFunctionCall(false,
                suspend {notificationsRepository.updateSeenAllBattles()})

    }

    fun getProfilePic(cognitoId: String){
        if (!cogntioIdSignedUrlServerCheckList.contains(cognitoId)){
            cogntioIdSignedUrlServerCheckList.add(cognitoId)
            viewModelScope.launch {
                val response = otherUsersProfilePicUrlRepository.getSignedUrlsFromServer(listOf(cognitoId))
                if (response.error == null){
                    val updatedSignedUrl  =  response.result.newSignedUrls[0]
                    //Insert into database
                    otherUsersProfilePicUrlRepository.insertOtherUsersProfilePicOnlyIfProfilePicCountDifferent(updatedSignedUrl.cognitoId, updatedSignedUrl.profilePicCount, updatedSignedUrl.newSignedUrl)
                }
            }
        }
    }







}