package com.liamfarrell.android.snapbattle.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "other_users_profile_pic_signed_url")
data class OtherUsersProfilePicUrlCache(
        @PrimaryKey @ColumnInfo(name = "cognito_id")
        val cognito_id: String,
        val profile_pic_count : Int,
        val last_saved_signed_url : String
)
