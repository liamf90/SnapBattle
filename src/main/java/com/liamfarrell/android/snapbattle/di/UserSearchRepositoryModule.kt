package com.liamfarrell.android.snapbattle.di

import android.content.Context
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.liamfarrell.android.snapbattle.data.FollowingUserCacheManager
import com.liamfarrell.android.snapbattle.data.UserFollowingDynamodbRepository
import com.liamfarrell.android.snapbattle.data.UserFollowingRepository
import com.liamfarrell.android.snapbattle.db.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [RepositoryModule::class, AmazonDynamoDBClientModule::class])
class UserSearchRepositoryModule(val context: Context) {

    @Provides
    fun getFollowingUserCacheManager(followingDynamodbRepository: UserFollowingDynamodbRepository, userDao: FollowingUserDao,
                                     followingUserFollowingRepository: UserFollowingRepository, followingUserDynamoDataDao: FollowingUserDynamoDataDao ) : FollowingUserCacheManager {
        return FollowingUserCacheManager(followingDynamodbRepository, userDao, followingUserFollowingRepository, followingUserDynamoDataDao)
    }

    @Provides
    @Singleton
    fun userDao() : FollowingUserDao {
        return FollowingUserCacheDatabase.getInstance(context.applicationContext).followingUserDao()
    }

    @Provides
    @Singleton
    fun userFollowingDynamodbRepository(ddbClient : AmazonDynamoDBClient) : UserFollowingDynamodbRepository {
        return UserFollowingDynamodbRepository(ddbClient)
    }

    @Provides
    @Singleton
    fun getFollowingUserDynamoDataDao() : FollowingUserDynamoDataDao {
        return FollowingUserCacheDatabase.getInstance(context.applicationContext).followingUserDynamoCountDao()
    }





}