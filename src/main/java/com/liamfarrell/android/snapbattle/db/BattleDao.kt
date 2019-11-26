package com.liamfarrell.android.snapbattle.db

import com.liamfarrell.android.snapbattle.model.Battle
import androidx.paging.DataSource
import androidx.room.*
import com.liamfarrell.android.snapbattle.model.Video


/**
 * The Data Access Object for the [Battle] class.
 */
@Dao
interface BattleDao {
//    @Query("SELECT * FROM all_battles ORDER BY mLastVideoUploadTime DESC")
//    fun getAllBattles(): DataSource.Factory<Int,Battle>

    @Query("SELECT * FROM all_battles " +
            " LEFT JOIN  thumbnail_signed_url " +
            " ON thumbnail_signed_url.battle_id = all_battles.battle_id "  +
            " ORDER BY mLastVideoUploadTime DESC")
    fun getAllBattles(): DataSource.Factory<Int,AllBattlesBattle>

    @Query("SELECT battle_id FROM all_battles")
    suspend fun getAllBattleIDs(): List<Int>

    @Query("SELECT COUNT(*) FROM all_battles")
    suspend fun getCountAllBattles(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(battles: List<Battle>)

    @Query("DELETE FROM all_battles")
    suspend fun deleteAllBattles()

    @Query("UPDATE all_battles SET mUserHasVoted = 1 WHERE battle_id = :battleId")
    suspend fun setHasVoted(battleId: Int)

    @Query("UPDATE all_battles SET mLikeCount = mLikeCount + 1 WHERE battle_id = :battleId")
    suspend fun increaseLikeCount(battleId: Int)

    @Query("UPDATE all_battles SET mDislikeCount = mDislikeCount + 1 WHERE battle_id = :battleId")
    suspend fun increaseDislikeCount(battleId: Int)

    @Query("UPDATE all_battles SET mLikeCount = mLikeCount - 1 WHERE battle_id = :battleId")
    suspend fun decreaseLikeCount(battleId: Int)

    @Query("UPDATE all_battles SET mDislikeCount = mDislikeCount - 1 WHERE battle_id = :battleId")
    suspend fun decreaseDislikeCount(battleId: Int)
}