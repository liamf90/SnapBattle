package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.data.CurrentBattlesRepository
import javax.inject.Inject

class CurrentBattlesViewModelFactory @Inject constructor(private val battleRepository: CurrentBattlesRepository

) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CurrentBattlesViewModel(battleRepository) as T
    }
}