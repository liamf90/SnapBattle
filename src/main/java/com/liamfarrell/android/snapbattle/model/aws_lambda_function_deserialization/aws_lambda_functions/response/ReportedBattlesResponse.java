package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.ReportedBattle;

import java.util.List;

public class ReportedBattlesResponse
{
    private List<ReportedBattle> sql_result;

    public List<ReportedBattle> getSqlResult() {
        return sql_result;
    }

    public void setSqlResult(List<ReportedBattle> sql_result) {
        this.sql_result = sql_result;
    }
}
