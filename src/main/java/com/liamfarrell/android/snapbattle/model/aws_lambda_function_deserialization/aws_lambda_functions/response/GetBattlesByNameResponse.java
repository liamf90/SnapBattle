package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.Battle;

import java.util.List;

public class GetBattlesByNameResponse {
    private List<Battle> sql_result;
    private String error;

    public List<Battle> getSqlResult() {
        return sql_result;
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
