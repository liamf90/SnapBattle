package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

public class UpdateUsernameResponse
{
    private final static String resultExistsAlready = "USERNAME_ALREADY_EXISTS";
    public final static String resultUsernameUpdated = "USERNAME_CHANGED";
    private String result;

    public static String getResultExistsAlready() {
        return resultExistsAlready;
    }

    public static String getResultUsernameUpdated() {
        return resultUsernameUpdated;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
