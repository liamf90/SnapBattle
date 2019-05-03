package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

public class VerifyUserResponse
{
    public static final String USER_NOT_VERIFIED_RESULT = "USER_NOT_VERIFIED";
    public static final String  USER_VERIFIED_RESULT = "USER_IS_VERIFIED";
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
