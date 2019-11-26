package com.liamfarrell.android.snapbattle.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/*
This class stores a single value indicating the total amount of notifications in the notifications feed for the user stored on the dynamo db server.
This value can be compared with updated notifications counts to get the correct indexes to load
 */
@Entity(tableName = "notifications_dynamo_info")
data class  NotificationsDynamoInfo(
        @PrimaryKey @ColumnInfo(name = "id") val id: Int = 1,
        val notifications_dynamo_count: Int = 0,
        val has_all_notifications_been_seen : Boolean = true
)
