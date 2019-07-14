package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.data.CompletedBattlesRepository
import javax.inject.Inject

class CompletedBattlesViewModelFactory @Inject constructor(private val battleRepository: CompletedBattlesRepository

) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CompletedBattlesViewModel(battleRepository) as T
    }
}