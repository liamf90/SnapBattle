package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import com.liamfarrell.android.snapbattle.db.FollowingBattlesFeedBoundaryCallback
import com.liamfarrell.android.snapbattle.db.BattleDao
import com.liamfarrell.android.snapbattle.db.FollowingBattleDao
import com.liamfarrell.android.snapbattle.model.BattlesSearchResult
import com.liamfarrell.android.snapbattle.model.FollowingBattlesSearchResult
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowingBattlesFeedRepository @Inject constructor(
        private val followingBattlesFeedCacheManager: FollowingBattlesFeedCacheManager,
        private val followingBattleDao: FollowingBattleDao)
{

    private val _networkErrors = MutableLiveData<Throwable>()
    private val _isLoadingMoreBattles = followingBattlesFeedCacheManager.loadingMoreBattles
    private val _isNoMoreOlderBattles = followingBattlesFeedCacheManager.noMoreBattles

    // LiveData of network errors.
    val networkErrors: LiveData<Throwable>
        get() = _networkErrors

    val isLoadingMoreBattles : LiveData<Boolean>
        get() = _isLoadingMoreBattles

    val isNoMoreOlderBattles = _isNoMoreOlderBattles




      fun loadAllBattles(compositeDisposable: CompositeDisposable) : FollowingBattlesSearchResult {

        // Get data source factory from the local cache
        val dataSourceFactory = followingBattleDao.getAllBattles()

        // every new query creates a new BoundaryCallback
        // The BoundaryCallback will observe when the user reaches to the edges of
        // the list and update the database with extra data
        val boundaryCallback = FollowingBattlesFeedBoundaryCallback(followingBattlesFeedCacheManager, compositeDisposable,
                _networkErrors )


        // Get the paged list
        val data = LivePagedListBuilder(dataSourceFactory, DATABASE_PAGE_SIZE)
                .setBoundaryCallback(boundaryCallback)
                .build()

        // Get the network errors exposed by the boundary callback
        return FollowingBattlesSearchResult(data, networkErrors)
    }

     fun updateBattles() : Completable {
         return Completable.fromCallable {followingBattlesFeedCacheManager.checkForUpdates()}
    }


    companion object {
        private const val DATABASE_PAGE_SIZE = 5
    }




}





