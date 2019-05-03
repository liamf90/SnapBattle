package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

public class GetNewSignedUrlResponse
{
    private String CognitoId;
    private String newSignedUrl;
    private int ProfilePicCount;

    public String getCognitoId() {
        return CognitoId;
    }

    public void setCognitoId(String cognitoId) {
        CognitoId = cognitoId;
    }

    public String getNewSignedUrl() {
        return newSignedUrl;
    }

    public void setNewSignedUrl(String newSignedUrl) {
        this.newSignedUrl = newSignedUrl;
    }

    public int getProfilePicCount() {
        return ProfilePicCount;
    }

    public void setProfilePicCount(int profilePicCount) {
        ProfilePicCount = profilePicCount;
    }
}
