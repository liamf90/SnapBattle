package com.liamfarrell.android.snapbattle.data

import com.facebook.AccessToken
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CreateUserRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CreateUserResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun createUser(facebookID: String, facebookName: String): AsyncTaskResult<CreateUserResponse> {
        val request = CreateUserRequest()
        request.facebookID = facebookID
        request.facebookName = facebookName
        request.setFacebookID(AccessToken.getCurrentAccessToken().userId)
        return executeAWSFunction { lambdaFunctionsInterface.createUser(request) }
    }
}