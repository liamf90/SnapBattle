/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liamfarrell.android.snapbattle.di



import android.app.Application
import androidx.room.Room
import com.liamfarrell.android.snapbattle.db.*
import com.liamfarrell.android.snapbattle.di.ViewModelModule
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {



    @Singleton
    @Provides
    fun provideDb(app: Application): SnapBattleDatabase {
        return Room
            .databaseBuilder(app, SnapBattleDatabase::class.java, "snapbattle.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideOtherUsersProfilePicUrlDao(db: SnapBattleDatabase): OtherUsersProfilePicUrlDao {
        return db.otherUsersProfilePicCacheDao()
    }

    @Singleton
    @Provides
    fun provideThumbnailSignedUrlDao(db: SnapBattleDatabase): ThumbnailSignedUrlDao {
        return db.thumbnailSignedUrlDao()
    }

    @Singleton
    @Provides
    fun provideBattleDao(db: SnapBattleDatabase): BattleDao {
        return db.battlesDao()
    }

    @Singleton
    @Provides
    fun provideAllBattlesDynamoDataDao(db: SnapBattleDatabase): AllBattlesDynamoDataDao {
        return db.allBattlesDynamoDataDao()
    }

    @Singleton
    @Provides
    fun provideNotificationDao(db: SnapBattleDatabase): NotificationDao {
        return db.notificationsDao()
    }

    @Singleton
    @Provides
    fun provideNotificationDynamoInfoDao(db: SnapBattleDatabase): NotificationDynamoInfoDao {
        return db.notificationsDynamoCountDao()
    }

    @Singleton
    @Provides
    fun provideFollowingUserDao(db: SnapBattleDatabase): FollowingUserDao {
        return db.followingUserDao()
    }

    @Singleton
    @Provides
    fun provideFollowingUserDynamoDataDao(db: SnapBattleDatabase): FollowingUserDynamoDataDao {
        return db.followingUserDynamoCountDao()
    }

    @Singleton
    @Provides
    fun provideFollowingBattlesFeedDynamoDataDao(db: SnapBattleDatabase): FollowingBattlesFeedDynamoDataDao {
        return db.followingBattlesFeedDynamoDataDao()
    }

    @Singleton
    @Provides
    fun provideFollowingBattleDao(db: SnapBattleDatabase): FollowingBattleDao {
        return db.followingBattlesDao()
    }

}
