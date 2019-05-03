package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.pjos.FollowingBattleVideoViewPOJO;

public class FriendBattleResponse
{
    private FollowingBattleVideoViewPOJO sql_result;
    private String cognitoIDUserRequest;

    public FollowingBattleVideoViewPOJO getSql_result() {
        return sql_result;
    }

    public void setSqlResult(FollowingBattleVideoViewPOJO sql_result) {
        this.sql_result = sql_result;
    }

    public String getUserRequestingCognitoId()
    {
        return cognitoIDUserRequest;
    }
}
