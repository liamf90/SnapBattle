package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import com.liamfarrell.android.snapbattle.model.Battle
import kotlinx.coroutines.CoroutineScope

/**
 * A simple data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
 */
class BattlesByNameDataSourceFactory (
        private val battlesRepository: BattlesFromNameRepository,
        private val thumbnailSignedUrlCacheRepository: ThumbnailSignedUrlCacheRepository,
        private val battleName: String,
        private val coroutineScope: CoroutineScope) : DataSource.Factory<Int, Battle>() {

    val sourceLiveData = MutableLiveData<ItemKeyedBattlesByNameDataSource>()
    var errorLiveData : LiveData<Exception> = Transformations.switchMap(sourceLiveData){it.errors}
    val spinnerLiveData : LiveData<Boolean> = Transformations.switchMap(sourceLiveData){it.spinner}

    override fun create(): DataSource<Int, Battle> {
        val source = ItemKeyedBattlesByNameDataSource(battlesRepository, thumbnailSignedUrlCacheRepository,  battleName, coroutineScope)
        sourceLiveData.postValue(source)
        return source
    }
}