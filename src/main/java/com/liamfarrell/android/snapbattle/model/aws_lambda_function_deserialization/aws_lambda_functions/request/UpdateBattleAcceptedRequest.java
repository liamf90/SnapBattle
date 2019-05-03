package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class UpdateBattleAcceptedRequest
{
    private boolean battleAccepted;
    private int battleID;

    public boolean isBattleAccepted() {
        return battleAccepted;
    }

    public void setBattleAccepted(boolean battleAccepted) {
        this.battleAccepted = battleAccepted;
    }

    public int getBattleID() {
        return battleID;
    }

    public void setBattleID(int battleID) {
        this.battleID = battleID;
    }
}
