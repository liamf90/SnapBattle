package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.data.following_battle_feed.FollowingBattlesFeedRepository
import javax.inject.Inject

class FollowingBattlesFeedViewModelFactory @Inject constructor(private val followingBattlesFeedRepository: FollowingBattlesFeedRepository
) : ViewModelProvider.NewInstanceFactory() {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FollowingBattlesFeedViewModel(followingBattlesFeedRepository) as T
    }

}