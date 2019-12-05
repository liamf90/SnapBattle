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
interface FollowingBattleDao {

    @Query("SELECT * FROM following_battle " +
            " LEFT JOIN  thumbnail_signed_url " +
            " ON thumbnail_signed_url.battle_id = following_battle.battle_id "  +
            " ORDER BY mLastVideoUploadTime DESC")
    fun getAllBattles(): DataSource.Factory<Int,FollowingBattle>

    @Query("SELECT battle_id_following FROM following_battle")
    fun getAllBattleIDs(): List<Int>

    @Query("SELECT COUNT(*) FROM following_battle")
    fun getCountAllBattles(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(battles: List<FollowingBattle>)

    @Query("DELETE FROM following_battle")
    fun deleteAllBattles()


    @Query("UPDATE following_battle SET mUserHasVoted = 1 WHERE battle_id = :battleId")
    fun setHasVoted(battleId: Int)  : Completable

    @Query("UPDATE following_battle SET mLikeCount = mLikeCount + 1 WHERE battle_id = :battleId")
    fun increaseLikeCount(battleId: Int)  : Completable

    @Query("UPDATE following_battle SET mDislikeCount = mDislikeCount + 1 WHERE battle_id = :battleId")
    fun increaseDislikeCount(battleId: Int)  : Completable

    @Query("UPDATE following_battle SET mLikeCount = mLikeCount - 1 WHERE battle_id = :battleId")
    fun decreaseLikeCount(battleId: Int)  : Completable

    @Query("UPDATE following_battle SET mDislikeCount = mDislikeCount - 1 WHERE battle_id = :battleId")
    fun decreaseDislikeCount(battleId: Int)  : Completable


}