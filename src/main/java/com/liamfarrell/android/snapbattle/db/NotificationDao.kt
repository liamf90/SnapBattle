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

    @Query("SELECT n.battleAccepted, n.battleId, n.battleName, " +
            "n.challengedCognitoId, n.challengedUsername, n.challengerCognitoId, n.challengerName, n.challengerUsername, n.cognitoIdChallenger, " +
            " n.notification_index, n.notificationType, n.opponentCognitoId, n.opponentName, n.voteOpponent, n.voteUser, n.votingResult, " +
            " o.last_saved_signed_url as signedUrlProfilePicOpponent,  o.profile_pic_count as opponentProfilePicCount" +
            " FROM notifications AS n LEFT JOIN other_users_profile_pic_signed_url AS o " +
            "ON n.opponentCognitoId = o.cognito_id " +
            "OR n.cognitoIdChallenger = o.cognito_id " +
            "OR n.challengerCognitoId = o.cognito_id " +
            "OR n.challengedCognitoId = o.cognito_id " +
            "ORDER BY notification_index DESC")
    fun getAllNotificationsWithSignedUrls(): DataSource.Factory<Int, NotificationDb>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertAll(notifications: List<NotificationDb>)

    @Query("DELETE FROM notifications")
     fun deleteAllNotifications()

    @Query("SELECT COUNT(*) FROM notifications")
     fun getCountAllNotifications(): Int
}