package com.liamfarrell.android.snapbattle.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("CognitoId")  private String mCognitoId;
    @SerializedName("battle_count") private int mBattleCount;
    @SerializedName("following_count")  private int mFollowingCount;
    @SerializedName("Username") private String mUsername;
    @SerializedName("Facebook_Name") private String mFacebookName;
    @SerializedName("FacebookUserId") private String mFacebookUserId;
    @SerializedName("ProfilePicCount") private int mProfilePicCount;
    @SerializedName("isFollowing") private boolean mIsFollowing;
    @SerializedName("profile_pic_small_signed_url") private String mProfilePicSignedUrl;

    public User(String cognitoId, String username, String facebookName, int profilePicCount, String profilePicSignedUrl) {
        mCognitoId = cognitoId;
        mUsername = username;
        mFacebookName = facebookName;
        mProfilePicCount = profilePicCount;
        mProfilePicSignedUrl = profilePicSignedUrl;
    }

    public User(String facebookName, String facebookUserId)
    {
        mFacebookName = facebookName;
        mFacebookUserId = facebookUserId;
    }

    public String getFacebookUserId() {
        return mFacebookUserId;
    }

    public void setFacebookUserId(String facebookUserId) {
        mFacebookUserId = facebookUserId;
    }

    public String getCognitoId() {
        return mCognitoId;
    }

    public int getBattleCount() {
        return mBattleCount;
    }

    public int getFollowingCount() {
        return mFollowingCount;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getFacebookName() {
        return mFacebookName;
    }

    public int getProfilePicCount() {
        return mProfilePicCount;
    }

    public boolean isFollowing() {
        return mIsFollowing;
    }

    public String getProfilePicSignedUrl() {
        return mProfilePicSignedUrl;
    }

    public void setBattleCount(int battleCount) {
        mBattleCount = battleCount;
    }

    public void setFollowingCount(int followingCount) {
        mFollowingCount = followingCount;
    }

    public void setFollowing(boolean following) {
        mIsFollowing = following;
    }

    public void setProfilePicCount(int profilePicCount) {
        mProfilePicCount = profilePicCount;
    }

    public void setProfilePicSignedUrl(String profilePicSignedUrl) {
        mProfilePicSignedUrl = profilePicSignedUrl;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public void setFacebookName(String facebookName) {
        mFacebookName = facebookName;
    }
}
