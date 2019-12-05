package com.liamfarrell.android.snapbattle.model

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.notifications.Notification
import com.liamfarrell.android.snapbattle.notifications.NotificationDb

/**
 * BattlesSearchResult from a load database, which contains LiveData<List<Battle>> holding query data,
 * and a LiveData<String> of network error state.
 */
data class NotificationsDatabaseResult(
        val data: LiveData<PagedList<NotificationDb>>,
        val networkErrors: LiveData<Throwable>
)
