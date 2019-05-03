package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.pjos.RecentBattleNamePOJO;

import java.util.List;

public class RecentBattleResponse
{
    private List<RecentBattleNamePOJO> sql_result;

    public List<RecentBattleNamePOJO> getSqlResult() {
        return sql_result;
    }

    public void setSqlResult(List<RecentBattleNamePOJO> sql_result) {
        this.sql_result = sql_result;
    }
}
