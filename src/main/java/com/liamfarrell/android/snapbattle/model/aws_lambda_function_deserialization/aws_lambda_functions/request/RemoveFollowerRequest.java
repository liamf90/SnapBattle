package com.liamfarrell.android.snapbattle.model.lambda_function_request_objects;

public class RemoveFollowerRequest
{
    private String cognitoIDUnfollow;

    public String getCognitoIDUnfollow() {
        return cognitoIDUnfollow;
    }

    public void setCognitoIDUnfollow(String cognitoIDUnfollow) {
        this.cognitoIDUnfollow = cognitoIDUnfollow;
    }
}
