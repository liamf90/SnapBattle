package com.liamfarrell.android.snapbattle.di

import com.liamfarrell.android.snapbattle.data.CommentRepository
import com.liamfarrell.android.snapbattle.viewmodels.CommentViewModelFactory
import dagger.Module
import dagger.Provides

@Module(includes = [RepositoryModule::class])
class CommentViewModelFactoryModule(private val battleID: Int)  {

    @Provides
    fun commentViewModelFactory( commentRepository: CommentRepository) : CommentViewModelFactory{
        return CommentViewModelFactory(commentRepository, battleID)
    }

}