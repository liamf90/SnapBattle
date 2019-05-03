package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

public class CreateBattleResponse
{
    private int battleID;
    private String error;
    private String time_ban_ends;

    public int getBattleID() {
        return battleID;
    }

    public void setBattleID(int battleID) {
        this.battleID = battleID;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTimeBanEnds() {
        return time_ban_ends;
    }

    public void setTimeBanEnds(String time_ban_ends) {
        this.time_ban_ends = time_ban_ends;
    }
}
