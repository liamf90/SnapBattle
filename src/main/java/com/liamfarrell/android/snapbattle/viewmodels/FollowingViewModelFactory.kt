package com.liamfarrell.android.snapbattle.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.data.CommentRepository
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import javax.inject.Inject


class FollowingViewModelFactory @Inject constructor(private val followingRepository: FollowingRepository
) : ViewModelProvider.NewInstanceFactory() {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FollowingViewModel(followingRepository) as T
    }

}