package com.liamfarrell.android.snapbattle.di

import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.liamfarrell.android.snapbattle.data.*
import com.liamfarrell.android.snapbattle.data.following_battle_feed.FollowingBattlesFeedCacheManager
import com.liamfarrell.android.snapbattle.data.following_battle_feed.FollowingBattlesFeedDynamodbRepository
import com.liamfarrell.android.snapbattle.data.following_battle_feed.FollowingBattlesFeedRepository
import com.liamfarrell.android.snapbattle.db.*
import com.liamfarrell.android.snapbattle.db.following_battle_feed.FollowingBattlesFeedDatabase
import com.liamfarrell.android.snapbattle.db.following_battle_feed.FollowingBattlesFeedDynamoDataDao
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AWSLambdaModule::class, AmazonDynamoDBClientModule::class])
class FollowingBattlesFeedRepositoryModule(private val context: Context) {

    @Singleton
    @Provides
    fun followingBattlesFeedRepository(followingBattlesFeedCacheManager: FollowingBattlesFeedCacheManager, battleDao: BattleDao): FollowingBattlesFeedRepository {
        return FollowingBattlesFeedRepository(followingBattlesFeedCacheManager, battleDao)
    }

    @Singleton
    @Provides
    fun followingBattlesFeedCacheManager(followingBattlesDynamoDataDao : FollowingBattlesFeedDynamoDataDao, battleDao: BattleDao, followingBattlesFeedDynamodbRepository: FollowingBattlesFeedDynamodbRepository, battlesApi : BattlesRepository) : FollowingBattlesFeedCacheManager {
        return FollowingBattlesFeedCacheManager(followingBattlesDynamoDataDao,battleDao, followingBattlesFeedDynamodbRepository, battlesApi)
    }

    @Provides
    @Singleton
    fun battleDao() : BattleDao {
        return AllBattlesDatabase.getInstance(context.applicationContext).battlesDao()
    }

    @Provides
    @Singleton
    fun allBattlesDynamoDataDao()  : FollowingBattlesFeedDynamoDataDao {
        return FollowingBattlesFeedDatabase.getInstance(context.applicationContext).followingBattlesFeedDynamoDataDao()
    }

    @Provides
    @Singleton
    fun battlesApi(lambdaFunctionsInterface: LambdaFunctionsInterface) : BattlesRepository {
        return BattlesRepository(lambdaFunctionsInterface)
    }

    @Provides
    @Singleton
    fun followingBattlesFeedDynamoDBRepository(ddbClient: AmazonDynamoDBClient) : FollowingBattlesFeedDynamodbRepository {
        return FollowingBattlesFeedDynamodbRepository(context, ddbClient)
    }
}