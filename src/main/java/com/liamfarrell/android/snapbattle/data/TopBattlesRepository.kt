package com.liamfarrell.android.snapbattle.data

import com.amazonaws.AmazonClientException
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.HashMap
import javax.inject.Inject

class TopBattlesRepository @Inject constructor(val ddbClient : AmazonDynamoDBClient) {


     suspend fun getTopBattlesListFromDynamo(): List<String> {
         return withContext(Dispatchers.IO) {
             try {
                 val key = HashMap<String, AttributeValue>()
                 val value = AttributeValue()
                 value.s = "english"
                 key["Language"] = value
                 val spec = GetItemRequest()
                         .withTableName("Top_Battles").withKey(key).withAttributesToGet("BattleType")
                 val result = ddbClient.getItem(spec)
                 val list = result.item["BattleType"]
                 val topBattleTypeListAttribute = list?.getL()
                 topBattleTypeListAttribute?.map { it.s } ?: listOf()
             } catch (e: AmazonClientException) {
                 //Network error. Return empty list
                 listOf<String>()
             }
         }
     }
}
