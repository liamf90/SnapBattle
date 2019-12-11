package com.liamfarrell.android.snapbattle.db

import androidx.room.*
import com.liamfarrell.android.snapbattle.model.Battle

//Following Battle

@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "following_battle")
data class FollowingBattle (
        @PrimaryKey @ColumnInfo(name = "battle_id_following") val id : Int,
        @Embedded
        val battle: Battle,
        @ColumnInfo(name="last_saved_signed_url") val lastSavedSignedUrl : String? = null
)
