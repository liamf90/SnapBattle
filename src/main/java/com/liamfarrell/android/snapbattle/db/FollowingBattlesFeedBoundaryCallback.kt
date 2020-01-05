package com.liamfarrell.android.snapbattle.db

import androidx.paging.PagedList
import android.util.Log
import com.liamfarrell.android.snapbattle.data.FollowingBattlesFeedCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This boundary callback gets notified when user reaches to the edges of the list for example when
 * the database cannot provide any more data.
 **/
class FollowingBattlesFeedBoundaryCallback(
        private val followingBattlesFeedCacheManager: FollowingBattlesFeedCacheManager,
        private val coroutineScope: CoroutineScope
) : PagedList.BoundaryCallback<FollowingBattle>() {


    override fun onZeroItemsLoaded() {
        Log.d("RepoBoundaryCallback", "onZeroItemsLoaded")
    }

    /**
     * When all items in the database were loaded, we need to query the backend for more items.
     */
    override fun onItemAtEndLoaded(itemAtEnd: FollowingBattle) {
        Log.d("RepoBoundaryCallback", "onItemAtEndLoaded")
        coroutineScope.launch(Dispatchers.IO) { followingBattlesFeedCacheManager.requestMoreBattles()}
    }

}