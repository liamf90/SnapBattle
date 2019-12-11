package com.liamfarrell.android.snapbattle.data

import com.amazonaws.AmazonClientException
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest
import com.liamfarrell.android.snapbattle.notifications.NotificationType
import com.liamfarrell.android.snapbattle.notifications.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.HashMap
import javax.inject.Inject

class NotificationsDynamoDbRepository @Inject constructor(val ddbClient : AmazonDynamoDBClient){


    @Throws(AmazonClientException::class)
    suspend fun getNotificationListFromDynamo(startIndex: Int, endIndex: Int): List<Notification> {
        return withContext(Dispatchers.IO) {
            val key = hashMapOf<String, AttributeValue>()
            key["CognitoID"] = AttributeValue().apply { s = IdentityManager.getDefaultIdentityManager().cachedUserID }

            var projectionExpression = ""
            for (i in startIndex..endIndex) {
                projectionExpression = projectionExpression + "NotificationList[" + i + "]"
                if (i != endIndex) {
                    projectionExpression = "$projectionExpression,"
                }
            }

            val spec = GetItemRequest()
                    .withProjectionExpression(projectionExpression)
                    .withTableName("Battle_Activity_Feed").withKey(key)

            val result = ddbClient.getItem(spec)
            val res = result.item
            val list = res["NotificationList"]

            val notificationsDynamo = list?.l

            val notificationCountDynamo = getNotificationCountDynamo()
            val lastIndexRetrieved = startIndex + (notificationsDynamo?.size ?: 0) - 1
            val notificationIndexList = (startIndex..lastIndexRetrieved).toList().map { notificationCountDynamo - 1 - it }

            notificationsDynamo?.let {
                val pairs = notificationIndexList zip (notificationsDynamo)

                return@withContext pairs.map {
                    val index = it.first
                    when (NotificationType.valueOf(it.second.m["TYPE"]?.s
                            ?: throw IllegalArgumentException())) {
                        NotificationType.NEW_BATTLE_REQUEST -> {
                            val battleId = Integer.parseInt(it.second.m["BATTLE_ID"]?.n ?: "0")
                            val cognitoIdChallenger = it.second.m["COGNITO_ID_CHALLENGER"]?.s
                            val challengerName = it.second.m["CHALLENGER_NAME"]?.s
                            NewBattleRequestNotification(index, battleId, cognitoIdChallenger, challengerName)
                        }
                        NotificationType.VIDEO_SUBMITTED -> {
                            val battleId = Integer.parseInt(it.second.m["BATTLE_ID"]!!.n)
                            val cognitoIdChallenger = it.second.m["COGNITO_ID_OPPONENT"]?.s
                            val challengerName = it.second.m["OPPONENT_NAME"]?.s
                            VideoSubmittedNotification(index, battleId, cognitoIdChallenger, challengerName)
                        }
                        NotificationType.CHALLENGE_ACCEPTED -> {
                            val battleId = Integer.parseInt(it.second.m["BATTLE_ID"]?.n ?: "0")
                            val cognitoIdOpponent = it.second.m["COGNITO_ID_OPPONENT"]?.s
                            val opponentName = it.second.m["OPPONENT_NAME"]?.s
                            val battleAccepted = it.second.m["ACCEPTED"]?.bool ?: false
                            BattleAcceptedNotification(index, battleId, cognitoIdOpponent, opponentName, battleAccepted)
                        }
                        NotificationType.FULL_VIDEO_CREATED -> {
                            val battleId = Integer.parseInt(it.second.m["BATTLE_ID"]!!.n)
                            val battleName = it.second.m["BATTLE_NAME"]?.s
                            FullVideoUploadedNotification(index, battleId, battleName)
                        }
                        NotificationType.NEW_COMMENT -> {
                            val battleID = Integer.parseInt(it.second.m["BATTLE_ID"]!!.n)
                            val battleName = it.second.m["BATTLE_NAME"]?.s
                            val cognitoIdCommenter = it.second.m["COGNITO_ID_COMMENTER"]?.s
                            val commenterName = it.second.m["COMMENTER_NAME"]?.getS()
                            NewCommentNotification(index, battleID, battleName, cognitoIdCommenter, commenterName)
                        }
                        NotificationType.TAGGED_IN_COMMENT -> {
                            val battleID = Integer.parseInt(it.second.m["BATTLE_ID"]!!.n)
                            val battleName = it.second.m["BATTLE_NAME"]?.s
                            val cognitoIdCommenter = it.second.m["COGNITO_ID_COMMENTER"]?.s
                            val commenterName = it.second.m["COMMENTER_NAME"]?.s
                            val cognitoIdChallenger = it.second.m["COGNITO_ID_CHALLENGER"]?.s
                            val cognitoIdChallenged = it.second.m["COGNITO_ID_CHALLENGED"]?.s
                            val usernameChallenger = it.second.m["USERNAME_CHALLENGER"]?.s
                            val usernameChallenged = it.second.m["USERNAME_CHALLENGED"]?.s
                            TaggedInCommentNotification(index, battleID, battleName, cognitoIdCommenter, commenterName, usernameChallenger, usernameChallenged, cognitoIdChallenger, cognitoIdChallenged)
                        }
                        NotificationType.NEW_FOLLOWER -> {
                            val cognitoIdFollower = it.second.m["COGNITO_ID_FOLLOWER"]?.s
                            val followerName = it.second.m["FOLLOWER_NAME"]?.getS()
                            NewFollowerNotification(index, cognitoIdFollower, followerName)
                        }
                        NotificationType.VOTE_COMPLETE -> {
                            val battleid = Integer.parseInt(it.second.m["BATTLE_ID"]!!.n)
                            val cognitoIdOpponent = it.second.m["COGNITO_ID_OPPONENT"]?.s
                            val opponentName = it.second.m["OPPONENT_NAME"]?.s
                            val vote = Integer.parseInt(it.second.m["VOTE"]!!.n)
                            val voteOpponent = Integer.parseInt(it.second.m["VOTE_OPPONENT"]!!.n)
                            val votingResult = it.second.m["RESULT"]?.s
                            VotingCompleteNotification(index, battleid, cognitoIdOpponent, opponentName, vote, voteOpponent, votingResult)
                        }
                    }

                }

            } ?: listOf<Notification>()



        }

    }


