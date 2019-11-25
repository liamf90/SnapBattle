package com.liamfarrell.android.snapbattle.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.User


/**
 * The Data Access Object for the [User] class.
 */
@Dao
interface FollowingUserDao {

    @Query("SELECT * FROM user")
    suspend fun getAllFollowingUsers(): List<User>

    @Query("UPDATE User SET mUsername = :username WHERE mCognitoId = :cognitoId")
    suspend fun updateUsername(cognitoId: String, username: String)

    @Query("UPDATE User SET mFacebookName = :name WHERE mCognitoId = :cognitoId")
    suspend fun updateName(cognitoId: String, name: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>)

    @Query("DELETE FROM user WHERE mCognitoId = :userCognitoID")
    suspend fun deleteFromUser(userCognitoID: String)

    @Query("SELECT * FROM user WHERE LOWER(mUsername) LIKE LOWER(:searchQuery) || '%' OR LOWER(mFacebookName) LIKE LOWER(:searchQuery) || '%' OR LOWER(mFacebookName) LIKE '% ' || LOWER(:searchQuery) || '%'")
    suspend fun searchUsersInCache(searchQuery: String): List<User>

}