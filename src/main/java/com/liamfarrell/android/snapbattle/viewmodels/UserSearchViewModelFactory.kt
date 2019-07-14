package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.data.BattleNameSearchRepository
import com.liamfarrell.android.snapbattle.data.BattlesFromNameRepository
import com.liamfarrell.android.snapbattle.data.FollowingUserCacheManager
import com.liamfarrell.android.snapbattle.data.UserSearchRepository
import javax.inject.Inject

class UserSearchViewModelFactory @Inject constructor(private val userSearchRepository: UserSearchRepository, private val followingUserCacheManager: FollowingUserCacheManager) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserSearchViewModel(userSearchRepository, followingUserCacheManager) as T
    }

}