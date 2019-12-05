package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import com.liamfarrell.android.snapbattle.db.NotificationDao
import com.liamfarrell.android.snapbattle.db.NotificationsBoundaryCallback
import com.liamfarrell.android.snapbattle.model.NotificationsDatabaseResult
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
        private val notificationsManager: NotificationsManager,
        private val notificationsDao: NotificationDao)
{

    private val _networkErrors = MutableLiveData<Throwable>()
    private val _isLoadingMoreNotifications = notificationsManager.loadingMoreNotifications
    private val _isNoMoreOlderNotifications = notificationsManager.noMoreNotifications

    // LiveData of network errors.
    val networkErrors: LiveData<Throwable>
        get() = _networkErrors

    val isLoadingMoreNotifications : LiveData<Boolean>
        get() = _isLoadingMoreNotifications

    val isNoMoreOlderNotifications = _isNoMoreOlderNotifications




    fun loadAllNotifications(compositeDisposable: CompositeDisposable) : NotificationsDatabaseResult {

        // Get data source factory from the local cache
        val dataSourceFactory = notificationsDao.getAllNotificationsWithSignedUrls()


        // every new query creates a new BoundaryCallback
        // The BoundaryCallback will observe when the user reaches to the edges of
        // the list and update the database with extra data
        val boundaryCallback = NotificationsBoundaryCallback (notificationsManager,compositeDisposable, _networkErrors)


        // Get the paged list
        val data = LivePagedListBuilder(dataSourceFactory, DATABASE_PAGE_SIZE)
                .setBoundaryCallback(boundaryCallback)
                .build()

        // Get the network errors exposed by the boundary callback
        return NotificationsDatabaseResult(data, networkErrors)
    }

     fun checkForUpdates() : Completable {
       return Completable.fromCallable { notificationsManager.checkForUpdates()}
    }


     fun updateSeenAllBattles() : Completable{
        return Completable.fromCallable {notificationsManager.updateAllNotificationsHaveBeenSeen()}
    }


    companion object {
        private const val DATABASE_PAGE_SIZE = 5
    }
}


