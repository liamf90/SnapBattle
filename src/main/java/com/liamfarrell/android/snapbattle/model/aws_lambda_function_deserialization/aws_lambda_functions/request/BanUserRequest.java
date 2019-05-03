package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class BanUserRequest
{
    private int banLengthDays;
    private int commentIDReason;
    private int battleIDReason;
    private String cognitoIDUser;

    public int getBanLengthDays() {
        return banLengthDays;
    }

    public void setBanLengthDays(int banLengthDays) {
        this.banLengthDays = banLengthDays;
    }

    public int getCommentIDReason() {
        return commentIDReason;
    }

    public void setCommentIDReason(int commentIDReason) {
        this.commentIDReason = commentIDReason;
    }

    public int getBattleIDReason() {
        return battleIDReason;
    }

    public void setBattleIDReason(int battleIDReason) {
        this.battleIDReason = battleIDReason;
    }

    public String getCognitoIDUser() {
        return cognitoIDUser;
    }

    public void setCognitoIDUser(String cognitoIDUser) {
        this.cognitoIDUser = cognitoIDUser;
    }
}
