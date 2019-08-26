package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.NotificationsRepository
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.BattlesSearchResult
import com.liamfarrell.android.snapbattle.model.NotificationsDatabaseResult
import com.liamfarrell.android.snapbattle.notifications.Notification
import com.liamfarrell.android.snapbattle.notifications.NotificationDb
import javax.inject.Inject


/**
 * The ViewModel used in [BattleChallengesListFragment].
 */
class NotificationsViewModel @Inject constructor(private val notificationsRepository: NotificationsRepository) : ViewModelLaunch() {

    private val notificationsResult = MutableLiveData<NotificationsDatabaseResult>()
    private val _noMoreOlderBattles =  notificationsRepository.isNoMoreOlderBattles
    private val _loadingMoreBattles = notificationsRepository.isLoadingMoreBattles

    val notifications: LiveData<PagedList<NotificationDb>> = Transformations.switchMap(notificationsResult) { it -> it.data }
    val networkErrors: LiveData<String> = Transformations.switchMap(notificationsResult) { it ->
        it.networkErrors
    }
    val noMoreOlderBattles : LiveData<Boolean> = _noMoreOlderBattles
    val isLoadingMoreBattles : LiveData<Boolean> = _loadingMoreBattles


    init {
        notificationsResult.value = notificationsRepository.loadAllNotifications(viewModelScope)
    }

    fun updateSeenAllBattles(){
        awsLambdaFunctionCall(false,
                suspend {notificationsRepository.updateSeenAllBattles()})

    }





}