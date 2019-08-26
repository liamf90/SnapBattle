package com.liamfarrell.android.snapbattle.notifications

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liamfarrell.android.snapbattle.caches.NotificationType

@Entity(tableName = "notifications")
class NotificationDb() {
    @PrimaryKey
    @ColumnInfo(name = "notification_index")
    var notificationIndex: Int = -1
    lateinit var notificationType: NotificationType
    var battleId: Int = -1
    var signedUrlProfilePicOpponent: String? = null
    var opponentProfilePicCount: Int = -1
    var battleAccepted: Boolean = false
    var battleName: String? = null
    var challengerName: String? = null
    var cognitoIdChallenger: String? = null
    var opponentCognitoId: String? = null
    var opponentName: String? = null
    var challengerUsername: String? = null
    var challengedUsername: String? = null
    var challengerCognitoId: String? = null
    var challengedCognitoId: String? = null

    var voteUser: Int = 0
    var voteOpponent: Int = 0
    var votingResult: VotingCompleteNotification.VotingResult? = null


    constructor(notification: Notification) : this()
    {
        when (notification) {
            is BattleAcceptedNotification -> {
                notificationType = NotificationType.CHALLENGE_ACCEPTED
                notificationIndex = notification.notificationIndex
                opponentCognitoId = notification.opponentCognitoId
                opponentName = notification.opponentName
                battleAccepted = notification.isBattleAccepted
            }
            is FullVideoUploadedNotification -> {
                notificationType = NotificationType.FULL_VIDEO_CREATED
                notificationIndex = notification.notificationIndex
                battleName = notification.battleName
            }
            is NewBattleRequestNotification -> {
                notificationType = NotificationType.NEW_BATTLE_REQUEST
                notificationIndex = notification.notificationIndex
                challengerName = notification.challengerName
                cognitoIdChallenger = notification.cognitoIdChallenger
            }
            is NewCommentNotification -> {
                notificationType = NotificationType.NEW_COMMENT
                notificationIndex = notification.notificationIndex
                opponentCognitoId = notification.opponentCognitoId
                opponentName = notification.opponentName
                battleName = notification.battleName
            }
            is NewFollowerNotification -> {
                notificationType = NotificationType.NEW_FOLLOWER
                notificationIndex = notification.notificationIndex
                opponentCognitoId = notification.opponentCognitoId
                opponentName = notification.opponentName
            }
            is TaggedInCommentNotification -> {
                notificationType = NotificationType.TAGGED_IN_COMMENT
                notificationIndex = notification.notificationIndex
                opponentCognitoId = notification.opponentCognitoId
                opponentName = notification.opponentName
                battleName = notification.battleName
                challengerUsername = notification.challengerUsername
                challengedUsername = notification.challengedUsername
                challengerCognitoId = notification.challengerCognitoId
                challengedCognitoId = notification.challengedCognitoId
            }
            is VideoSubmittedNotification -> {
                notificationType = NotificationType.VIDEO_SUBMITTED
                notificationIndex = notification.notificationIndex
                opponentCognitoId = notification.opponentCognitoId
                opponentName = notification.opponentName
            }
            is VotingCompleteNotification -> {
                notificationType = NotificationType.VOTE_COMPLETE
                notificationIndex = notification.notificationIndex
                opponentCognitoId = notification.opponentCognitoId
                opponentName = notification.opponentName
                voteUser = notification.battleId
                voteOpponent = notification.voteOpponent
                votingResult = notification.votingResult
            }
        }

    }


    fun getNotification(): Notification {
        return when (notificationType) {
            NotificationType.NEW_BATTLE_REQUEST -> NewBattleRequestNotification(notificationIndex, battleId, cognitoIdChallenger, challengerName)
            NotificationType.CHALLENGE_ACCEPTED -> BattleAcceptedNotification( notificationIndex, battleId, opponentCognitoId, opponentName, battleAccepted)
            NotificationType.VIDEO_SUBMITTED -> VideoSubmittedNotification(notificationIndex, battleId, opponentCognitoId, opponentName)
            NotificationType.NEW_COMMENT -> NewCommentNotification(notificationIndex, battleId, battleName, opponentCognitoId, opponentName)
            NotificationType.FULL_VIDEO_CREATED -> FullVideoUploadedNotification(notificationIndex, battleId, battleName)
            NotificationType.NEW_FOLLOWER -> NewFollowerNotification(notificationIndex, opponentCognitoId, opponentName)
            NotificationType.VOTE_COMPLETE -> VotingCompleteNotification(notificationIndex, battleId, opponentCognitoId, opponentName, voteUser, voteOpponent, votingResult?.name)
            NotificationType.TAGGED_IN_COMMENT -> TaggedInCommentNotification(notificationIndex, battleId, battleName, opponentCognitoId, opponentName,
                    challengerUsername, challengedUsername, challengerCognitoId, challengedCognitoId)
        }
    }






}







