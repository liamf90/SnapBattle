package com.liamfarrell.android.snapbattle.db

import androidx.room.ColumnInfo
import androidx.room.Embedded

//Since last_saved_signed_url is retrieved from the Room Database in a LEFT JOIN call, this class is created to hold that query
data class FollowingBattle (
        @Embedded
        val followingBattleDb: FollowingBattleDb,
        @ColumnInfo(name="last_saved_signed_url") val lastSavedSignedUrl : String? = null
)