    @Throws(AmazonClientException::class)
    suspend fun getNotificationCountDynamo(): Int {
        return withContext(Dispatchers.IO) {
            val key = HashMap<String, AttributeValue>()
            key["CognitoID"] = AttributeValue().apply { s = IdentityManager.getDefaultIdentityManager().cachedUserID }

            val projectionExpression = "notification_count"
            val spec = GetItemRequest()
                    .withProjectionExpression(projectionExpression)
                    .withTableName("Battle_Activity_Feed").withKey(key)
            var notification_count = 0

            val result = ddbClient.getItem(spec)
            val res = result.item

            if (res != null && res.size > 0) {
                val item_count = res["notification_count"]
                notification_count = Integer.parseInt(item_count?.getN() ?: "0")
            }
            return@withContext notification_count

        }
    }


    @Throws(AmazonClientException::class)
    suspend fun getDynamoHasSeenAllNotifications(): Boolean {
        return withContext(Dispatchers.IO) {
            val key = HashMap<String, AttributeValue>()
            key["CognitoID"] = AttributeValue().apply { s = IdentityManager.getDefaultIdentityManager().cachedUserID }

            val projectionExpression = "notifications_seen"
            val spec = GetItemRequest()
                    .withProjectionExpression(projectionExpression)
                    .withTableName("Battle_Activity_Feed").withKey(key)

            val result = ddbClient.getItem(spec)
            val res = result.item

            return@withContext if (res == null) {
                true
            } else {
                val hasSeenNotifications = res["notifications_seen"]
                if (hasSeenNotifications != null) {
                    hasSeenNotifications.bool
                } else {
                    true
                }
            }
        }
    }

    @Throws(AmazonClientException::class)
    suspend fun updateDynamoSeenAllNotifications(){
        withContext(Dispatchers.IO) {
            val key = HashMap<String, AttributeValue>()
            key["CognitoID"] = AttributeValue().apply { s = IdentityManager.getDefaultIdentityManager().cachedUserID }
            val notificationSeenAttribute = HashMap<String, AttributeValue>()
            val notificationsSeenFalse = AttributeValue()
            notificationsSeenFalse.isBOOL = true
            notificationSeenAttribute[":val1"] = notificationsSeenFalse

            val updateExpression = "SET notifications_seen = :val1"
            val uir = UpdateItemRequest()
                    .withTableName("Battle_Activity_Feed")
                    .withKey(key).withUpdateExpression(updateExpression)
                    .withExpressionAttributeValues(notificationSeenAttribute)
            ddbClient.updateItem(uir)
        }
    }
}