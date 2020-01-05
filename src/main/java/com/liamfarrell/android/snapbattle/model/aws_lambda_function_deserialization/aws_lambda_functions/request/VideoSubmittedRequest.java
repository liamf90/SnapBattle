package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class VideoSubmittedRequest
{
//    private int battleID;
    private int videoID;
    private String videoRotationLock;

//    public int getBattleID() {
//        return battleID;
//    }
//
//    public void setBattleID(int battleID) {
//        this.battleID = battleID;
//    }

    public int getVideoID() {
        return videoID;
    }

    public void setVideoID(int videoID) {
        this.videoID = videoID;
    }

    public String getVideoRotationLock() {
        return videoRotationLock;
    }

    public void setVideoRotationLock(String videoRotationLock) {
        this.videoRotationLock = videoRotationLock;
    }
}
