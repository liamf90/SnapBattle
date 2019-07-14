package com.liamfarrell.android.snapbattle.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/*
This class stores a single value indicating the total amount of ADD/REMOVE updates to following users on the dynamo db server.
This value can be compared with updated following counts to get the amount of updates to load
 */
@Entity(tableName = "following_users_dynamo_info")
data class  FollowingUserDynamoCount(
        @PrimaryKey @ColumnInfo(name = "id") val id: Int = 1,
        val following_user_update_dynamo_count: Int = 0
)
