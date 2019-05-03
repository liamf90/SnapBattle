package com.liamfarrell.android.snapbattle.notifications;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;

import java.io.Serializable;

/**
 * Created by Liam on 5/11/2017.
 */

public abstract class Notification implements Serializable{
    private int mBattleId;
    private String signedUrlProfilePicOpponent;
    private int mOpponentProfilePicCount;

    public Notification()
    {
        mBattleId = -1;
    }
    public Notification(int BattleId)
    {
        mBattleId = BattleId;
        mOpponentProfilePicCount = -1;
    }

    public abstract Intent getIntent(Context context);

    public abstract SpannableStringBuilder getMessage();

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




}


