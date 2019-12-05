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
    fun getDynamoCount(): Int

    @Query("SELECT full_feed_update_bound FROM following_battles_info LIMIT 1")
    fun getLastFullFeedUpdateDynamoCount(): Int

    @Query("SELECT last_time_following_battles_updated FROM following_battles_info LIMIT 1")
    fun getLastTimeBattlesUpdated(): Date?

    @Query("UPDATE following_battles_info SET full_feed_update_bound = :fullFeedUpdateCount")
    fun updateFollowingBattlesFullFeedUpdatedCount(fullFeedUpdateCount: Int)

    @Query("UPDATE following_battles_info SET following_battles_dynamo_count = :followingBattlesDynamoCount")
    fun updateFollowingBattlesDynamoCount(followingBattlesDynamoCount: Int)

    @Query("UPDATE following_battles_info SET last_time_following_battles_updated = :lastTimeBattlesUpdated")
    fun updateLastTimeBattlesUpdated(lastTimeBattlesUpdated: Date)

    @Query("UPDATE following_battles_info SET full_feed_update_bound = 0 AND following_battles_dynamo_count = 0 " +
            "AND last_time_following_battles_updated = NULL")
    fun resetFollowingBattlesInfo()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(followingBattlesInfo : FollowingBattlesDynamoCount)


}