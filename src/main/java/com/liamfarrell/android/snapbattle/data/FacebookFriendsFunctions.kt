package com.liamfarrell.android.snapbattle.data

import com.facebook.*
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


suspend fun getFriendsList() : AsyncTaskResult<List<User>> =
    suspendCoroutine {continuation ->
    /* make the API call */
    GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/me/friends?fields=id,name,picture", null,
            HttpMethod.GET,
            GraphRequest.Callback { response ->
                if (response.error != null && response.error.errorCode == FacebookRequestError.INVALID_ERROR_CODE){
                    continuation.resume(AsyncTaskResult(response.error.exception))
                }
                else{
                    continuation.resume(AsyncTaskResult<List<User>>(graphResponseToOpponentList(response)))
                }

            }
    ).executeAsync()

}



fun graphResponseToOpponentList(response: GraphResponse): ArrayList<User> {
    val opponentListFromGraph = ArrayList<User>()

    try {
        //Log.i(TAG, "Response: " +  response.getJSONObject().getJSONArray("data").toString());
        val friendsList = response.jsonObject.getJSONArray("data")
        var friend: JSONObject
        var friendName: String
        var friendId: String
        var isSilouhette: Boolean
        var proflilePicPath: String

        for (i in 0 until friendsList.length()) {

            friend = friendsList.getJSONObject(i)
            friendName = friend.getString("name")
            friendId = friend.getString("id")
            proflilePicPath = friend.getJSONObject("picture").getJSONObject("data").getString("url")
            isSilouhette = friend.getJSONObject("picture").getJSONObject("data").getBoolean("is_silhouette")

            val op = User(friendName, friendId)
            if (!isSilouhette){
                op.profilePicSignedUrl = proflilePicPath
            }
            opponentListFromGraph.add(op)
        }

    } catch (e: JSONException) {
        e.printStackTrace()
    }
    return opponentListFromGraph
}

