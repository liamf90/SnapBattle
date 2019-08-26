package com.liamfarrell.android.snapbattle.notifications;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.liamfarrell.android.snapbattle.app.SnapBattleApp;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.ui.UsersBattlesActivity;

public class NewFollowerNotification extends Notification {
    private String mOpponentCognitoId;
    private String mOpponentName;
    public NewFollowerNotification(int notificationIndex, String follower_cognito_id, String followingName)
    {
        super(notificationIndex);
        mOpponentCognitoId = follower_cognito_id;
        mOpponentName = followingName;
    }

    @Override
    public Intent getIntent(Context context) {
        Intent intent = new Intent(context, UsersBattlesActivity.class);
        intent.putExtra(UsersBattlesActivity.EXTRA_COGNITO_ID, mOpponentCognitoId);
        return intent;
    }

    @Override
    public SpannableStringBuilder getMessage(Context context) {
        SpannableStringBuilder longDescription = new SpannableStringBuilder();
        longDescription.append(mOpponentName);
        longDescription.setSpan(new ForegroundColorSpan(0xFFCC5500), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.append(context.getResources().getString(R.string.new_follower_notification_append));
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

