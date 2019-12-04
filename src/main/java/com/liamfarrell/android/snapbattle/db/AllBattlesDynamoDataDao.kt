package com.liamfarrell.android.snapbattle.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liamfarrell.android.snapbattle.data.AllBattlesDynamoCount
import com.liamfarrell.android.snapbattle.model.Battle
import io.reactivex.Completable
import java.util.*


/**
 * The Data Access Object for the [AllBattlesDynamoCount] class.
 */
@Dao
interface AllBattlesDynamoDataDao {
    @Query("SELECT all_battles_dynamo_count FROM all_battles_info LIMIT 1")
    fun getDynamoCount(): Int

    @Query("SELECT last_time_battle_updated FROM all_battles_info LIMIT 1")
     fun getLastTimeBattlesUpdated(): Date?

    @Query("UPDATE all_battles_info SET all_battles_dynamo_count = :allBattlesDynamoCount")
     fun updateAllBattlesDynamoCount(allBattlesDynamoCount: Int)

    @Query("UPDATE all_battles_info SET last_time_battle_updated = :lastTimeBattlesUpdated")
     fun updateLastTimeBattlesUpdated(lastTimeBattlesUpdated: Date)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(allBattlesInfo : AllBattlesDynamoCount)


}