package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FollowingRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UsernameToFacebookIDRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UsernameToFacebookIDResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import io.reactivex.Single
import javax.inject.Inject

class ChooseOpponentRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun getFollowing(): AsyncTaskResult<ResponseFollowing> {
        val request = FollowingRequest()
        request.isShouldGetProfilePic = true
        return executeAWSFunction { lambdaFunctionsInterface.GetFollowing(request) }
    }

    fun getFollowingRx(): Single<ResponseFollowing> {
        val request = FollowingRequest()
        request.isShouldGetProfilePic = true
        return Single.fromCallable { lambdaFunctionsInterface.GetFollowing(request) }
    }

    suspend fun getRecentOpponents(): AsyncTaskResult<GetUsersResponse> {
        return executeAWSFunction { lambdaFunctionsInterface.GetRecentBattleUsers() }
    }

    fun getRecentOpponentsRx(): Single<GetUsersResponse> {
        return Single.fromCallable { lambdaFunctionsInterface.GetRecentBattleUsers() }
    }

    suspend fun getUsernameToCognitoId(username: String): AsyncTaskResult<UsernameToFacebookIDResponse> {
        val request = UsernameToFacebookIDRequest()
        request.username = username
        return executeAWSFunction { lambdaFunctionsInterface.UsernameToFacebookID(request)}
    }

    fun getUsernameToCognitoIdRx(username: String): Single<UsernameToFacebookIDResponse> {
        val request = UsernameToFacebookIDRequest()
        request.username = username
        return Single.fromCallable { lambdaFunctionsInterface.UsernameToFacebookID(request)}
    }




}