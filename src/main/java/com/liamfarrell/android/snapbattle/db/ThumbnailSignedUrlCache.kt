package com.liamfarrell.android.snapbattle.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "thumbnail_signed_url")
data class ThumbnailSignedUrlCache(
        @PrimaryKey @ColumnInfo(name = "battle_id")
        val battle_id: Int,
        val last_saved_signed_url : String
)
