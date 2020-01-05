package com.liamfarrell.android.snapbattle.data
import com.amazonaws.AmazonClientException
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.HashMap
import javax.inject.Inject


class UserFollowingDynamodbRepository @Inject constructor(val ddbClient : AmazonDynamoDBClient){



    @Throws(AmazonClientException::class)
    suspend fun getActionListFromDynamo(startIndex: Int, endIndex: Int): List<AttributeValue> {
        return withContext(Dispatchers.IO) {
            // DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            // BattleFeed selectedBattle = mapper.load(BattleFeed.class, "45");
            val key = hashMapOf<String, AttributeValue>()
            key["CognitoID"] = AttributeValue().apply { s = IdentityManager.getDefaultIdentityManager().cachedUserID }

            var projectionExpression = ""
            for (i in startIndex..endIndex) {
                projectionExpression = projectionExpression + "Following_Action_List[" + i + "]"
                if (i != endIndex) {
                    projectionExpression = "$projectionExpression,"
                }
            }

            val spec = GetItemRequest()
                    .withProjectionExpression(projectionExpression)
                    .withTableName("Battle_Activity_Feed").withKey(key)

            val result = ddbClient.getItem(spec)
            val res = result.item
            val list = res["Following_Action_List"]
            return@withContext list?.l?.reversed() ?: listOf()

//        var actionMap: Map<String, AttributeValue>
//        var action: String?
//        var cognitoID: String?
//        //Get all the cognitoId's of users to add, so we can get the information to retrieve from server
//        return  actionList.filter { it.m["ACTION"]?.s == ACTION_ADD }.mapNotNull { it.m["COGNITO_ID"]?.s }
        }
    }


    @Throws(AmazonClientException::class)
    suspend fun getFollowingUpdateCountDynamo(): Int {
        return withContext(Dispatchers.IO) {
            val key = HashMap<String, AttributeValue>()
            key["CognitoID"] = AttributeValue().apply { s = IdentityManager.getDefaultIdentityManager().cachedUserID }

            val projectionExpression = "Following_updated_count"
            val spec = GetItemRequest()
                    .withProjectionExpression(projectionExpression)
                    .withTableName("Battle_Activity_Feed").withKey(key)
            var following_update_count = 0

            val result = ddbClient.getItem(spec)
            val res = result.item

            if (res != null && res.size > 0) {
                val item_count = res["Following_updated_count"]
                following_update_count = Integer.parseInt(item_count?.getN() ?: "0")
            }
            return@withContext following_update_count
        }
    }


}

