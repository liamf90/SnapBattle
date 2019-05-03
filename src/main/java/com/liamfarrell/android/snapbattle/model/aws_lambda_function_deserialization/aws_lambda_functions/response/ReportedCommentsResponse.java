package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.ReportedComment;

import java.util.List;

public class ReportedCommentsResponse
{
    private List<ReportedComment> sql_result;

    public List<ReportedComment> getSqlResult() {
        return sql_result;
    }

    public void setSqlResult(List<ReportedComment> sql_result) {
        this.sql_result = sql_result;
    }
}
