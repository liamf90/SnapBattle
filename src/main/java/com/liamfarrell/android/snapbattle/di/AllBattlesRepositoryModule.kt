package com.liamfarrell.android.snapbattle.di

import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.liamfarrell.android.snapbattle.data.AllBattlesCacheManager
import com.liamfarrell.android.snapbattle.data.AllBattlesFeedDynamodbRepository
import com.liamfarrell.android.snapbattle.data.AllBattlesRepository
import com.liamfarrell.android.snapbattle.data.BattlesRepository
import com.liamfarrell.android.snapbattle.db.AllBattlesDatabase
import com.liamfarrell.android.snapbattle.db.AllBattlesDynamoDataDao
import com.liamfarrell.android.snapbattle.db.BattleDao
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AWSLambdaModule::class, AmazonDynamoDBClientModule::class])
class AllBattlesRepositoryModule(private val context: Context) {

    @Singleton
    @Provides
    fun allBattlesRepository(allBattlesCacheManager: AllBattlesCacheManager, battleDao: BattleDao): AllBattlesRepository {
        return AllBattlesRepository(allBattlesCacheManager, battleDao)
    }

    @Singleton
    @Provides
    fun allBattlesCacheManager(allBattlesDynamoInfoDao : AllBattlesDynamoDataDao, battleDao: BattleDao, allBattlesDynamoRepository : AllBattlesFeedDynamodbRepository, battlesApi : BattlesRepository) : AllBattlesCacheManager {
        return AllBattlesCacheManager(allBattlesDynamoInfoDao,battleDao, allBattlesDynamoRepository, battlesApi)
    }

    @Provides
    @Singleton
    fun battleDao() : BattleDao {
        return AllBattlesDatabase.getInstance(context.applicationContext).battlesDao()
    }

    @Provides
    @Singleton
    fun allBattlesDynamoData()  : AllBattlesDynamoDataDao{
        return AllBattlesDatabase.getInstance(context.applicationContext).allBattlesDynamoDataDao()
    }

    @Provides
    @Singleton
    fun battlesApi(lambdaFunctionsInterface: LambdaFunctionsInterface) : BattlesRepository {
        return BattlesRepository(lambdaFunctionsInterface)
    }

    @Provides
    @Singleton
    fun allBattlesFeedDynamoDBRepository(ddbClient: AmazonDynamoDBClient) : AllBattlesFeedDynamodbRepository{
        return AllBattlesFeedDynamodbRepository(ddbClient)
    }


}




