package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class GetBattlesByNameRequest
{
    private String battleName;
    private int fetchLimit;
    private int getAfterBattleID;


    public String getBattleName() {
        return battleName;
    }

    public void setBattleName(String battleName) {
        this.battleName = battleName;
    }

    public int getFetchLimit() {
        return fetchLimit;
    }

    public void setFetchLimit(int fetchLimit) {
        this.fetchLimit = fetchLimit;
    }

    public int getAfterBattleID() {
        return getAfterBattleID;
    }

    public void setAfterBattleID(int afterBattleID) {
        this.getAfterBattleID = afterBattleID;
    }
}
