package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.liamfarrell.android.snapbattle.ui.createbattle.ChooseVotingFragment;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.Video;
import com.liamfarrell.android.snapbattle.model.Voting;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

public class BattleDeserializer implements JsonDeserializer<Battle> {
    @Override
    public Battle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

         final JsonObject jsonObject = json.getAsJsonObject();
         int battleID = jsonObject.get("battleid").getAsInt();
         Integer rounds = jsonObject.get("rounds").getAsInt();
         String battleName = jsonObject.get("battle_name").getAsString();
         String challengerCognitoId = jsonObject.get("challenger_cognito_id").getAsString();
         String challengedCognitoId = jsonObject.get("challenged_cognito_id").getAsString();
         Battle b = new Battle(battleID, challengerCognitoId, challengedCognitoId, battleName, rounds);

         if (jsonObject.has("challenger_facebook_name")&& !jsonObject.get("challenger_facebook_name").isJsonNull()) {
             b.setChallengerName(jsonObject.get("challenger_facebook_name").getAsString());
         }
        if (jsonObject.has("challenged_facebook_name")&& !jsonObject.get("challenged_facebook_name").isJsonNull()) {
            b.setChallengedName(jsonObject.get("challenged_facebook_name").getAsString());
        }
        if (jsonObject.has("challenger_facebook_id")&& !jsonObject.get("challenger_facebook_id").isJsonNull()) {
            b.setChallengerFacebookUserId(jsonObject.get("challenger_facebook_id").getAsString());
        }
        if (jsonObject.has("challenged_facebook_id")&& !jsonObject.get("challenged_facebook_id").isJsonNull()) {
            b.setChallengedFacebookUserId(jsonObject.get("challenged_facebook_id").getAsString());
        }
        if (jsonObject.has("challenger_username")&& !jsonObject.get("challenger_username").isJsonNull()) {
            b.setChallengerUsername(jsonObject.get("challenger_username").getAsString());
        }
        if (jsonObject.has("challenged_username") && !jsonObject.get("challenged_username").isJsonNull()) {
            b.setChallengedUsername(jsonObject.get("challenged_username").getAsString());
        }
        if (jsonObject.has("final_video_ready")) {
            b.setFinalVideoReady(JSONDeserializerHelperMethods.getBooleanFromMysqlBool(jsonObject.get("final_video_ready").getAsInt()));
        }

        if (jsonObject.has("video_number_recorded")) {
            b.setVideosUploaded(jsonObject.get("video_number_recorded").getAsInt());
        }
        else if (jsonObject.has("video_number")) {
            b.setVideosUploaded(jsonObject.get("video_number").getAsInt());
        }

        if (jsonObject.has("orientation_lock")) {
            b.setOrientationLock(jsonObject.get("orientation_lock").getAsString());
        }
        if (jsonObject.has("like_count")) {
            b.setLikeCount(jsonObject.get("like_count").getAsInt());
        }
        if (jsonObject.has("dislike_count")) {
            b.setDislikeCount(jsonObject.get("dislike_count").getAsInt());
        }
        if (jsonObject.has("comment_count")) {
            b.setCommentCount(jsonObject.get("comment_count").getAsInt());
        }
        if (jsonObject.has("profile_pic_small_signed_url")) {
            b.setProfilePicSmallSignedUrl(jsonObject.get("profile_pic_small_signed_url").getAsString());
        }
        if (jsonObject.has("thumbnail_signed_url")) {
            b.setSignedThumbnailUrl(jsonObject.get("thumbnail_signed_url").getAsString());
        }
        if (jsonObject.has("challenged_time")) {
            b.setChallengedTime(JSONDeserializerHelperMethods.getDateFromString(jsonObject.get("challenged_time").getAsString()));
        }
        if (jsonObject.has("who_turn")) {
            b.setWhoTurn(Battle.Who_turn.valueOf(jsonObject.get(("who_turn")).getAsString()));
        }
        if (jsonObject.has("date_of_last_upload")&& !jsonObject.get("date_of_last_upload").isJsonNull()) {
            b.setLastVideoUploadTime(JSONDeserializerHelperMethods.getDateFromString(jsonObject.get("date_of_last_upload").getAsString()));
        }
        if (jsonObject.has("challenge_time")&& !jsonObject.get("challenge_time").isJsonNull()) {
            b.setChallengedTime(JSONDeserializerHelperMethods.getDateFromString(jsonObject.get("challenge_time").getAsString()));
        }
        if (jsonObject.has("challenger_profile_pic_count")) {
            b.setChallengerProfilePicCount(jsonObject.get(("challenger_profile_pic_count")).getAsInt());
        }
        if (jsonObject.has("challenged_profile_pic_count")){
            b.setChallengedProfilePicCount(jsonObject.get(("challenged_profile_pic_count")).getAsInt());
        }
        if (jsonObject.has("video_view_count")){
            b.setVideoViewCount(jsonObject.get(("video_view_count")).getAsInt());
        }
        if (jsonObject.has("deleted")) {
            b.setDeleted(JSONDeserializerHelperMethods.getBooleanFromMysqlBool(jsonObject.get("deleted").getAsInt()));
        }
        if (jsonObject.has("battle_accepted")){
            b.setBattleAccepted(JSONDeserializerHelperMethods.getBooleanFromMysqlBool(jsonObject.get("battle_accepted").getAsInt()));
        }
        if (jsonObject.has("user_has_voted")) {
            b.setUserHasVoted(JSONDeserializerHelperMethods.getBooleanFromMysqlBool(jsonObject.get("user_has_voted").getAsInt()));
        }







