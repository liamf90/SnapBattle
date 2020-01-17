package com.liamfarrell.android.snapbattle.db

import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.NotificationsManager
import com.liamfarrell.android.snapbattle.notifications.NotificationDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * This boundary callback gets notified when user reaches to the edges of the list for example when
 * the database cannot provide any more data.
 **/
class NotificationsBoundaryCallback(
        private val notificationsManager: NotificationsManager,
        private val coroutineScope: CoroutineScope
) : PagedList.BoundaryCallback<NotificationDb>() {


    override fun onZeroItemsLoaded() {
        Timber.d("onZeroItemsLoaded")
    }

    /**
     * When all items in the database were loaded, we need to query the backend for more items.
     */
    override fun onItemAtEndLoaded(itemAtEnd: NotificationDb) {
        Timber.d("onItemAtEndLoaded")
        coroutineScope.launch(Dispatchers.IO) { notificationsManager.requestMoreBattles()}
    }

}