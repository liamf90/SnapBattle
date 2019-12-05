package com.liamfarrell.android.snapbattle.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.User
import io.reactivex.Maybe
import io.reactivex.Single


/**
 * The Data Access Object for the [User] class.
 */
@Dao
interface FollowingUserDao {

    @Query("SELECT * FROM user")
     fun getAllFollowingUsers(): List<User>

    @Query("UPDATE User SET mUsername = :username WHERE mCognitoId = :cognitoId")
     fun updateUsername(cognitoId: String, username: String)

    @Query("UPDATE User SET mFacebookName = :name WHERE mCognitoId = :cognitoId")
     fun updateName(cognitoId: String, name: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertAll(users: List<User>)

    @Query("DELETE FROM user WHERE mCognitoId = :userCognitoID")
     fun deleteFromUser(userCognitoID: String)

    @Query("SELECT * FROM user WHERE LOWER(mUsername) LIKE LOWER(:searchQuery) || '%' OR LOWER(mFacebookName) LIKE LOWER(:searchQuery) || '%' OR LOWER(mFacebookName) LIKE '% ' || LOWER(:searchQuery) || '%'")
     fun searchUsersInCache(searchQuery: String): List<User>

    @Query("SELECT * FROM user WHERE LOWER(mUsername) LIKE LOWER(:searchQuery) || '%' OR LOWER(mFacebookName) LIKE LOWER(:searchQuery) || '%' OR LOWER(mFacebookName) LIKE '% ' || LOWER(:searchQuery) || '%'")
    fun searchUsersInCacheRx(searchQuery: String): Single<List<User>>

}