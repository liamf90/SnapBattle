package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.NotificationsRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.db.OtherUsersProfilePicUrlCache
import com.liamfarrell.android.snapbattle.model.NotificationsDatabaseResult
import com.liamfarrell.android.snapbattle.notifications.NotificationDb
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * The ViewModel used in [NotificationsListFragment].
 */
class NotificationsViewModel @Inject constructor(private val notificationsRepository: NotificationsRepository, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelLaunch() {

    private val notificationsResult = MutableLiveData<NotificationsDatabaseResult>()
    private val _noMoreOlderBattles =  notificationsRepository.isNoMoreOlderBattles
    private val _loadingMoreBattles = notificationsRepository.isLoadingMoreBattles

    private val cogntioIdSignedUrlServerCheckList = mutableListOf<String>()

    val notifications: LiveData<PagedList<NotificationDb>> = Transformations.switchMap(notificationsResult) {
        //getProfilePics()
        it.data }
    val networkErrors: LiveData<String> = Transformations.switchMap(notificationsResult) { it ->
        it.networkErrors
    }
    val noMoreOlderBattles : LiveData<Boolean> = _noMoreOlderBattles
    val isLoadingMoreBattles : LiveData<Boolean> = _loadingMoreBattles


    init {

        notificationsResult.value = notificationsRepository.loadAllNotifications(viewModelScope)
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