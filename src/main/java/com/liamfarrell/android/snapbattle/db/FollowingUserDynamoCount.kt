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
class  FollowingUserDynamoCount(val following_user_update_dynamo_count: Int = 0) {
    //Since we only want 1 row to store the following_user_update_dynamo_count, the primary key id field can only be set to 1
    @PrimaryKey
    @ColumnInfo(name = "id")
     var id = 1
        set(_) {field = 1}
}
