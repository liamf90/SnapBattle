package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

public class CreateBattleResponse
{
    //Error names
    public static final String battleNameTooLongError = "BATTLE_NAME_TOO_LONG";
    public static final String ROUNDS_WRONG_AMOUNT_ERROR = "ROUNDS_WRONG_AMOUNT";
    public static final String NOT_BEEN_LONG_ENOUGH_ERROR = "NOT_BEEN_LONG_ENOUGH_ERROR";
    public static final String USER_BANNED_ERROR = "USER_BANNED_ERROR";


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
