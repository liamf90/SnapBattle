package com.liamfarrell.android.snapbattle.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Comment {
    @SerializedName("COMMENT_ID")   private int mCommentId;
    @SerializedName("USERNAME")  private String mUsername;
    @SerializedName("NAME")  private String mName;
    @SerializedName("BATTLEID")  private int mBattleId;
    @SerializedName("deleted")  private boolean mIsDeleted;
    @SerializedName("COMMENT")  private String mComment;
    @SerializedName("TIME")  private Date mTime;
    @SerializedName("COGNITO_ID")  private String mCognitoIdCommenter;
    @SerializedName("PROFILE_PIC_COUNT")  private int mCommenterProfilePicCount;
    @SerializedName("profile_pic_small_signed_url")  private String mCommenterProfilePicSmallSignedUrl;

    public Comment(int commentid, String username, String name, int battleId, boolean isDeleted, String comment, Date time, String cognitoIdCommenter, int commenterProfilePicCount, String commenterProfilePicSmallSignedUrl) {
        mCommentId = commentid;
        mUsername = username;
        mName = name;
        mBattleId = battleId;
        mIsDeleted = isDeleted;
        mComment = comment;
        mTime = time;
        mCognitoIdCommenter = cognitoIdCommenter;
        mCommenterProfilePicCount = commenterProfilePicCount;
        mCommenterProfilePicSmallSignedUrl = commenterProfilePicSmallSignedUrl;
    }

    public int getCommentId() {
        return mCommentId;
    }


    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getBattleId() {
        return mBattleId;
    }

    public void setBattleId(int battleId) {
        mBattleId = battleId;
    }

    public boolean isDeleted() {
        return mIsDeleted;
    }

    public void setDeleted(boolean deleted) {
        mIsDeleted = deleted;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
    }

    public Date getTime() {
        return mTime;
    }

    public void setTime(Date time) {
        mTime = time;
    }

    public String getCognitoIdCommenter() {
        return mCognitoIdCommenter;
    }

    public void setCognitoIdCommenter(String cognitoIdCommenter) {
        mCognitoIdCommenter = cognitoIdCommenter;
    }

    public int getCommenterProfilePicCount() {
        return mCommenterProfilePicCount;
    }

    public void setCommenterProfilePicCount(int commenterProfilePicCount) {
        mCommenterProfilePicCount = commenterProfilePicCount;
    }

    public String getCommenterProfilePicSmallSignedUrl() {
        return mCommenterProfilePicSmallSignedUrl;
    }

    public void setCommenterProfilePicSmallSignedUrl(String commenterProfilePicSmallSignedUrl) {
        mCommenterProfilePicSmallSignedUrl = commenterProfilePicSmallSignedUrl;
    }
}
