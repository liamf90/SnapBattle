package com.liamfarrell.android.snapbattle.data

import android.app.Application
import com.amazonaws.AmazonClientException
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import java.util.*
import javax.inject.Inject


class FollowingBattlesFeedDynamodbRepository @Inject constructor(val context: Application, val ddbClient : AmazonDynamoDBClient, val awsMobileClient: AWSMobileClient){


    @Throws(AmazonClientException::class)
    fun loadListFromDynamo(startIndex: Int, endIndex: Int): List<Int> {
        // DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        // BattleFeed selectedBattle = mapper.load(BattleFeed.class, "45");

        val cognitoID = awsMobileClient.identityId
        val key = hashMapOf<String, AttributeValue>()
        key["CognitoID"] = AttributeValue().apply {s = cognitoID}

        var projectionExpression = ""
        for (i in startIndex..endIndex) {
            projectionExpression = projectionExpression + "BattleID[" + i + "].battleid"
            if (i != endIndex) {
                projectionExpression = "$projectionExpression,"
            }
        }

        val spec = GetItemRequest()
                .withProjectionExpression(projectionExpression)
                .withTableName("Battle_Activity_Feed").withKey(key)

        val res = ddbClient.getItem(spec).item

        return if (res != null && res.containsKey("BattleID")) {
            res["BattleID"]?.l?.mapNotNull { it.m.get("battleid")?.n?.toInt()} ?: listOf()
        } else {
            listOf()
        }
    }


    @Throws(AmazonClientException::class)
    suspend fun getBattlesCountDynamo(): Int {
        val cognitoID = awsMobileClient.identityId
        val key = HashMap<String, AttributeValue>()
        key["CognitoID"] = AttributeValue().apply {s = cognitoID}

        val projectionExpression = "battle_count"
        val spec = GetItemRequest()
                .withProjectionExpression(projectionExpression)
                .withTableName("Battle_Activity_Feed").withKey(key)
        var battle_count = 0

        val result = ddbClient.getItem(spec)
        val res = result.item

        if (res != null && res.size > 0) {
            val item_count = res["battle_count"]
            battle_count = Integer.parseInt(item_count?.getN() ?: "0")
        }
        return battle_count
    }

    @Throws(AmazonClientException::class)
    suspend fun getFullFeedUpdateCount(): Int {
        val cognitoID = awsMobileClient.identityId
        val key = HashMap<String, AttributeValue>()
        key["CognitoID"] = AttributeValue().apply {s = cognitoID}

        val projectionExpression = "feed_full_update_count"
        val spec = GetItemRequest()
                .withProjectionExpression(projectionExpression)
                .withTableName("Battle_Activity_Feed").withKey(key)
        var battle_count = 0

        val result = ddbClient.getItem(spec)
        val res = result.item

        if (res != null && res.size > 0) {
            val item_count = res["feed_full_update_count"]
            battle_count = Integer.parseInt(item_count?.getN() ?: "0")
        }
        return battle_count
    }


}