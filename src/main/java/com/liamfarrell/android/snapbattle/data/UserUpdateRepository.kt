package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BattleRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateNameRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateUsernameRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateNameResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateUsernameResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserUpdateRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {


    suspend fun updateUsername(newUsername: String): AsyncTaskResult<UpdateUsernameResponse> {
        val request = UpdateUsernameRequest()
        request.username = newUsername
        return executeAWSFunction { lambdaFunctionsInterface.UpdateUsername(request)}
    }

    fun updateUsernameRx(newUsername: String): Single<UpdateUsernameResponse> {
        val request = UpdateUsernameRequest()
        request.username = newUsername
        return Single.fromCallable { lambdaFunctionsInterface.UpdateUsername(request)}
    }

    suspend fun updateName(newName: String): AsyncTaskResult<UpdateNameResponse> {
        val request = UpdateNameRequest()
        request.name = newName
        return executeAWSFunction { lambdaFunctionsInterface.UpdateName(request)}
    }

    fun updateNameRx(newName: String): Single<UpdateNameResponse> {
        val request = UpdateNameRequest()
        request.name = newName
        return Single.fromCallable { lambdaFunctionsInterface.UpdateName(request)}
    }

}
