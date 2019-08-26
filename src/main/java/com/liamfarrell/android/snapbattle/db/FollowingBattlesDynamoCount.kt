package com.liamfarrell.android.snapbattle.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


/*
This class stores a single value indicating the total amount of topBattles in the all topBattles feed stored on the server.
This value can be compared with updated battle counts to get the correct indexes to load
 */
@Entity(tableName = "following_battles_info")
data class FollowingBattlesDynamoCount(
        @PrimaryKey @ColumnInfo(name = "id") val id: Int = 1,
        val following_battles_dynamo_count: Int = 0,
        val full_feed_update_bound: Int = 0,
        val last_time_following_battles_updated: Date? = null
)
