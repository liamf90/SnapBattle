package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.data.AllBattlesRepository
import com.liamfarrell.android.snapbattle.data.CommentRepository
import javax.inject.Inject

class AllBattlesViewModelFactory @Inject constructor(private val allBattlesRepository: AllBattlesRepository
) : ViewModelProvider.NewInstanceFactory() {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AllBattlesViewModel(allBattlesRepository) as T
    }

}