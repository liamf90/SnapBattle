package com.liamfarrell.android.snapbattle.model.lambda_function_request_objects;

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
