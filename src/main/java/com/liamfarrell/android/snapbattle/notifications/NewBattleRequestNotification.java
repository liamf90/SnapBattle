package com.liamfarrell.android.snapbattle.notifications;


import android.app.PendingIntent;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.navigation.NavDeepLinkBuilder;

import com.liamfarrell.android.snapbattle.MainActivity;
import com.liamfarrell.android.snapbattle.R;

/**
 * Created by Liam on 9/11/2017.
 */

public class NewBattleRequestNotification extends Notification {

    private String mChallengerName;
    private String mCognitoIdChallenger;
    public NewBattleRequestNotification(int notificationIndex, int battleID, String cognitoIDChallenger, String challengerName)
    {
        super(notificationIndex,battleID);
        mChallengerName = challengerName;
        mCognitoIdChallenger = cognitoIDChallenger;

    }

    @Override
    public PendingIntent getIntent(Context context) {

        return new NavDeepLinkBuilder(context)
                .setComponentName(MainActivity.class)
                .setGraph(R.navigation.navigation_menu)
                .setDestination(R.id.battleChallengesListFragment)
                .createPendingIntent();
    }

    @Override
    public SpannableStringBuilder getMessage(Context context)
    {
        SpannableStringBuilder longDescription = new SpannableStringBuilder();
        longDescription.append(context.getResources().getString(R.string.battle_request_notification_append));
        int start = longDescription.length();
        longDescription.append(mChallengerName);
        longDescription.setSpan(new ForegroundColorSpan(0xFFCC5500), start, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return longDescription;
    }

    @Override
    public String getOpponentCognitoId() {
        return mCognitoIdChallenger;
    }

    public String getChallengerName() {
        return mChallengerName;
    }

    public String getCognitoIdChallenger() {
        return mCognitoIdChallenger;
    }
}
