package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.*
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowingRepository @Inject constructor
(val snapBattleApiService: SnapBattleApiService) {

    suspend fun getFollowing(): AsyncTaskResult<ResponseFollowing> {
        return executeRestApiFunction(snapBattleApiService.getFollowing (true))
    }

    suspend fun getFacebookFriends() : AsyncTaskResult<List<User>> {
        return getFriendsList()
    }

    suspend fun removeFollowing(cognitoIDUnfollow: String) : AsyncTaskResult<DefaultResponse> {
        return executeRestApiFunction(snapBattleApiService.unfollowUser  (cognitoIDUnfollow))
    }

    suspend fun addFollowing(username: String) : AsyncTaskResult<ResponseFollowing> {
        val request = AddFollowerRequestWithUsername()
        request.usernameFollow = username
        return executeRestApiFunction(snapBattleApiService.addFollowing  (request))
    }

    suspend fun addFollowingCognitoId(cognitoId: String) : AsyncTaskResult<ResponseFollowing> {
        val request = AddFollowerRequestWithCognitoIDs()
        val cognitoList = ArrayList<String>()
        cognitoList.add(cognitoId)
        request.cognitoIDFollowList = cognitoList
        return executeRestApiFunction(snapBattleApiService.addFollowing  (request))
    }

    suspend fun addFollowing(facebookIDList : List<String>) :  AsyncTaskResult<ResponseFollowing>{
        val addFollowerRequest = FollowUserWithFacebookIDsRequest()
        addFollowerRequest.facebookFriendIdList = ArrayList(facebookIDList)
        return executeRestApiFunction(snapBattleApiService.addFollowing(addFollowerRequest))

    }

}