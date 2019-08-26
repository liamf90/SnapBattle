package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.AddCommentRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.AddDislikeRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BanUserRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BattleTypeSuggestionsSearchRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CompletedBattlesRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CreateBattleRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CreateUserRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CurrentBattlesRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetBattlesByNameRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetCommentsRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetFriendsBattlesRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetFriendsBattlesRequestOld;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetUsersBattlesRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.RemoveFollowerRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateNameRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateUsernameRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UrlLambdaRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UsernameToFacebookIDRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UsersSearchRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.VerifyUserRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.VideoSubmittedRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BanUserResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BattleRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FriendBattleRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DoVoteResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetSignedUrlsResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.ReportedCommentsRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateBattleAcceptedRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.AddFollowerRequestWithCognitoIDs;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FollowUserWithFacebookIDsRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.AddFollowerRequestWithUsername;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.DeleteBattleRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.DeleteCommentRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FollowingRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetUsersRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.IgnoreBattleRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.IgnoreCommentRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.IncreaseVideoViewCountRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.RemoveFacebookFriendAsFollowerRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.ReportBattleRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.ReportCommentRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.ReportedBattlesRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateGCMRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateProfilePictureRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.AddCommentResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CompletedBattlesResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CreateBattleResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CreateUserResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CurrentBattleResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DeleteBattleResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DeleteCommentResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.FriendBattleResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetBattlesByNameResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetChallengesResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetCommentsResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetFriendsBattlesResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetProfileResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersBattlesResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.IgnoreBattleResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.IgnoreCommentResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.RecentBattleResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportBattleResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportCommentResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportedBattlesResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportedCommentsResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseBattle;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateNameResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateUsernameResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UsernameToFacebookIDResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.VerifyUserResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.VideoSubmittedResponse;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.AddLikeRequest;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.DoVoteRequest;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.SignedUrlsRequest;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.UndoDislikeRequest;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.UndoLikeRequest;

/*
 * A holder for lambda functions
 */
public interface LambdaFunctionsInterface {

    public static final String UPGRADE_REQUIRED_ERROR_MESSAGE = "UPGRADE_REQUIRED";


    @LambdaFunction(functionName = "getCloudFrontSignedUrlFunction")
    String getSignedUrl(UrlLambdaRequest url);


    @LambdaFunction(functionName = "getBattleFunction")
    ResponseBattle getBattleFunction(BattleRequest battle);


    @LambdaFunction(functionName = "getCurrentBattles")
    CurrentBattleResponse getCurrentBattle(CurrentBattlesRequest request);


    @LambdaFunction(functionName = "getCompletedBattles")
    CompletedBattlesResponse getCompletedBattles(CompletedBattlesRequest request);


    @LambdaFunction(functionName = "getBattleChallenges")
    GetChallengesResponse getBattleChallenges();


    @LambdaFunction(functionName = "createUser")
    CreateUserResponse createUser(CreateUserRequest request);


    @LambdaFunction(functionName = "doVote")
    DoVoteResponse doVote(DoVoteRequest doVoteRequest);

    @LambdaFunction (functionName= "updateProfilePicture")
    DefaultResponse updateProfilePicture(UpdateProfilePictureRequest request);

    @LambdaFunction(functionName =  "updateGCM")
    DefaultResponse updateGCM(UpdateGCMRequest updateGCMRequest);

    @LambdaFunction (functionName = "removeFollower")
    DefaultResponse RemoveFollower(RemoveFollowerRequest request);


    @LambdaFunction (functionName = "getFriendsBattles")
    GetFriendsBattlesResponse GetFriendsBattles(GetFriendsBattlesRequestOld request);

    @LambdaFunction (functionName = "getFriendsBattles")
    GetFriendsBattlesResponse GetFriendsBattles(GetFriendsBattlesRequest request);

    @LambdaFunction (functionName = "getFollowing")
    ResponseFollowing GetFollowing(FollowingRequest request);


    @LambdaFunction (functionName = "getComments")
    GetCommentsResponse GetComments(GetCommentsRequest requestComments);

    @LambdaFunction (functionName = "addComment")
    AddCommentResponse AddComment(AddCommentRequest request);

    @LambdaFunction (functionName = "updateUsername")
    UpdateUsernameResponse UpdateUsername(UpdateUsernameRequest request);

    @LambdaFunction (functionName = "updateName")
    UpdateNameResponse UpdateName(UpdateNameRequest request);


    @LambdaFunction(functionName = "videoSubmitted")
    VideoSubmittedResponse VideoSubmitted(VideoSubmittedRequest request);

