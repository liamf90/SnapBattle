package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

public class UpdateNameResponse
{
    private final static String resultNameUpdated = "NAME_CHANGED";
    private final static String NameTooLongErrorCode = "NAME_TOO_LONG";
    private String result;

    public static String getResultNameUpdated() {
        return resultNameUpdated;
    }

    public static String getNameTooLongErrorCode() {
        return NameTooLongErrorCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
