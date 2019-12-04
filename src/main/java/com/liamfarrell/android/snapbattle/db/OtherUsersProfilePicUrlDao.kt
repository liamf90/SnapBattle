package com.liamfarrell.android.snapbattle.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Maybe

/**
 * The Data Access Object for the [OtherUsersProfilePicUrlCache] class.
 */
@Dao
interface OtherUsersProfilePicUrlDao {

    @Query("SELECT * FROM other_users_profile_pic_signed_url WHERE cognito_id = :cognitoId")
    suspend fun getSignedUrlAndProfilePicForUser(cognitoId: String): OtherUsersProfilePicUrlCache?


    @Query("SELECT * FROM other_users_profile_pic_signed_url WHERE cognito_id = :cognitoId")
    fun getSignedUrlAndProfilePicForUserRx(cognitoId: String): Maybe<OtherUsersProfilePicUrlCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignedUrl(signedUrl: OtherUsersProfilePicUrlCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSignedUrlRx(signedUrl: OtherUsersProfilePicUrlCache)

    @Query("DELETE FROM other_users_profile_pic_signed_url")
    suspend fun deleteAllProfilePicSignedUrls()
}