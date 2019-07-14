package com.liamfarrell.android.snapbattle.db

import androidx.paging.PagedList
import android.util.Log
import com.liamfarrell.android.snapbattle.data.AllBattlesCacheManager
import com.liamfarrell.android.snapbattle.model.Battle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This boundary callback gets notified when user reaches to the edges of the list for example when
 * the database cannot provide any more data.
 **/
class AllBattlesBoundaryCallback(
        private val allBattlesCacheManager : AllBattlesCacheManager,
         private val coroutineScope: CoroutineScope
) : PagedList.BoundaryCallback<Battle>() {


    override fun onZeroItemsLoaded() {
        Log.d("RepoBoundaryCallback", "onZeroItemsLoaded")
    }

    /**
     * When all items in the database were loaded, we need to query the backend for more items.
     */
    override fun onItemAtEndLoaded(itemAtEnd: Battle) {
        Log.d("RepoBoundaryCallback", "onItemAtEndLoaded")
        coroutineScope.launch(Dispatchers.IO) { allBattlesCacheManager.requestMoreBattles()}
    }

}