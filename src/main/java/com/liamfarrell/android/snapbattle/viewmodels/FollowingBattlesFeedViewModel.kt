package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.*
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.FollowingBattlesFeedRepository
import com.liamfarrell.android.snapbattle.data.ThumbnailSignedUrlCacheRepository
import com.liamfarrell.android.snapbattle.db.FollowingBattle
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.FollowingBattlesSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


/**
 * The ViewModel used in [FollowingBattlesFeedFragment].
 */
class FollowingBattlesFeedViewModel @Inject constructor(private val followingBattlesFeedRepository: FollowingBattlesFeedRepository, val thumbnailSignedUrlCacheRepository: ThumbnailSignedUrlCacheRepository) : ViewModelLaunch() {

    private val allBattlesResult = MutableLiveData<FollowingBattlesSearchResult>()
    private val _noMoreOlderBattles =  followingBattlesFeedRepository.isNoMoreOlderBattles
    private val _loadingMoreBattles = followingBattlesFeedRepository.isLoadingMoreBattles

    val battles: LiveData<PagedList<FollowingBattle>> = Transformations.switchMap(allBattlesResult) { it -> it.data }
    val networkErrors: LiveData<String> = Transformations.switchMap(allBattlesResult) { it ->
        it.networkErrors
    }

    val noMoreOlderBattles : LiveData<Boolean> = _noMoreOlderBattles
    val isLoadingMoreBattles : LiveData<Boolean> = _loadingMoreBattles
    val isNoBattlesInFeed = Transformations.map(_noMoreOlderBattles) {
        it && battles.value?.size == 0
    }


    val battleIdsOfThumbnailsLoadedList = mutableListOf<Int>()



    init {
        allBattlesResult.value = followingBattlesFeedRepository.loadAllBattles(viewModelScope)
        updateFollowingBattlesRepeating()
    }

    private fun updateFollowingBattlesRepeating() {
        viewModelScope.launch (Dispatchers.IO){
            while(true) {
                followingBattlesFeedRepository.updateBattles()
                delay(HOW_OFTEN_CHECK_FOR_UPDATES_MILLISECONDS)
            }
        }
    }

    fun updateFollowingBattles(){
       awsLambdaFunctionCall(true){
           withContext(Dispatchers.IO) {
                followingBattlesFeedRepository.updateBattles()
        }
    }}

    fun loadThumbnail(b: Battle){
        if (battleIdsOfThumbnailsLoadedList.contains(b.battleId)) return

        viewModelScope.launch {
            //thumbnail signed url repository will update the signed url for the thumbnail in the room db if needed (not still in picasso cache, not loaded yet etc)
            //this change will be reflected in the left join of the LiveData of the getAllBattles Dao call and the thumbnail will be auto updated
            battleIdsOfThumbnailsLoadedList.add(b.battleID)
            thumbnailSignedUrlCacheRepository.getThumbnailSignedUrl(b)
        }
    }

    companion object{
        val HOW_OFTEN_CHECK_FOR_UPDATES_MILLISECONDS : Long = 60000; // EVERY 60 SECONDS
    }


}