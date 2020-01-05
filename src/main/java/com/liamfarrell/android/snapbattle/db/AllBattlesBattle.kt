package com.liamfarrell.android.snapbattle.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liamfarrell.android.snapbattle.model.Battle

data class AllBattlesBattle (
        @Embedded
        val battle: Battle,
        @ColumnInfo(name="last_saved_signed_url") val lastSavedSignedUrl : String? = null
)
