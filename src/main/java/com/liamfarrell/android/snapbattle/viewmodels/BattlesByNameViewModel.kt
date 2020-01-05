package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.BattlesByNameDataSourceFactory
import com.liamfarrell.android.snapbattle.data.BattlesFromNameRepository
import com.liamfarrell.android.snapbattle.data.ThumbnailSignedUrlCacheRepository
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import java.lang.Exception
import javax.inject.Inject

/**
 * The ViewModel used in [BattlesByNameFragment].
 */
class BattlesByNameViewModel @Inject constructor(private val context: Application, private val battlesRepository: BattlesFromNameRepository,
                                                 thumbnailSignedUrlCacheRepository: ThumbnailSignedUrlCacheRepository) : ViewModelBase() {

    private val _battleName = MutableLiveData<String>()
    val battleName : LiveData<String> = _battleName

    private var _errorLiveData = MediatorLiveData<Exception>()
    private var errorLiveData : LiveData<Exception> = _errorLiveData

    private var _spinnerLiveData = MediatorLiveData<Boolean>()
    val spinnerLiveData : LiveData<Boolean> = _spinnerLiveData



    val battlesList: LiveData<PagedList<Battle>> = Transformations.switchMap(_battleName){ battleName->
        if (battleName == null){
            null
        } else {
            val dataSourceFactory = BattlesByNameDataSourceFactory(battlesRepository, thumbnailSignedUrlCacheRepository,  battleName, viewModelScope)
            _spinnerLiveData.addSource(dataSourceFactory.spinnerLiveData){_spinnerLiveData.value = it}
            _errorLiveData.addSource(dataSourceFactory.errorLiveData){_errorLiveData.value = it}
            LivePagedListBuilder<Int, Battle>(dataSourceFactory, config).build()
        }
    }


    val errorMessage : LiveData<String> = Transformations.map(errorLiveData){
        getErrorMessage(context, it)
    }

    fun setBattleName( battleName: String){
        _battleName.value = battleName
    }



    companion object{
          private val PAGE_SIZE = 10
        private val INITIAL_LOAD_SIZE_HINT = 25

        private val config = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(INITIAL_LOAD_SIZE_HINT)
                .setPageSize(PAGE_SIZE)
                .build()

    }


}