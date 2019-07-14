package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.*
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.following_battle_feed.FollowingBattlesFeedRepository
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.BattlesSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * The ViewModel used in [FollowingBattlesFeedFragment].
 */
class FollowingBattlesFeedViewModel(val followingBattlesFeedRepository: FollowingBattlesFeedRepository) : ViewModelLaunch() {

    private val allBattlesResult = MutableLiveData<BattlesSearchResult>()
    private val _noMoreOlderBattles =  followingBattlesFeedRepository.isNoMoreOlderBattles
    private val _loadingMoreBattles = followingBattlesFeedRepository.isLoadingMoreBattles

    val battles: LiveData<PagedList<Battle>> = Transformations.switchMap(allBattlesResult) { it -> it.data }
    val networkErrors: LiveData<String> = Transformations.switchMap(allBattlesResult) { it ->
        it.networkErrors
    }
    val noMoreOlderBattles : LiveData<Boolean> = _noMoreOlderBattles
    val isLoadingMoreBattles : LiveData<Boolean> = _loadingMoreBattles




    init {
        allBattlesResult.value = followingBattlesFeedRepository.loadAllBattles(viewModelScope)
        updateAllBattlesRepeating()
    }

    private fun updateAllBattlesRepeating() {
        viewModelScope.launch (Dispatchers.IO){
            while(true) {
                followingBattlesFeedRepository.updateBattles()
                delay(HOW_OFTEN_CHECK_FOR_UPDATES_MILLISECONDS)
            }
        }

    }

    companion object{
        val HOW_OFTEN_CHECK_FOR_UPDATES_MILLISECONDS : Long = 60000; // EVERY 60 SECONDS
    }


}