package com.liamfarrell.android.snapbattle.data

import android.os.Bundle
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CreateUserRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CreateUserResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import org.json.JSONException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class LoginRepository @Inject constructor
(val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun createUser(facebookID: String, facebookName: String): AsyncTaskResult<CreateUserResponse> {
        val request = CreateUserRequest()
        request.facebookID = facebookID
        request.facebookName = facebookName
        request.facebookID = AccessToken.getCurrentAccessToken().userId
        return executeAWSFunction { lambdaFunctionsInterface.createUser(request) }
    }

    suspend fun getFacebookName() : AsyncTaskResult<String> =
            suspendCoroutine { continuation ->
                //Get User Info
                /* make the API call */
                val request = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken()
                ) { `object`, response ->
                    try {
                        continuation.resume(AsyncTaskResult(`object`.getString("name")))

                    } catch (e: JSONException) {
                        continuation.resume(AsyncTaskResult(e))
                        e.printStackTrace()
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "name")
                request.parameters = parameters
                request.executeAsync()

            }

}