package com.liamfarrell.android.snapbattle.model.lambda_function_request_objects;

public class VideoSubmittedRequest
{
    private int battleID;
    private int videoID;
    private String videoRotationLock;

    public int getBattleID() {
        return battleID;
    }

    public void setBattleID(int battleID) {
        this.battleID = battleID;
    }

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
