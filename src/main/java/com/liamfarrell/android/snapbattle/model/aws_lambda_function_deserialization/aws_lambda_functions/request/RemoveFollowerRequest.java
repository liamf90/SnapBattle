package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

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
