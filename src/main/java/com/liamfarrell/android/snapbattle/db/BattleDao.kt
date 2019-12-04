package com.liamfarrell.android.snapbattle.db

import com.liamfarrell.android.snapbattle.model.Battle
import androidx.paging.DataSource
import androidx.room.*
import com.liamfarrell.android.snapbattle.model.Video
import io.reactivex.Completable


/**
 * The Data Access Object for the [Battle] class.
 */
@Dao
interface BattleDao {
    @Query("SELECT * FROM all_battles " +
            " LEFT JOIN  thumbnail_signed_url " +
            " ON thumbnail_signed_url.battle_id = all_battles.battle_id "  +
            " ORDER BY mLastVideoUploadTime DESC")
    fun getAllBattles(): DataSource.Factory<Int,AllBattlesBattle>

    @Query("SELECT battle_id FROM all_battles")
    fun getAllBattleIDs(): List<Int>

    @Query("SELECT COUNT(*) FROM all_battles")
    fun getCountAllBattles(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(battles: List<Battle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertAllRx(battles: List<Battle>)

    @Query("DELETE FROM all_battles")
    fun deleteAllBattles() : Completable

    @Query("UPDATE all_battles SET mUserHasVoted = 1 WHERE battle_id = :battleId")
    fun setHasVoted(battleId: Int) : Completable

    @Query("UPDATE all_battles SET mLikeCount = mLikeCount + 1 WHERE battle_id = :battleId")
    fun increaseLikeCount(battleId: Int) : Completable

    @Query("UPDATE all_battles SET mDislikeCount = mDislikeCount + 1 WHERE battle_id = :battleId")
    fun increaseDislikeCount(battleId: Int) : Completable

    @Query("UPDATE all_battles SET mLikeCount = mLikeCount - 1 WHERE battle_id = :battleId")
    fun decreaseLikeCount(battleId: Int) : Completable

    @Query("UPDATE all_battles SET mDislikeCount = mDislikeCount - 1 WHERE battle_id = :battleId")
    fun decreaseDislikeCount(battleId: Int) : Completable
}