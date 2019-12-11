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

    @Query("SELECT * FROM following_battle " +
            " LEFT JOIN  thumbnail_signed_url " +
            " ON thumbnail_signed_url.battle_id = following_battle.battle_id "  +
            " ORDER BY mLastVideoUploadTime DESC")
    fun getAllBattles(): DataSource.Factory<Int,FollowingBattle>

    @Query("SELECT battle_id_following FROM following_battle")
    suspend fun getAllBattleIDs(): List<Int>

    @Query("SELECT COUNT(*) FROM following_battle")
    suspend fun getCountAllBattles(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(battles: List<FollowingBattle>)

    @Query("DELETE FROM following_battle")
    suspend fun deleteAllBattles()

    @Transaction
    suspend fun resetFollowingBattles(){
        deleteAllBattles()
        insertFollowingBattlesInfo(FollowingBattlesDynamoCount())

    }

    @Query("UPDATE following_battle SET mUserHasVoted = 1 WHERE battle_id = :battleId")
    suspend fun setHasVoted(battleId: Int)

    @Query("UPDATE following_battle SET mLikeCount = mLikeCount + 1 WHERE battle_id = :battleId")
    suspend fun increaseLikeCount(battleId: Int)

    @Query("UPDATE following_battle SET mDislikeCount = mDislikeCount + 1 WHERE battle_id = :battleId")
    suspend fun increaseDislikeCount(battleId: Int)

    @Query("UPDATE following_battle SET mLikeCount = mLikeCount - 1 WHERE battle_id = :battleId")
    suspend fun decreaseLikeCount(battleId: Int)

    @Query("UPDATE following_battle SET mDislikeCount = mDislikeCount - 1 WHERE battle_id = :battleId")
    suspend fun decreaseDislikeCount(battleId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowingBattlesInfo(followingBattlesInfo : FollowingBattlesDynamoCount)


}