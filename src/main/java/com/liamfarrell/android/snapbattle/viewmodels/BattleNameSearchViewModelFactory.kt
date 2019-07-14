package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.data.BattleNameSearchRepository
import com.liamfarrell.android.snapbattle.data.BattlesFromNameRepository
import javax.inject.Inject

class BattleNameSearchViewModelFactory @Inject constructor(private val battleNameSearchRepository: BattleNameSearchRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BattleNameSearchViewModel(battleNameSearchRepository) as T
    }

}