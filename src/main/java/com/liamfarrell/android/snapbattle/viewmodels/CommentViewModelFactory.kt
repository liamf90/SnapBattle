package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.data.CommentRepository
import javax.inject.Inject


class CommentViewModelFactory @Inject constructor(private val commentRepository: CommentRepository,
                              private val battleID: Int
) : ViewModelProvider.NewInstanceFactory() {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CommentViewModel(commentRepository, battleID) as T
    }

}