package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

/**
 * A data source that uses the "id" field of battle as the key for next/prev pages.
 */


class ItemKeyedBattlesByNameDataSource @Inject constructor( val battlesFromNameRepository: BattlesFromNameRepository, val battleName: String, val viewModelScope: CoroutineScope) : ItemKeyedDataSource<Int, Battle>() {

     val _errors = MutableLiveData<Exception>()

//    val errors : LiveData<Exception>
//        get() = _errors

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Battle>) {
        //Ignored
    }

    override fun loadInitial(params: LoadInitialParams<Int>,
                             callback: LoadInitialCallback<Battle>) {

        viewModelScope.launch {
            val response = battlesFromNameRepository.getBattlesFromName(battleName)
            if (response.error == null)
                callback.onResult(response.result.sqlResult)
            else{
                _errors.value = response.error
            }

        }
    }

    override fun loadAfter(params: LoadParams<Int>,
                           callback: LoadCallback<Battle>) {
        viewModelScope.launch {
            val response = battlesFromNameRepository.getBattlesFromName(battleName, params.key)
            if (response.error == null)
                callback.onResult(response.result.sqlResult)
            else{
                _errors.value = response.error
            }
        }
    }

    override fun getKey(item: Battle): Int {
        return item.battleID
    }
}