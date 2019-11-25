package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class GetUsersBattlesRequest
{
    private String cognitoIDUser;
    private int fetchLimit;
    private int getAfterBattleID;
    private String facebookId;

    public String getCognitoIDUser() {
        return cognitoIDUser;
    }

    public void setCognitoIDUser(String cognitoIDUser) {
        this.cognitoIDUser = cognitoIDUser;
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

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }
}
