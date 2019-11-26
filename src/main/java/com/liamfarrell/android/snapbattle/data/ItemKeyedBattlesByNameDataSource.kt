package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import com.liamfarrell.android.snapbattle.model.Battle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

/**
 * A data source that uses the "id" field of battle as the key for next/prev pages.
 */


class ItemKeyedBattlesByNameDataSource @Inject constructor( val battlesFromNameRepository: BattlesFromNameRepository,
                                                            val thumbnailSignedUrlCacheRepository: ThumbnailSignedUrlCacheRepository, val battleName: String, val viewModelScope: CoroutineScope) : ItemKeyedDataSource<Int, Battle>() {

     val errors = MutableLiveData<Exception>()
    val spinner = MutableLiveData(false)


    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Battle>) {
        //Ignored
    }

    override fun loadInitial(params: LoadInitialParams<Int>,
                             callback: LoadInitialCallback<Battle>) {

        viewModelScope.launch {
            spinner.value = true
            val response = battlesFromNameRepository.getBattlesFromName(battleName)
            if (response.error == null) {
                response.result.sqlResult = getThumbnailSignedUrls(response.result.sqlResult)
                spinner.value = false
                callback.onResult(response.result.sqlResult)
            }
            else {
                spinner.value = false
                errors.value = response.error
            }

        }
    }

    override fun loadAfter(params: LoadParams<Int>,
                           callback: LoadCallback<Battle>) {
        viewModelScope.launch {
            val response = battlesFromNameRepository.getBattlesFromName(battleName, params.key)
            if (response.error == null) {
                response.result.sqlResult = getThumbnailSignedUrls(response.result.sqlResult)
                callback.onResult(response.result.sqlResult)
            }
            else{
                errors.value = response.error
            }
        }
    }

    private suspend fun getThumbnailSignedUrls(battleList: List<Battle>) : List<Battle>{
        battleList.forEach {
            it.signedThumbnailUrl = thumbnailSignedUrlCacheRepository.getThumbnailSignedUrl(it)
        }
        return battleList
    }


    override fun getKey(item: Battle): Int {
        return item.battleID
    }
}