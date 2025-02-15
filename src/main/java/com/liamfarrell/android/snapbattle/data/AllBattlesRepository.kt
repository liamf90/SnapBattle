package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import com.liamfarrell.android.snapbattle.db.AllBattlesBoundaryCallback
import com.liamfarrell.android.snapbattle.db.BattleDao
import com.liamfarrell.android.snapbattle.model.BattlesSearchResult
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AllBattlesRepository @Inject constructor(
        private val allBattlesCacheManager : AllBattlesCacheManager,
        private val battleDao: BattleDao)
{

    private val _networkErrors = MutableLiveData<String>()
    private val _isLoadingMoreBattles = allBattlesCacheManager.loadingMoreBattles
    private val _isNoMoreOlderBattles = allBattlesCacheManager.noMoreBattles

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
        val boundaryCallback = AllBattlesBoundaryCallback (allBattlesCacheManager,coroutineScope)


        // Get the paged list
        val data = LivePagedListBuilder(dataSourceFactory, DATABASE_PAGE_SIZE)
                .setBoundaryCallback(boundaryCallback)
                .build()

        // Get the network errors exposed by the boundary callback
        return BattlesSearchResult(data, networkErrors)
    }

    suspend fun updateBattles(){
        allBattlesCacheManager.checkForUpdates()
    }


    companion object {
        private const val DATABASE_PAGE_SIZE = 5
    }




}





