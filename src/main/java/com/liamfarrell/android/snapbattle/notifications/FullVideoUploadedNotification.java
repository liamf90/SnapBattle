package com.liamfarrell.android.snapbattle.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;

import androidx.navigation.NavDeepLinkBuilder;

import com.liamfarrell.android.snapbattle.MainActivity;
import com.liamfarrell.android.snapbattle.app.SnapBattleApp;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.ui.ViewBattleActivity;
import com.liamfarrell.android.snapbattle.ui.ViewBattleFragment;

/**
 * Created by Liam on 10/11/2017.
 */

public class FullVideoUploadedNotification extends Notification {
    private String mBattleName;
    public FullVideoUploadedNotification(int notificationIndex,int battleID, String battleName)
    {
        super(notificationIndex, battleID);
        mBattleName = battleName;

    }

    @Override
    public PendingIntent getIntent(Context context) {
        Bundle args = new Bundle();
        args.putInt("battleId", super.getBattleId());

        return new NavDeepLinkBuilder(context)
                .setComponentName(MainActivity.class)
                .setGraph(R.navigation.navigation_menu)
                .setDestination(R.id.viewBattleFragment)
                .setArguments(args)
                .createPendingIntent();

    }

    @Override
    public SpannableStringBuilder getMessage(Context context)
    {
        SpannableStringBuilder longDescription = new SpannableStringBuilder();
        longDescription.append(context.getResources().getString(R.string.full_video_uploaded_notification_append, mBattleName));
        return longDescription;
    }

    @Override
    public String getOpponentCognitoId() {
        return null;
    }

    public String getBattleName() {
        return mBattleName;
    }
}
