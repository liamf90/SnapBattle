package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.Battle;

import java.util.List;

public class GetChallengesResponse {
    private List<Battle> sql_result;
    private String error;

    public List<Battle> getSql_result() {
        return sql_result;
    }

    public void setSql_result(List<Battle> sql_result) {
        this.sql_result = sql_result;
    }

    public void setSqlResult(List<Battle> sql_result) {
        this.sql_result = sql_result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
