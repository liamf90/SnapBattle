package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import com.liamfarrell.android.snapbattle.db.NotificationDao
import com.liamfarrell.android.snapbattle.db.NotificationsBoundaryCallback
import com.liamfarrell.android.snapbattle.model.NotificationsDatabaseResult
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
        private val notificationsManager: NotificationsManager,
        private val notificationsDao: NotificationDao)
{

    private val _networkErrors = MutableLiveData<String>()
    private val _isLoadingMoreBattles = notificationsManager.loadingMoreBattles
    private val _isNoMoreOlderBattles = notificationsManager.noMoreBattles

    // LiveData of network errors.
    val networkErrors: LiveData<String>
        get() = _networkErrors

    val isLoadingMoreBattles : LiveData<Boolean>
        get() = _isLoadingMoreBattles

    val isNoMoreOlderBattles = _isNoMoreOlderBattles




    fun loadAllNotifications(coroutineScope: CoroutineScope) : NotificationsDatabaseResult {

        // Get data source factory from the local cache
        val dataSourceFactory = notificationsDao.getAllNotifications()


        // every new query creates a new BoundaryCallback
        // The BoundaryCallback will observe when the user reaches to the edges of
        // the list and update the database with extra data
        val boundaryCallback = NotificationsBoundaryCallback (notificationsManager,coroutineScope)


        // Get the paged list
        val data = LivePagedListBuilder(dataSourceFactory, DATABASE_PAGE_SIZE)
                .setBoundaryCallback(boundaryCallback)
                .build()

        // Get the network errors exposed by the boundary callback
        return NotificationsDatabaseResult(data, networkErrors)
    }

    suspend fun updateSeenAllBattles(){
        notificationsManager.updateAllNotificationsHaveBeenSeen()
    }


    companion object {
        private const val DATABASE_PAGE_SIZE = 5
    }
}


