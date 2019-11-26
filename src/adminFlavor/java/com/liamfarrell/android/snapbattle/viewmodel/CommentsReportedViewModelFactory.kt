package com.liamfarrell.android.snapbattle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.data.AllBattlesRepository
import com.liamfarrell.android.snapbattle.data.BattlesByNameDataSourceFactory
import com.liamfarrell.android.snapbattle.data.BattlesFromNameRepository
import com.liamfarrell.android.snapbattle.data.ReportingsRepository
import javax.inject.Inject

class CommentsReportedViewModelFactory @Inject constructor(private val reportingsRepository: ReportingsRepository
) : ViewModelProvider.NewInstanceFactory() {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CommentsReportedViewModel(reportingsRepository) as T
    }

}