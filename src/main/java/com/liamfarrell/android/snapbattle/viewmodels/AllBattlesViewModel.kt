package com.liamfarrell.android.snapbattle.viewmodels


import androidx.lifecycle.*
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.AllBattlesRepository
import com.liamfarrell.android.snapbattle.data.ThumbnailSignedUrlCacheRepository
import com.liamfarrell.android.snapbattle.db.AllBattlesBattle
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.BattlesSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer


/**
 * The ViewModel used in [AllBattlesFragment].
 */
class AllBattlesViewModel @Inject constructor(private val allBattlesRepository: AllBattlesRepository, val thumbnailSignedUrlCacheRepository: ThumbnailSignedUrlCacheRepository) : ViewModelLaunch() {

    private val allBattlesResult = MutableLiveData<BattlesSearchResult>()
    private val _noMoreOlderBattles =  allBattlesRepository.isNoMoreOlderBattles
    private val _loadingMoreBattles = allBattlesRepository.isLoadingMoreBattles

    val battles: LiveData<PagedList<AllBattlesBattle>> = Transformations.switchMap(allBattlesResult) { it -> it.data }
    val networkErrors: LiveData<String> = Transformations.switchMap(allBattlesResult) { it ->
        it.networkErrors
    }


    val noMoreOlderBattles : LiveData<Boolean> = _noMoreOlderBattles
    val isLoadingMoreBattles : LiveData<Boolean> = _loadingMoreBattles

    val battleIdsOfThumbnailsLoadedList = mutableListOf<Int>()




    init {
        allBattlesResult.value = allBattlesRepository.loadAllBattles(viewModelScope)
        updateAllBattlesRepeating()
    }

    private fun updateAllBattlesRepeating() {
        viewModelScope.launch (Dispatchers.IO){
            while(true) {
                allBattlesRepository.updateBattles()
                delay(HOW_OFTEN_CHECK_FOR_UPDATES_MILLISECONDS)
            }
        }
    }

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