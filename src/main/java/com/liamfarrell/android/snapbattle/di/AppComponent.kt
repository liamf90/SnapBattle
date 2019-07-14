package com.liamfarrell.android.snapbattle.di

import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.liamfarrell.android.snapbattle.data.AllBattlesRepository
import com.liamfarrell.android.snapbattle.data.CommentRepository
import com.liamfarrell.android.snapbattle.data.UsersBattleRepository
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.viewmodels.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RepositoryModule::class, AWSLambdaModule::class, CommentViewModelFactoryModule::class])
interface AppComponent {
    fun getCommentViewModelFactory() : CommentViewModelFactory
    fun getCognitoIDCachingProvider() : CognitoCachingCredentialsProvider
}


@Singleton
@Component(modules = [RepositoryModule::class, AWSLambdaModule::class, FollowingViewModelFactoryModule::class])
interface FollowingComponent {
    fun getFollowingViewModelFactory() : FollowingViewModelFactory
    fun getCognitoIDCachingProvider() : CognitoCachingCredentialsProvider
}

@Singleton
@Component(modules = [RepositoryModule::class, AWSLambdaModule::class, CompletedBattlesViewModelFactoryMoodule::class])
interface CompletedBattlesComponent {
    fun getCompletedBattlesViewModelFactory() : CompletedBattlesViewModelFactory
}

@Singleton
@Component(modules = [RepositoryModule::class, AWSLambdaModule::class, CurrentBattlesViewModelFactoryMoodule::class])
interface CurrentBattlesComponent {
    fun getCurrentBattlesViewModelFactory() : CurrentBattlesViewModelFactory
}

@Singleton
@Component(modules = [RepositoryModule::class, AWSLambdaModule::class, FollowFacebookFriendViewModelFactoryModule::class])
interface FacebookFollowingComponent {
    fun getFollowFacebookFriendsViewModelFactory() : FollowFacebookFriendsViewModelFactory
    fun getCognitoIDCachingProvider() : CognitoCachingCredentialsProvider
}

@Singleton
@Component(modules = [AllBattlesRepositoryModule::class, AWSLambdaModule::class, AllBattlesViewModelFactoryModule::class])
interface AllBattlesComponent {
    fun getAllBattlesViewModelFactory() : AllBattlesViewModelFactory
}

@Singleton
@Component(modules = [FollowingBattlesFeedRepositoryModule::class, AWSLambdaModule::class, FollowingBattlesFeedViewModelFactoryModule::class])
interface FollowingBattlesFeedComponent {
    fun getFollowingBattlesFeedViewModelFactory() : FollowingBattlesFeedViewModelFactory
}

@Singleton
@Component(modules = [RepositoryModule::class, AWSLambdaModule::class, BattlesByNameViewModelFactoryModule::class])
interface BattlesByNameComponent {
    fun getBattlesByNameViewModelFactory() : BattlesByNameViewModelFactory
}


@Singleton
@Component(modules = [RepositoryModule::class, AWSLambdaModule::class, ViewOwnBattleViewModelFactoryModule::class])
interface OwnBattleComponent {
    fun getViewOwnBattleViewModelFactory() : ViewOwnBattleViewModelFactory
    fun getUsersBattleRepository() : UsersBattleRepository
}

@Singleton
@Component(modules = [RepositoryModule::class, AWSLambdaModule::class, BattleNameSearchViewModelFactoryModule::class])
interface BattleNameSearchComponent {
    fun getBattleNameSearchViewModelFactory() : BattleNameSearchViewModelFactory
}

@Singleton
@Component(modules = [RepositoryModule::class, AWSLambdaModule::class, UserSearchViewModelFactoryModule::class])
interface UserSearchComponent {
    fun getUserSearchViewModelFactory() : UserSearchViewModelFactory
}



