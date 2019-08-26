package com.liamfarrell.android.snapbattle.db

import android.util.Log
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.AllBattlesCacheManager
import com.liamfarrell.android.snapbattle.data.NotificationsManager
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.notifications.Notification
import com.liamfarrell.android.snapbattle.notifications.NotificationDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This boundary callback gets notified when user reaches to the edges of the list for example when
 * the database cannot provide any more data.
 **/
class NotificationsBoundaryCallback(
        private val notificationsManager: NotificationsManager,
        private val coroutineScope: CoroutineScope
) : PagedList.BoundaryCallback<NotificationDb>() {


    override fun onZeroItemsLoaded() {
        Log.d("RepoBoundaryCallback", "onZeroItemsLoaded")
    }

    /**
     * When all items in the database were loaded, we need to query the backend for more items.
     */
    override fun onItemAtEndLoaded(itemAtEnd: NotificationDb) {
        Log.d("RepoBoundaryCallback", "onItemAtEndLoaded")
        coroutineScope.launch(Dispatchers.IO) { notificationsManager.requestMoreBattles()}
    }

}