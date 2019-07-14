package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.liamfarrell.android.snapbattle.model.Battle
import kotlinx.coroutines.CoroutineScope
import java.lang.Exception
import java.util.concurrent.Executor
import javax.inject.Inject

/**
 * A simple data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
 */
class BattlesByNameDataSourceFactory (
        private val battlesRepository: BattlesFromNameRepository,
        private val battleName: String,
        private val coroutineScope: CoroutineScope) : DataSource.Factory<Int, Battle>() {

    val sourceLiveData = MutableLiveData<ItemKeyedBattlesByNameDataSource>()
    var errorLiveData = MutableLiveData<Exception>()

    override fun create(): DataSource<Int, Battle> {
        val source = ItemKeyedBattlesByNameDataSource(battlesRepository, battleName, coroutineScope)
        sourceLiveData.postValue(source)
        errorLiveData = source._errors
        return source
    }
}