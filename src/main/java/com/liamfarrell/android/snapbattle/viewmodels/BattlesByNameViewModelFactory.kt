package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.data.AllBattlesRepository
import com.liamfarrell.android.snapbattle.data.BattlesByNameDataSourceFactory
import com.liamfarrell.android.snapbattle.data.BattlesFromNameRepository
import javax.inject.Inject

class BattlesByNameViewModelFactory @Inject constructor(private val battlesRepository: BattlesFromNameRepository,
                                                        private val battleName: String
) : ViewModelProvider.NewInstanceFactory() {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BattlesByNameViewModel(battlesRepository, battleName) as T
    }

}