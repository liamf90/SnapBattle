package com.liamfarrell.android.snapbattle.data.following_battle_feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import com.liamfarrell.android.snapbattle.db.following_battle_feed.FollowingBattlesFeedBoundaryCallback
import com.liamfarrell.android.snapbattle.db.BattleDao
import com.liamfarrell.android.snapbattle.model.BattlesSearchResult
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowingBattlesFeedRepository @Inject constructor(
        private val followingBattlesFeedCacheManager: FollowingBattlesFeedCacheManager,
        private val battleDao: BattleDao)
{

    private val _networkErrors = MutableLiveData<String>()
    private val _isLoadingMoreBattles = followingBattlesFeedCacheManager.loadingMoreBattles
    private val _isNoMoreOlderBattles = followingBattlesFeedCacheManager.noMoreBattles

    // LiveData of network errors.
    val networkErrors: LiveData<String>
        get() = _networkErrors

    val isLoadingMoreBattles : LiveData<Boolean>
        get() = _isLoadingMoreBattles

    val isNoMoreOlderBattles = _isNoMoreOlderBattles




      fun loadAllBattles(coroutineScope: CoroutineScope) : BattlesSearchResult {

        // Get data source factory from the local cache
        val dataSourceFactory = battleDao.getAllBattles()

        // every new query creates a new BoundaryCallback
        // The BoundaryCallback will observe when the user reaches to the edges of
        // the list and update the database with extra data
        val boundaryCallback = FollowingBattlesFeedBoundaryCallback (followingBattlesFeedCacheManager,coroutineScope)


        // Get the paged list
        val data = LivePagedListBuilder(dataSourceFactory, DATABASE_PAGE_SIZE)
                .setBoundaryCallback(boundaryCallback)
                .build()

        // Get the network errors exposed by the boundary callback
        return BattlesSearchResult(data, networkErrors)
    }

    suspend fun updateBattles(){
        followingBattlesFeedCacheManager.checkForUpdates()
    }


    companion object {
        private const val DATABASE_PAGE_SIZE = 5
    }




}





