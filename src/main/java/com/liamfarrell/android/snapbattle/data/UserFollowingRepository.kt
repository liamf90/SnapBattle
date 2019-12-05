package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FollowingRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetUsersRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserFollowingRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun getUsers(cognitoIDList : List<String>): AsyncTaskResult<GetUsersResponse> {
        val request = GetUsersRequest()
        request.userCognitoIDList = cognitoIDList
        return executeAWSFunction { lambdaFunctionsInterface.GetUsers(request) }
    }

    fun getUsersRx(cognitoIDList : List<String>): GetUsersResponse {
        val request = GetUsersRequest()
        request.userCognitoIDList = cognitoIDList
        return lambdaFunctionsInterface.GetUsers(request)
    }


    suspend fun getFollowing(): AsyncTaskResult<ResponseFollowing> {
        val request = FollowingRequest()
        request.isShouldGetProfilePic = false
        return executeAWSFunction {lambdaFunctionsInterface.GetFollowing(request)}
    }

    fun getFollowingRx(): Single<ResponseFollowing> {
        val request = FollowingRequest()
        request.isShouldGetProfilePic = false
        return Single.fromCallable {lambdaFunctionsInterface.GetFollowing(request)}
    }
}