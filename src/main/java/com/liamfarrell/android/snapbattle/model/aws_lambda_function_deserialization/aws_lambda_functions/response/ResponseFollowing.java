package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.User;

import java.util.List;

public class ResponseFollowing
{
    public static String userNotExistErrorMessage = "USER_DOES_NOT_EXIST";
    private List<User> sql_result;

    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<User> getSqlResult() {
        return sql_result;
    }

    public void setSqlResult(List<User> sql_result) {
        this.sql_result = sql_result;
    }
}
