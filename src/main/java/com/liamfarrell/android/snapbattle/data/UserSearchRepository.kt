package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserSearchRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService) {

    suspend fun searchUser(searchQuery : String) : AsyncTaskResult<GetUsersResponse> {
        return executeRestApiFunction(snapBattleApiService.userSearch(searchQuery))
    }
}
