package com.liamfarrell.android.snapbattle.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.text.SpannableStringBuilder;

import java.io.Serializable;


public abstract class Notification implements Serializable{

    private int mNotificationIndex;


    private int mBattleId;
    private String signedUrlProfilePicOpponent;
    private int mOpponentProfilePicCount;


    public Notification(int notificationIndex)
    {
        mNotificationIndex = notificationIndex;
        mBattleId = -1;
    }

    public Notification(int notificationIndex, int BattleId)
    {
        mNotificationIndex = notificationIndex;
        mBattleId = BattleId;
        mOpponentProfilePicCount = -1;
    }


    public abstract PendingIntent getIntent(Context context);

    public abstract SpannableStringBuilder getMessage(Context context);

    public int getBattleId()
    {
        return mBattleId;
    }

    public abstract String getOpponentCognitoId();

    public String getSignedUrlProfilePicOpponent()
    {
        return signedUrlProfilePicOpponent;
    }
    public void setSignedUrlProfilePicOpponent(String signedUrl)
    {
        signedUrlProfilePicOpponent = signedUrl;
    }
    public void setOpponentProfilePicCount(int profilePicCount)
    {
        mOpponentProfilePicCount = profilePicCount;
    }
    public int getOpponentProfilePicCount()
    {
        return mOpponentProfilePicCount;
    }

    public void setBattleId(int battleId) {
        mBattleId = battleId;
    }
    public int getNotificationIndex() {
        return mNotificationIndex;
    }
}


