package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.AddFollowerRequestWithCognitoIDs
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetUsersBattlesRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.RemoveFollowerRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersBattlesResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import io.reactivex.Single
import javax.inject.Inject

class UsersBattlesRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface)
{
     fun getUsersBattlesRx(cognitoID: String) : Single<GetUsersBattlesResponse> {
        val request = GetUsersBattlesRequest()
        request.getAfterBattleID = -1
        request.cognitoIDUser = cognitoID
        request.fetchLimit = -1
        return  Single.fromCallable{lambdaFunctionsInterface.GetUsersBattles(request)}
    }

    suspend fun getUsersBattles(cognitoID: String) : AsyncTaskResult<GetUsersBattlesResponse> {
        val request = GetUsersBattlesRequest()
        request.getAfterBattleID = -1
        request.cognitoIDUser = cognitoID
        request.fetchLimit = -1
        return executeAWSFunction {lambdaFunctionsInterface.GetUsersBattles(request)}
    }

    suspend fun getUsersBattlesWithFacebookId(facebookId: String) : AsyncTaskResult<GetUsersBattlesResponse> {
        val request = GetUsersBattlesRequest()
        request.getAfterBattleID = -1
        request.facebookId = facebookId
        request.fetchLimit = -1
        return executeAWSFunction {lambdaFunctionsInterface.GetUsersBattles(request)}
    }


    fun getUsersBattlesWithFacebookIdRx(facebookId: String) : Single<GetUsersBattlesResponse> {
        val request = GetUsersBattlesRequest()
        request.getAfterBattleID = -1
        request.facebookId = facebookId
        request.fetchLimit = -1
        return Single.fromCallable {lambdaFunctionsInterface.GetUsersBattles(request)}
    }

    suspend fun followUser(cognitoID: String) : AsyncTaskResult<ResponseFollowing>{
        val request = AddFollowerRequestWithCognitoIDs()
        val cognitoIDFollowList = ArrayList<String>()
        cognitoIDFollowList.add(cognitoID)
        request.cognitoIDFollowList = cognitoIDFollowList
        return executeAWSFunction {lambdaFunctionsInterface.AddFollower(request)}
    }

    fun followUserRx(cognitoID: String) : Single<ResponseFollowing>{
        val request = AddFollowerRequestWithCognitoIDs()
        val cognitoIDFollowList = ArrayList<String>()
        cognitoIDFollowList.add(cognitoID)
        request.cognitoIDFollowList = cognitoIDFollowList
        return Single.fromCallable {lambdaFunctionsInterface.AddFollower(request)}
    }

    fun unfollowUserRx(cognitoID: String) : Single<DefaultResponse> {
        val request = RemoveFollowerRequest()
        request.cognitoIDUnfollow = cognitoID
        return Single.fromCallable{lambdaFunctionsInterface.RemoveFollower(request)}
    }

    suspend fun unfollowUser(cognitoID: String) : AsyncTaskResult<DefaultResponse> {
        val request = RemoveFollowerRequest()
        request.cognitoIDUnfollow = cognitoID
        return executeAWSFunction {lambdaFunctionsInterface.RemoveFollower(request)}
    }



}
