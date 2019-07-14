package com.liamfarrell.android.snapbattle.di

import com.liamfarrell.android.snapbattle.data.*
import com.liamfarrell.android.snapbattle.db.BattleDao
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AWSLambdaModule::class])
class RepositoryModule {

    @Singleton
    @Provides
    fun commentRepository(lambdaFunctionsInterface: LambdaFunctionsInterface) : CommentRepository{
        return CommentRepository(lambdaFunctionsInterface)
    }

    @Singleton
    @Provides
    fun followingRepository(lambdaFunctionsInterface: LambdaFunctionsInterface) : FollowingRepository{
        return FollowingRepository(lambdaFunctionsInterface)
    }

    @Singleton
    @Provides
    fun completedBattlesRepository(lambdaFunctionsInterface: LambdaFunctionsInterface) : CompletedBattlesRepository{
        return CompletedBattlesRepository(lambdaFunctionsInterface)
    }

    @Singleton
    @Provides
    fun currentBattlesRepository(lambdaFunctionsInterface: LambdaFunctionsInterface) : CurrentBattlesRepository{
        return CurrentBattlesRepository(lambdaFunctionsInterface)
    }

    @Singleton
    @Provides
    fun battlesByNameRepository(lambdaFunctionsInterface: LambdaFunctionsInterface) : BattlesFromNameRepository{
        return BattlesFromNameRepository(lambdaFunctionsInterface)
    }

    @Singleton
    @Provides
    fun usersBattlesRepository(lambdaFunctionsInterface: LambdaFunctionsInterface) : UsersBattleRepository{
        return UsersBattleRepository(lambdaFunctionsInterface)
    }

    @Singleton
    @Provides
    fun battleNameSearchRepository(lambdaFunctionsInterface: LambdaFunctionsInterface) : BattleNameSearchRepository{
        return BattleNameSearchRepository(lambdaFunctionsInterface)
    }

    @Singleton
    @Provides
    fun userSearchRepository(lambdaFunctionsInterface: LambdaFunctionsInterface) : UserSearchRepository{
        return UserSearchRepository(lambdaFunctionsInterface)
    }



}