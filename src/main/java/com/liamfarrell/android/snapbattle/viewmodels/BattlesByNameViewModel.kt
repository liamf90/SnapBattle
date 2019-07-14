package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.app.App
import com.liamfarrell.android.snapbattle.data.BattlesByNameDataSourceFactory
import com.liamfarrell.android.snapbattle.data.BattlesFromNameRepository
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.BattlesSearchResult
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject

/**
 * The ViewModel used in [BattlesByNameFragment].
 */
class BattlesByNameViewModel @Inject constructor(  private val battlesRepository: BattlesFromNameRepository,
                                                   private val battleName: String ) : ViewModelLaunch() {



    val dataSourceFactory = BattlesByNameDataSourceFactory(battlesRepository, battleName, viewModelScope)
    val battlesList = LivePagedListBuilder<Int, Battle>(dataSourceFactory, config).build()

    val errorMessage : LiveData<String> = Transformations.map(dataSourceFactory.errorLiveData){
        getErrorMessage(App.getContext(), it)
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