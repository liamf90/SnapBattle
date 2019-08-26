package com.liamfarrell.android.snapbattle.db

import com.liamfarrell.android.snapbattle.model.Battle
import androidx.paging.DataSource
import androidx.room.*
import com.liamfarrell.android.snapbattle.model.Video


/**
 * The Data Access Object for the [Battle] class.
 */
@Dao
interface FollowingBattleDao {
    @Query("SELECT * FROM following_battle ORDER BY mLastVideoUploadTime")
    fun getAllBattles(): DataSource.Factory<Int,FollowingBattle>

    @Query("SELECT battle_id_following FROM following_battle")
    suspend fun getAllBattleIDs(): List<Int>

    @Query("SELECT COUNT(*) FROM following_battle")
    suspend fun getCountAllBattles(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(battles: List<FollowingBattle>)

    @Query("DELETE FROM following_battle")
    suspend fun deleteAllBattles()
}