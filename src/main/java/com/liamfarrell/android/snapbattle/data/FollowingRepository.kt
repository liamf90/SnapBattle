package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.adapters.AddFacebookFriendsAsFollowersStartupListAdapter
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.*
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowingRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun getFollowing(): AsyncTaskResult<MutableList<User>> {
        val request = FollowingRequest()
        request.isShouldGetProfilePic = true
        return executeAWSFunction {lambdaFunctionsInterface.GetFollowing(request).sqlResult}
    }

    suspend fun getFacebookFriends() : AsyncTaskResult<List<User>> {
        return getFriendsList()
    }

    suspend fun removeFollowing(cognitoIDUnfollow: String) : AsyncTaskResult<DefaultResponse> {
        val request = RemoveFollowerRequest()
        request.cognitoIDUnfollow = cognitoIDUnfollow
        return  executeAWSFunction {lambdaFunctionsInterface.RemoveFollower(request)}
    }

    suspend fun addFollowing(username: String) : AsyncTaskResult<ResponseFollowing> {
        val request = AddFollowerRequestWithUsername()
        request.usernameFollow = username
        return executeAWSFunction{lambdaFunctionsInterface.AddFollower(request)}
    }

    suspend fun addFollowing(facebookIDList : List<String>) :  AsyncTaskResult<ResponseFollowing>{
        val addFollowerRequest = FollowUserWithFacebookIDsRequest()
        addFollowerRequest.facebookFriendIdList = facebookIDList as ArrayList<String>
        return executeAWSFunction{lambdaFunctionsInterface.AddFollower(addFollowerRequest)}

    }

}