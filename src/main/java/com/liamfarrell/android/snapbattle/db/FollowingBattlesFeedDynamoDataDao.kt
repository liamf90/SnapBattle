package com.liamfarrell.android.snapbattle.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*


/**
 * The Data Access Object for the [FollowingBattlesDynamoCount] class.
 */
@Dao
interface FollowingBattlesFeedDynamoDataDao {
    @Query("SELECT following_battles_dynamo_count FROM following_battles_info LIMIT 1")
    suspend fun getDynamoCount(): Int

    @Query("SELECT full_feed_update_bound FROM following_battles_info LIMIT 1")
    suspend fun getLastFullFeedUpdateDynamoCount(): Int

    @Query("SELECT last_time_following_battles_updated FROM following_battles_info LIMIT 1")
    suspend fun getLastTimeBattlesUpdated(): Date?

    @Query("UPDATE following_battles_info SET full_feed_update_bound = :fullFeedUpdateCount")
    suspend fun updateFollowingBattlesFullFeedUpdatedCount(fullFeedUpdateCount: Int)

    @Query("UPDATE following_battles_info SET following_battles_dynamo_count = :followingBattlesDynamoCount")
    suspend fun updateFollowingBattlesDynamoCount(followingBattlesDynamoCount: Int)

    @Query("UPDATE following_battles_info SET last_time_following_battles_updated = :lastTimeBattlesUpdated")
    suspend fun updateLastTimeBattlesUpdated(lastTimeBattlesUpdated: Date)

    @Query("UPDATE following_battles_info SET full_feed_update_bound = 0 AND following_battles_dynamo_count = 0 " +
            "AND last_time_following_battles_updated = NULL")
    suspend fun resetFollowingBattlesInfo()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(followingBattlesInfo : FollowingBattlesDynamoCount)


}