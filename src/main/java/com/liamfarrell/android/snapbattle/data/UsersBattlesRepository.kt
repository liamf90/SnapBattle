package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.AddFollowerRequestWithCognitoIDs
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetUsersBattlesRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersBattlesResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import javax.inject.Inject

class UsersBattlesRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService)
{
    suspend fun getUsersBattles(cognitoID: String) : AsyncTaskResult<GetUsersBattlesResponse> {
        val request = GetUsersBattlesRequest()
        request.getAfterBattleID = -1
        request.fetchLimit = -1
        return executeRestApiFunction(snapBattleApiService.getUsersBattles(cognitoId = cognitoID))
    }

    suspend fun getUsersBattlesWithFacebookId(facebookId: String) : AsyncTaskResult<GetUsersBattlesResponse> {
        return executeRestApiFunction(snapBattleApiService.getUsersBattles("undefined", facebookId = facebookId))
    }

    suspend fun followUser(cognitoID: String) : AsyncTaskResult<ResponseFollowing>{
        val request = AddFollowerRequestWithCognitoIDs()
        val cognitoIDFollowList = ArrayList<String>()
        cognitoIDFollowList.add(cognitoID)
        request.cognitoIDFollowList = cognitoIDFollowList
        return executeRestApiFunction(snapBattleApiService.addFollowing(request))
    }

    suspend fun unfollowUser(cognitoID: String) : AsyncTaskResult<DefaultResponse> {
        return executeRestApiFunction(snapBattleApiService.unfollowUser(cognitoID))
    }



}
