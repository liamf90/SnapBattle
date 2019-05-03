package com.liamfarrell.android.snapbattle.model.lambda_function_request_objects;

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

    public int getGetAfterBattleID() {
        return getAfterBattleID;
    }

    public void setGetAfterBattleID(int getAfterBattleID) {
        this.getAfterBattleID = getAfterBattleID;
    }
}
