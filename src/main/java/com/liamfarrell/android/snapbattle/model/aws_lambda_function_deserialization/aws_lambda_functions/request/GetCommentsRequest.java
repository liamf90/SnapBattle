package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class GetCommentsRequest
{
    private int battleID;
    private int lastCommentID;

    public int getBattleID() {
        return battleID;
    }

    public void setBattleID(int battleID) {
        this.battleID = battleID;
    }

    public int getLastCommentID() {
        return lastCommentID;
    }

    public void setLastCommentID(int lastCommentID) {
        this.lastCommentID = lastCommentID;
    }
}
