package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.data.UsersBattleRepository
import javax.inject.Inject

class ViewOwnBattleViewModelFactory @Inject constructor(private val battleID: Int, private val usersBattleRepository: UsersBattleRepository

) : ViewModelProvider.NewInstanceFactory() {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ViewOwnBattleViewModel(battleID, usersBattleRepository) as T
    }

}