        if (jsonObject.has("voting_type")) {
            Voting v;
            ChooseVotingFragment.VotingChoice votingType = ChooseVotingFragment.VotingChoice.valueOf(jsonObject.get("voting_type").getAsString());
            if (votingType == ChooseVotingFragment.VotingChoice.NONE) {
                v = new Voting(ChooseVotingFragment.VotingChoice.NONE, null, null, null, null);
            } else {
                ChooseVotingFragment.VotingLength votingLength = ChooseVotingFragment.VotingLength.valueOf(jsonObject.get("voting_length").getAsString());

                Date voting_time_end = null;
                if (jsonObject.has("voting_time_end") && !jsonObject.get("voting_time_end").isJsonNull()){
                    voting_time_end = JSONDeserializerHelperMethods.getDateFromString(jsonObject.get("voting_time_end").getAsString());
                }

                int voteChallenger = 0;
                int vote_challenged = 0;
                if (jsonObject.has("vote_challenger") && !jsonObject.get("vote_challenger").isJsonNull()){
                    voteChallenger = jsonObject.get("vote_challenger").getAsInt();
                }
                if (jsonObject.has("vote_challenged") && !jsonObject.get("vote_challenged").isJsonNull()){
                    vote_challenged = jsonObject.get("vote_challenged").getAsInt();
                }

                v = new Voting(votingType, votingLength, voting_time_end, voteChallenger, vote_challenged);
            }
            b.setVoting(v);
        }

        //b.setBattleAccepted(battleAccepted);
        if (jsonObject.has("videoArray"))
        {
            JsonArray videoJSONArray = jsonObject.getAsJsonArray("videoArray");
            ArrayList<Video> videoList = new ArrayList<Video>();
            for (JsonElement vidElement : videoJSONArray) {
                JsonObject videoJsonObject = vidElement.getAsJsonObject();
                String videoCreatorName = "";
                if (jsonObject.has("video_creator_name")) {
                   videoCreatorName = videoJsonObject.get("video_creator_name").getAsString();
                }

                int videoID = videoJsonObject.get("video_id").getAsInt();
                int videoNumber = videoJsonObject.get("video_number").getAsInt();
                String creatorCognitoId = videoJsonObject.get("creator_cognito_id").getAsString();
                Boolean uploaded = JSONDeserializerHelperMethods.getBooleanFromMysqlBool(videoJsonObject.get("uploaded").getAsInt());
                Date dateUploaded = JSONDeserializerHelperMethods.getDateFromString(videoJsonObject.get("date_uploaded").getAsString());
                Video vid = new Video(videoID, dateUploaded, videoNumber, creatorCognitoId, videoCreatorName, uploaded);
                videoList.add(vid);
            }
            b.setVideos(videoList);
        }


        return b;
    }
}
