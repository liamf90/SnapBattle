package com.liamfarrell.android.snapbattle.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * The Data Access Object for the [FollowingUserDynamoCount] class.
 */
@Dao
interface FollowingUserDynamoDataDao {
    @Query("SELECT following_user_update_dynamo_count FROM following_users_dynamo_info LIMIT 1")
    suspend fun getDynamoCount(): Int

    @Query("UPDATE following_users_dynamo_info SET following_user_update_dynamo_count = :followingUserCount")
    suspend fun updateFollowingUserDynamoCount(followingUserCount: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(followingUserDynamoInfo : FollowingUserDynamoCount)
}