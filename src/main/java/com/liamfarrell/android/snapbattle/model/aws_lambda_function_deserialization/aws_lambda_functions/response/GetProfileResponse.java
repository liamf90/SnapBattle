package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.User;

import java.util.List;

public class GetProfileResponse
{
    private List<User> sql_result;

    public List<User> getSqlResult() {
        return sql_result;
    }

    public void setSqlResult(List<User> sql_result) {
        this.sql_result = sql_result;
    }
}
