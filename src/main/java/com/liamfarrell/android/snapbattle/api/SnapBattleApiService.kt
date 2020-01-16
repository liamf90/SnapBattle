package com.liamfarrell.android.snapbattle.api

import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.*
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.*
import retrofit2.Call
import retrofit2.http.*
import java.util.*

/**
 * REST API access points
 */
interface SnapBattleApiService {

    @DELETE("battles/{battleid}/comments/{comment-id}")
    fun deleteComment(@Path("comment-id") commentId: Int, @Path("battleid") battleId: Int) : Call<DeleteCommentResponse>

    @DELETE("battles/{battleid}/likes/me")
    fun undoLike(@Path("battleid") battleId: Int) : Call<DefaultResponse>

    @DELETE("battles/{battleid}/dislikes/me")
    fun undoDislike(@Path("battleid") battleId: Int) : Call<DefaultResponse>

    @PUT("battles/{battleid}/likes/me")
    fun addLike(@Path("battleid") battleId: Int) : Call<DefaultResponse>

    @PUT("battles/{battleid}/dislikes/me")
    fun addDislike(@Path("battleid") battleId: Int) : Call<DefaultResponse>

    //Due to an bug in the AWS signer, lists have to be sent as JSON strings in query parameters
    @GET("users/signed-urls-profile-pictures")
    fun getSignedUrlsProfilePictures(@Query("cognitoIdToGetSignedUrlList") cognitoIdListAsJsonString: String) : Call<GetSignedUrlsResponse>

    @GET("users/me/battles/recent")
    fun getRecentBattleUsers() : Call<GetUsersResponse>

    @DELETE("admin/reported-battles/{battle-id}/deleted")
    fun deleteReportedBattleAdmin(@Path("battleid") battleId: Int) : Call<DeleteBattleResponse>

    @GET("users/me/following")
    fun getFollowing(@Query("shouldGetProfilePic") shouldGetProfilePic: Boolean) : Call<ResponseFollowing>

    @PUT("battles/{battleid}/comments/{comment-id}/report-comment")
    fun reportComment(@Path("comment-id") commentId: Int, @Path("battleid") battleId: Int) : Call <ReportCommentResponse>

    @GET("battles/battle-type-suggestions-search")
    fun battleTypeSuggestionsSearch(@Query("query") searchQuery: String): Call<BattleTypeSuggestionsSearchResponse>

    @PUT("users/me/verify")
    fun verifyUser(@Body request: VerifyUserRequest) : Call<VerifyUserResponse>

    @GET("battles/search-name")
    fun getBattlesByName(@Query("query") searchQuery: String, @Query("fetchLimit") fetchLimit: Int, @Query("getAfterBattleID") getAfterBattleID: Int): Call<GetBattlesByNameResponse>

    @GET("users/me/battles/challenges")
    fun getChallenges() : Call<GetChallengesResponse>

    @PUT("users/me/gcm-id")
    fun updateGCM(@Body request: UpdateGCMRequest) : Call<DefaultResponse>

    @PUT("admin/reported-comments/{comment-id}/delete")
    fun deleteReportedCommentAdmin(@Path("comment-id") commentId: Int) : Call<DeleteCommentResponse>

    @PUT("admin/reported-comments/{comment-id}/ignore")
    fun ignoreReportedCommentAdmin(@Path("comment-id") commentId: Int) : Call<IgnoreCommentResponse>

    @PUT("battles/{battleid}/report")
    fun reportBattle(@Path("battleid") battleId: Int) : Call<ReportBattleResponse>

    //Due to an bug in the AWS signer, lists have to be sent as JSON strings in query parameters
    @GET("users")
    fun getUsers(@Query("userCognitoIDList")  userCognitoIDListAsJson: String) : Call<GetUsersResponse>

    @PUT("battles/{battleid}/increase-video-view-count")
    fun increaseVideoViewCount(@Path("battleid") battleId: Int) : Call<DefaultResponse>

    @POST("battles")
    fun createBattle(@Body request: CreateBattleRequest) : Call<CreateBattleResponse>

    @PUT("battles/{battleid}/battle-accepted")
    fun updateBattleAccepted(@Path("battleid") battleId: Int, @Body request: UpdateBattleAcceptedRequest) : Call<DefaultResponse>

