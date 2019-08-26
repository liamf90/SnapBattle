package com.liamfarrell.android.snapbattle.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liamfarrell.android.snapbattle.model.Battle

/**
 * The Data Access Object for the [ThumbnailSignedUrlCache] class.
 */
@Dao
interface ThumbnailSignedUrlDao {

    @Query("SELECT last_saved_signed_url FROM thumbnail_signed_url WHERE battle_id = :battleID")
    suspend fun getLastSavedThumbnailSignedUrl(battleID: Int): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignedUrl(signedUrl: ThumbnailSignedUrlCache)

    @Query("DELETE FROM other_users_profile_pic_signed_url")
    suspend fun deleteAllProfilePicSignedUrls()
}