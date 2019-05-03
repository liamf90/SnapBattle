package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class IncreaseVideoViewCountRequest
{
    private int battleID;

    public int getBattleID() {
        return battleID;
    }

    public void setBattleID(int battleID) {
        this.battleID = battleID;
    }
}