    @PUT("admin/reported-battles/{battleid}/ignore")
    fun ignoreReportedBattlesAdmin(@Path("battleid") battleid: Int) : Call<IgnoreBattleResponse>

    @GET("users/me/battles/completed")
    fun getCompletedBattles(@Query("getAfterDate") getAfterDate: Date?, @Query("fetchLimit")fetchLimit: Int?) : Call<CompletedBattlesResponse>

    @GET("users/me/username-to-facebook-id")
    fun usernameToFacebookId(@Query("username") username : String) : Call<UsernameToFacebookIDResponse>

    @PUT("battles/{battleid}/video-submitted")
    fun videoSubmitted(@Path("battleid") battleid: Int, @Body videoSubmittedRequest: VideoSubmittedRequest) : Call<VideoSubmittedResponse>

    @POST("users/me/following")
    fun addFollowing(@Body request: FollowUserWithFacebookIDsRequest) : Call<ResponseFollowing>

    @POST("users/me/following")
    fun addFollowing(@Body request: AddFollowerRequestWithUsername) : Call<ResponseFollowing>

    @POST("users/me/following")
    fun addFollowing(@Body request: AddFollowerRequestWithCognitoIDs) : Call<ResponseFollowing>

    //Due to an bug in the AWS signer, lists have to be sent as JSON strings in query parameters
    @GET("battles")
    fun getOtherUsersBattles( @Query("id") battleIdListJSON: String, @Query("lastUpdatedDate")lastUpdatedDate: Date?) : Call<GetFriendsBattlesResponse>

    @PUT("admin/ban-user")
    fun banUser(@Body banUserRequest: BanUserRequest) : Call<BanUserResponse>

    @GET("users/me/battles/recent-names")
    fun getRecentBattleNames() : Call<RecentBattleResponse>

    @DELETE("users/me/following/{cognitoid}")
    fun unfollowUser(@Path("cognitoid") cognitoIdUserToUnfollow: String) : Call<DefaultResponse>

    @GET("battles/{battleid}")
    fun getUsersBattle(@Query("battleID") battleID: Int): Call<FriendBattleResponse>

    @PUT("battles/{battleid}/comments")
    fun addComment(@Path("battleid") battleId: Int, @Body addCommentRequest: AddCommentRequest) : Call<AddCommentResponse>

    @PUT("users/me/profile-picture-count")
    fun updateProfilePictureCount(@Body request: UpdateProfilePictureRequest) : Call<DefaultResponse>

    @GET("admin/reported-battles")
    fun getReportedBattles(@Query("fetchLimit") fetchLimit: Int) : Call<ReportedBattlesResponse>

    @GET("battles/{battleid}/comments")
    fun getComments(@Path("battleid") battleid: Int) : Call<GetCommentsResponse>

    @POST("users")
    fun createUser(@Body createUserRequest: CreateUserRequest) : Call<CreateUserResponse>

    @GET("users/me/cloudfrontsignedurl")
    fun getCloudfrontSignedUrl(@Query("url") url: String) : Call<String>

    @GET("users/{cognitoid}/battles")
    fun getUsersBattles(@Path("cognitoid") cognitoId: String, @Query("fetchLimit") fetchLimit: Int = -1, @Query("getAfterBattleID") getAfterBattleID: Int = -1, @Query("facebookId") facebookId: String? = null) : Call<GetUsersBattlesResponse>

    @PUT("users/me/name")
    fun updateName(@Body request: UpdateNameRequest) : Call<UpdateNameResponse>

    @GET("users/search")
    fun userSearch(@Query("query") searchQuery: String) : Call<GetUsersResponse>

    @PUT("users/me/username")
    fun updateUsername(@Body request: UpdateUsernameRequest) : Call<UpdateUsernameResponse>

    @PUT("battles/{battleid}/vote")
    fun doVote(@Body doVoteRequest: DoVoteRequest) : Call<DoVoteResponse>

    @GET("admin/reported-comments")
    fun getReportedCommentsAdmin(@Query("fetchLimit") fetchLimit: Int, @Query("offset") offset: Int)

    @GET("users/me/battles/{battleid}")
    fun getBattle(@Path("battleid") battleId: Int): Call<ResponseBattle>


    @GET("users/me/battles/current")
    fun getCurrentBattles(): Call<CurrentBattleResponse>


    @GET("users/me")
    fun getProfile(): Call<GetProfileResponse>


}