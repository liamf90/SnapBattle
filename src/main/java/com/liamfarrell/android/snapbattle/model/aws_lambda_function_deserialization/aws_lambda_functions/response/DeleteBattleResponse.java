package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

public class DeleteBattleResponse {
    private int affected_rows;

    public int getAffectedRows() {
        return affected_rows;
    }

    public void setAffectedRows(int affected_rows) {
        this.affected_rows = affected_rows;
    }
}
