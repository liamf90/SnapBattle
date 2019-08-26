package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.BattlesByNameDataSourceFactory
import com.liamfarrell.android.snapbattle.data.BattlesFromNameRepository
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import java.lang.Exception
import javax.inject.Inject

/**
 * The ViewModel used in [BattlesByNameFragment].
 */
class BattlesByNameViewModel @Inject constructor(private val context: Application, private val battlesRepository: BattlesFromNameRepository) : ViewModelLaunch() {

    private val _battleName = MutableLiveData<String>()

    private var errorLiveData = MutableLiveData<Exception>()

    //private val dataSourceFactory = BattlesByNameDataSourceFactory(battlesRepository, battleName, viewModelScope)
    //val battlesList = LivePagedListBuilder<Int, Battle>(dataSourceFactory, config).build()

    val battlesList: LiveData<PagedList<Battle>> = Transformations.switchMap(_battleName){ battleName->
        if (battleName == null){
            null
        } else {
            val dataSourceFactory = BattlesByNameDataSourceFactory(battlesRepository, battleName, viewModelScope)
            errorLiveData = dataSourceFactory.errorLiveData
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