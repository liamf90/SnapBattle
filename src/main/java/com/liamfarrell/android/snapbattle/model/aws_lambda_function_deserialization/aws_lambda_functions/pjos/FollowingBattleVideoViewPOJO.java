package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.pjos;

public class FollowingBattleVideoViewPOJO {
    private int user_has_disliked;
    private int user_has_liked;
    private int user_has_voted;
    private int dislike_count;
    private int like_count;
    private int comment_count;
    private int deleted;
    private String challengerFacebookName;
    private String challengedFacebookName;
    private String challenger_facebook_id;
    private String challenged_facebook_id;
    private String challenger_cognito_id;
    private String challenged_cognito_id;
    private String voting_type;
    private String voting_time_end;

    public String getChallenger_cognito_id() {
        return challenger_cognito_id;
    }

    public String getChallenged_cognito_id() {
        return challenged_cognito_id;
    }

    public int getUser_has_disliked() {
        return user_has_disliked;
    }

    public void setUser_has_disliked(int user_has_disliked) {
        this.user_has_disliked = user_has_disliked;
    }

    public int getUser_has_liked() {
        return user_has_liked;
    }

    public void setUser_has_liked(int user_has_liked) {
        this.user_has_liked = user_has_liked;
    }

    public int getUser_has_voted() {
        return user_has_voted;
    }

    public void setUser_has_voted(int user_has_voted) {
        this.user_has_voted = user_has_voted;
    }

    public int getDislike_count() {
        return dislike_count;
    }

    public void setDislike_count(int dislike_count) {
        this.dislike_count = dislike_count;
    }

    public int getLike_count() {
        return like_count;
    }

    public void setLike_count(int like_count) {
        this.like_count = like_count;
    }

    public int getComment_count() {
        return comment_count;
    }

    public void setComment_count(int comment_count) {
        this.comment_count = comment_count;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public String getChallengerFacebookName() {
        return challengerFacebookName;
    }

    public void setChallengerFacebookName(String challengerFacebookName) {
        this.challengerFacebookName = challengerFacebookName;
    }

    public String getChallengedFacebookName() {
        return challengedFacebookName;
    }

    public void setChallengedFacebookName(String challengedFacebookName) {
        this.challengedFacebookName = challengedFacebookName;
    }

    public String getChallenger_facebook_id() {
        return challenger_facebook_id;
    }

    public void setChallenger_facebook_id(String challenger_facebook_id) {
        this.challenger_facebook_id = challenger_facebook_id;
    }

    public String getChallenged_facebook_id() {
        return challenged_facebook_id;
    }

    public void setChallenged_facebook_id(String challenged_facebook_id) {
        this.challenged_facebook_id = challenged_facebook_id;
    }

    public String getVoting_type() {
        return voting_type;
    }

    public void setVoting_type(String voting_type) {
        this.voting_type = voting_type;
    }

    public String getVoting_time_end() {
        return voting_time_end;
    }

    public void setVoting_time_end(String voting_time_end) {
        this.voting_time_end = voting_time_end;
    }
}
