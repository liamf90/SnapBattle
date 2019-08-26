package com.liamfarrell.android.snapbattle.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liamfarrell.android.snapbattle.model.Battle

//Following Battle needs

@Entity(tableName = "following_battle")
data class FollowingBattle (
        @PrimaryKey @ColumnInfo(name = "battle_id_following") val id : Int,
        @Embedded
        val battle: Battle
)
