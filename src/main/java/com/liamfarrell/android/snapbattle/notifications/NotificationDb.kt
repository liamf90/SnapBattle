package com.liamfarrell.android.snapbattle.notifications

import android.content.Context
import androidx.room.*
import com.liamfarrell.android.snapbattle.caches.NotificationType
import com.liamfarrell.android.snapbattle.db.OtherUsersProfilePicUrlCache

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
            NotificationType.NEW_BATTLE_REQUEST -> NewBattleRequestNotification(notificationIndex, battleId, cognitoIdChallenger, challengerName).also { it.signedUrlProfilePicOpponent = signedUrlProfilePicOpponent; it.opponentProfilePicCount = opponentProfilePicCount }
            NotificationType.CHALLENGE_ACCEPTED -> BattleAcceptedNotification( notificationIndex, battleId, opponentCognitoId, opponentName, battleAccepted).also { it.signedUrlProfilePicOpponent = signedUrlProfilePicOpponent; it.opponentProfilePicCount = opponentProfilePicCount }
            NotificationType.VIDEO_SUBMITTED -> VideoSubmittedNotification(notificationIndex, battleId, opponentCognitoId, opponentName).also { it.signedUrlProfilePicOpponent = signedUrlProfilePicOpponent; it.opponentProfilePicCount = opponentProfilePicCount }
            NotificationType.NEW_COMMENT -> NewCommentNotification(notificationIndex, battleId, battleName, opponentCognitoId, opponentName).also { it.signedUrlProfilePicOpponent = signedUrlProfilePicOpponent; it.opponentProfilePicCount = opponentProfilePicCount }
            NotificationType.FULL_VIDEO_CREATED -> FullVideoUploadedNotification(notificationIndex, battleId, battleName).also { it.signedUrlProfilePicOpponent = signedUrlProfilePicOpponent; it.opponentProfilePicCount = opponentProfilePicCount }
            NotificationType.NEW_FOLLOWER -> NewFollowerNotification(notificationIndex, opponentCognitoId, opponentName).also { it.signedUrlProfilePicOpponent = signedUrlProfilePicOpponent; it.opponentProfilePicCount = opponentProfilePicCount }
            NotificationType.VOTE_COMPLETE -> VotingCompleteNotification(notificationIndex, battleId, opponentCognitoId, opponentName, voteUser, voteOpponent, votingResult?.name).also  { it.signedUrlProfilePicOpponent = signedUrlProfilePicOpponent; it.opponentProfilePicCount = opponentProfilePicCount }
            NotificationType.TAGGED_IN_COMMENT -> TaggedInCommentNotification(notificationIndex, battleId, battleName, opponentCognitoId, opponentName,
                    challengerUsername, challengedUsername, challengerCognitoId, challengedCognitoId).also { it.signedUrlProfilePicOpponent = signedUrlProfilePicOpponent; it.opponentProfilePicCount = opponentProfilePicCount }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationDb

        if (notificationIndex != other.notificationIndex) return false
        if (notificationType != other.notificationType) return false
        if (battleId != other.battleId) return false
        if (signedUrlProfilePicOpponent != other.signedUrlProfilePicOpponent) return false
        if (opponentProfilePicCount != other.opponentProfilePicCount) return false
        if (battleAccepted != other.battleAccepted) return false
        if (battleName != other.battleName) return false
        if (challengerName != other.challengerName) return false
        if (cognitoIdChallenger != other.cognitoIdChallenger) return false
        if (opponentCognitoId != other.opponentCognitoId) return false
        if (opponentName != other.opponentName) return false
        if (challengerUsername != other.challengerUsername) return false
        if (challengedUsername != other.challengedUsername) return false
        if (challengerCognitoId != other.challengerCognitoId) return false
        if (challengedCognitoId != other.challengedCognitoId) return false
        if (voteUser != other.voteUser) return false
        if (voteOpponent != other.voteOpponent) return false
        if (votingResult != other.votingResult) return false

        return true
    }

    override fun hashCode(): Int {
        var result = notificationIndex
        result = 31 * result + battleId
        return result
    }


}







