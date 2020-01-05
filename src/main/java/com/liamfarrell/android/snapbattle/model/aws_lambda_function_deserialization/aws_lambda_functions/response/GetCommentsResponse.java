package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.Comment;

import java.util.List;

public class GetCommentsResponse
{
    private List<Comment> sql_result;
    private int battle_deleted;

    public List<Comment> getSql_result() {
        return sql_result;
    }

    public void setSqlResult(List<Comment> sql_result) {
        this.sql_result = sql_result;
    }

    public void setSql_result(List<Comment> sql_result) {
        this.sql_result = sql_result;
    }

    public int getBattle_deleted() {
        return battle_deleted;
    }

    public void setBattleDeleted(int battle_deleted) {
        this.battle_deleted = battle_deleted;
    }
}
