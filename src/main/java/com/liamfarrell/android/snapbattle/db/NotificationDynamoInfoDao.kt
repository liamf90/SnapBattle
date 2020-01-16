package com.liamfarrell.android.snapbattle.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * The Data Access Object for the [NotificationsDynamoInfo] class.
 */
@Dao
interface NotificationDynamoInfoDao {
    @Query("SELECT notifications_dynamo_count FROM notifications_dynamo_info LIMIT 1")
    suspend fun getNotificationDynamoCount(): Int

    @Query("UPDATE notifications_dynamo_info SET notifications_dynamo_count = :notificationsDynamoCount")
    suspend fun updateNotificationsDynamoCount(notificationsDynamoCount: Int)

    @Query("SELECT has_all_notifications_been_seen FROM notifications_dynamo_info LIMIT 1")
    suspend fun getHasAllNotificationsBeenSeen(): Boolean

    @Query("UPDATE notifications_dynamo_info SET has_all_notifications_been_seen = :hasAllNotificationsBeenSeen")
    suspend fun updateHasAllNotificationsBeenSeen(hasAllNotificationsBeenSeen: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notificationsInfo : NotificationsDynamoInfo)


}