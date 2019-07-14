package com.liamfarrell.android.snapbattle.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


/*
This class stores a single value indicating the total amount of battles in the all battles feed stored on the server.
This value can be compared with updated battle counts to get the correct indexes to load
 */
@Entity(tableName = "all_battles_info")
data class  AllBattlesDynamoCount(
        @PrimaryKey @ColumnInfo(name = "id") val id: Int = 1,
        val all_battles_dynamo_count: Int = 0,
        val last_time_battle_updated: Date? = null
)
