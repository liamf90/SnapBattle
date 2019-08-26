package com.liamfarrell.android.snapbattle.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liamfarrell.android.snapbattle.notifications.Notification
import com.liamfarrell.android.snapbattle.notifications.NotificationDb

/**
 * The Data Access Object for the [NotificationDb] class.
 */
@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY notification_index DESC")
    fun getAllNotifications(): DataSource.Factory<Int, NotificationDb>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationDb>)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun getCountAllNotifications(): Int
}