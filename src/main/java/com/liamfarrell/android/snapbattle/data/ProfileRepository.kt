package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetUsersBattlesRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateNameRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateUsernameRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetProfileResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersBattlesResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateNameResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateUsernameResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import javax.inject.Inject

class ProfileRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun getProfile(): AsyncTaskResult<GetProfileResponse> {
        return executeAWSFunction { lambdaFunctionsInterface.GetProfile() }
    }

    suspend fun updateUsername(newUsername: String): AsyncTaskResult<UpdateUsernameResponse> {
        val request = UpdateUsernameRequest()
        request.username = newUsername
        return executeAWSFunction { lambdaFunctionsInterface.UpdateUsername(request) }
    }

    suspend fun updateName(newName: String): AsyncTaskResult<UpdateNameResponse> {
        val request = UpdateNameRequest()
        request.name = newName
        return executeAWSFunction { lambdaFunctionsInterface.UpdateName(request) }
    }
}