package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import java.util.List;

public class BattleTypeSuggestionsSearchResponse
{
    private List<SuggestionsResponse> sql_result;

    public List<SuggestionsResponse> getSqlResult() {
        return sql_result;
    }

    public void setSqlResult(List<SuggestionsResponse> sql_result) {
        this.sql_result = sql_result;
    }
}
