package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.Battle;

public class ResponseBattle {
    private Battle sql_result;

    public Battle getSqlResult() {
        return sql_result;
    }

    public void setSqlResult(Battle sql_result) {
        this.sql_result = sql_result;
    }
}
