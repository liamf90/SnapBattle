package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BattleTypeSuggestionsSearchRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UsersSearchRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserSearchRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun searchUser(searchQuery : String) : AsyncTaskResult<GetUsersResponse> {
        val request = UsersSearchRequest()
        request.userSearchQuery = searchQuery
        return executeAWSFunction {lambdaFunctionsInterface.UserSearch(request)}
    }

    fun searchUserRx(searchQuery : String) : Single<GetUsersResponse> {
        Timber.i("UserSearchRequest")
        val request = UsersSearchRequest()
        request.userSearchQuery = searchQuery
        return Single.fromCallable {lambdaFunctionsInterface.UserSearch(request)}
    }
}
