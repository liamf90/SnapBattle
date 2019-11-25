package com.liamfarrell.android.snapbattle.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "user")
public class User implements Serializable {

    @NonNull @PrimaryKey @SerializedName("CognitoId")  private String mCognitoId;
    @SerializedName("battle_count") private int mBattleCount;
    @SerializedName("following_count")  private int mFollowingCount;
    @SerializedName("Username") private String mUsername;
    @SerializedName("Facebook_Name") private String mFacebookName;
    @SerializedName("FacebookUserId") private String mFacebookUserId;
    @SerializedName("ProfilePicCount") private int mProfilePicCount;
    @SerializedName("isFollowing") private boolean mIsFollowing;
    @SerializedName("profile_pic_small_signed_url") private String mProfilePicSignedUrl;
    private Boolean mIsFollowingChangeInProgress;

    public User(@NonNull String cognitoId, String username, String facebookName, int profilePicCount, String profilePicSignedUrl) {
        mCognitoId = cognitoId;
        mUsername = username;
        mFacebookName = facebookName;
        mProfilePicCount = profilePicCount;
        mProfilePicSignedUrl = profilePicSignedUrl;
    }

    @Ignore
    public User(String facebookName, String facebookUserId)
    {
        mFacebookName = facebookName;
        mFacebookUserId = facebookUserId;
    }

    public Boolean getIsFollowingChangeInProgress() {
        return mIsFollowingChangeInProgress;
    }

    public void setIsFollowingChangeInProgress(Boolean followingChangeInProgress) {
        mIsFollowingChangeInProgress = followingChangeInProgress;
    }

    public void setCognitoId(String cognitoId) {
        mCognitoId = cognitoId;
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

    public boolean getIsFollowing() {
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

    public void setIsFollowing(boolean following) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return mBattleCount == user.mBattleCount &&
                mFollowingCount == user.mFollowingCount &&
                mProfilePicCount == user.mProfilePicCount &&
                mIsFollowing == user.mIsFollowing &&
                Objects.equals(mCognitoId, user.mCognitoId) &&
                Objects.equals(mUsername, user.mUsername) &&
                Objects.equals(mFacebookName, user.mFacebookName) &&
                Objects.equals(mFacebookUserId, user.mFacebookUserId) &&
                Objects.equals(mProfilePicSignedUrl, user.mProfilePicSignedUrl);
    }

}
