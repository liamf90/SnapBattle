package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UsernameToFacebookIDRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UsernameToFacebookIDResponse
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import javax.inject.Inject

class ChooseOpponentRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService) {

    suspend fun getFollowing(): AsyncTaskResult<ResponseFollowing> {
        return executeRestApiFunction(snapBattleApiService.getFollowing(true)) 
    }

    suspend fun getRecentOpponents(): AsyncTaskResult<GetUsersResponse> {
        return executeRestApiFunction(snapBattleApiService.getRecentBattleUsers())
        
    }

    suspend fun getUsernameToCognitoId(username: String): AsyncTaskResult<UsernameToFacebookIDResponse> {
        val request = UsernameToFacebookIDRequest()
        request.username = username
        return executeRestApiFunction(snapBattleApiService.usernameToFacebookId(username))
    }




}