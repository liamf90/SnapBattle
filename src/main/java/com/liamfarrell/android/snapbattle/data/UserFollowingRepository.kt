package com.liamfarrell.android.snapbattle.data

import com.google.gson.Gson
import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FollowingRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserFollowingRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService) {

    suspend fun getUsers(cognitoIDList : List<String>): AsyncTaskResult<GetUsersResponse> {
        val jsonList = Gson().toJson(cognitoIDList)
        Timber.i("JSON: $jsonList")
        return executeRestApiFunction(snapBattleApiService.getUsers(jsonList.toString()))
    }

    suspend fun getFollowing(): AsyncTaskResult<ResponseFollowing> {
        val request = FollowingRequest()
        request.isShouldGetProfilePic = false
        return executeRestApiFunction(snapBattleApiService.getFollowing(false))
    }
}