    @LambdaFunction(functionName = "createBattle")
    CreateBattleResponse CreateBattle(CreateBattleRequest request);

    @LambdaFunction(functionName = "updateBattleAccepted")
    DefaultResponse UpdateBattleAccepted(UpdateBattleAcceptedRequest request);

    @LambdaFunction (functionName = "addFollower")
    ResponseFollowing AddFollower(FollowUserWithFacebookIDsRequest request);

    @LambdaFunction (functionName = "addFollower")
    ResponseFollowing AddFollower(AddFollowerRequestWithUsername request);

    @LambdaFunction (functionName = "addFollower")
    ResponseFollowing AddFollower(AddFollowerRequestWithCognitoIDs request);


    @LambdaFunction (functionName = "getProfile")
    GetProfileResponse GetProfile();


    @LambdaFunction (functionName = "getRecentBattleNames")
    RecentBattleResponse GetRecentBattleNames();

    @LambdaFunction (functionName = "getUsers")
    GetUsersResponse GetUsers(GetUsersRequest request);

    @LambdaFunction (functionName = "usernameToFacebookID")
    UsernameToFacebookIDResponse UsernameToFacebookID(UsernameToFacebookIDRequest request);

    @LambdaFunction (functionName = "battleTypeSuggestionsSearch")
    BattleTypeSuggestionsSearchResponse BattleTypeSuggestionsSearch(BattleTypeSuggestionsSearchRequest request);

    @LambdaFunction (functionName = "removeFacebookFriendAsFollower")
    DefaultResponse RemoveFacebookFriendAsFollower(RemoveFacebookFriendAsFollowerRequest req);

    @LambdaFunction (functionName = "getRecentBattleUsers")
    GetUsersResponse GetRecentBattleUsers();

    @LambdaFunction (functionName = "getUsersBattles")
    GetUsersBattlesResponse GetUsersBattles(GetUsersBattlesRequest request);


    @LambdaFunction (functionName = "userSearch")
    GetUsersResponse UserSearch(UsersSearchRequest request);

    @LambdaFunction(functionName = "getSignedUrlsProfilePictures")
    GetSignedUrlsResponse GetProfilePicSignedUrls(SignedUrlsRequest request);

    @LambdaFunction (functionName = "getBattlesByName")
    GetBattlesByNameResponse GetBattlesByName(GetBattlesByNameRequest request);

    @LambdaFunction (functionName = "addLike")
    DefaultResponse AddLike(AddLikeRequest request);

    @LambdaFunction (functionName = "addDislike")
    DefaultResponse AddDisLike(AddDislikeRequest request);

    @LambdaFunction (functionName = "undoLike")
    DefaultResponse UndoLike(UndoLikeRequest request);

    @LambdaFunction (functionName = "undoDislike")
    DefaultResponse UndoDislike(UndoDislikeRequest request);

    @LambdaFunction (functionName = "getFriendBattle")
    FriendBattleResponse GetFriendBattle(FriendBattleRequest request);

    @LambdaFunction (functionName = "verifyUser")
    VerifyUserResponse VerifyUser(VerifyUserRequest request);


    @LambdaFunction (functionName = "reportComment")
    ReportCommentResponse ReportComment(ReportCommentRequest request);

    @LambdaFunction (functionName = "reportBattle")
    ReportBattleResponse ReportBattle(ReportBattleRequest request);

    @LambdaFunction (functionName = "increaseVideoViewCount")
    DefaultResponse IncreaseViewViewCount(IncreaseVideoViewCountRequest request);


    @LambdaFunction (functionName = "deleteComment")
    DeleteCommentResponse DeleteComment(DeleteCommentRequest request);


    @LambdaFunction (functionName = "ignoreCommentAdmin")
    IgnoreCommentResponse IgnoreCommentAdmin(IgnoreCommentRequest request);

    @LambdaFunction (functionName = "banUserAdmin")
    BanUserResponse BanUserAdmin(BanUserRequest request);

    @LambdaFunction (functionName = "deleteCommentAdmin")
    DeleteCommentResponse DeleteCommentAdmin(DeleteCommentRequest request);


    @LambdaFunction (functionName = "getReportedBattles")
    ReportedCommentsResponse GetReportedComments(ReportedCommentsRequest request);


    @LambdaFunction (functionName = "getReportedBattles")
    ReportedBattlesResponse GetReportedBattles(ReportedBattlesRequest request);

    @LambdaFunction (functionName = "deleteReportedBattleAdmin")
    DeleteBattleResponse DeleteBattleAdmin(DeleteBattleRequest request);

    @LambdaFunction (functionName = "ignoreReportedBattleAdmin")
    IgnoreBattleResponse IgnoreBattleAdmin(IgnoreBattleRequest request);


}