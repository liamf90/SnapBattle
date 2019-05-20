package com.liamfarrell.android.snapbattle.notifications;


import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.liamfarrell.android.snapbattle.app.App;
import com.liamfarrell.android.snapbattle.ui.BattleChallengesListActivity;
import com.liamfarrell.android.snapbattle.R;

/**
 * Created by Liam on 9/11/2017.
 */

public class NewBattleRequestNotification extends Notification {

    private String mChallengerName;
    private String mCognitoIdChallenger;
    public NewBattleRequestNotification(int battleID, String cognitoIDChallenger, String challengerName)
    {
        super(battleID);
        mChallengerName = challengerName;
        mCognitoIdChallenger = cognitoIDChallenger;

    }

    @Override
    public Intent getIntent(Context context) {
        Intent intent = new Intent(context, BattleChallengesListActivity.class);
        return intent;
    }

    @Override
    public SpannableStringBuilder getMessage()
    {
        SpannableStringBuilder longDescription = new SpannableStringBuilder();
        longDescription.append(App.getContext().getResources().getString(R.string.battle_request_notification_append));
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

}
