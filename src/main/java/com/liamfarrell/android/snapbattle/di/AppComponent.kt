package com.liamfarrell.android.snapbattle.di

import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.liamfarrell.android.snapbattle.data.CommentRepository
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.viewmodels.CommentViewModelFactory
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RepositoryModule::class, AWSLambdaModule::class, CommentViewModelFactoryModule::class])
interface AppComponent {
    fun getCommentViewModelFactory() : CommentViewModelFactory
    fun getCognitoIDCachingProvider() : CognitoCachingCredentialsProvider
}

