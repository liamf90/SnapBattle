package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateNameRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateUsernameRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateNameResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateUsernameResponse
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserUpdateRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService) {


    suspend fun updateUsername(newUsername: String): AsyncTaskResult<UpdateUsernameResponse> {
        val updateUsernameRequest = UpdateUsernameRequest()
        updateUsernameRequest.username = newUsername
        return executeRestApiFunction(snapBattleApiService.updateUsername(updateUsernameRequest))
    }

    suspend fun updateName(newName: String): AsyncTaskResult<UpdateNameResponse> {
        return executeRestApiFunction(snapBattleApiService.updateName(UpdateNameRequest().apply { name = newName }))
    }

}
