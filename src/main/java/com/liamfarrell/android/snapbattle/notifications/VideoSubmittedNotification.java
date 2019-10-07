package com.liamfarrell.android.snapbattle.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.navigation.NavDeepLinkBuilder;

import com.liamfarrell.android.snapbattle.MainActivity;
import com.liamfarrell.android.snapbattle.app.SnapBattleApp;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.ui.ViewBattleActivity;
import com.liamfarrell.android.snapbattle.ui.ViewBattleFragment;

/**
 * Created by Liam on 9/11/2017.
 */

public class VideoSubmittedNotification extends Notification {
    private String mOpponentCognitoId;
    private String mOpponentName;
    public VideoSubmittedNotification(int notificationIndex, int battleID, String opponent_cognito_id, String opponentName)
    {
        super(notificationIndex,battleID);
        mOpponentCognitoId = opponent_cognito_id;
        mOpponentName = opponentName;
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
        longDescription.append(mOpponentName);
        longDescription.setSpan(new ForegroundColorSpan(0xFFCC5500), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.append(context.getResources().getString(R.string.video_submitted_notification_append));
        return longDescription;

    }

    @Override
    public String getOpponentCognitoId() {
        return mOpponentCognitoId;
    }

    public String getOpponentName() {
        return mOpponentName;
    }
}
