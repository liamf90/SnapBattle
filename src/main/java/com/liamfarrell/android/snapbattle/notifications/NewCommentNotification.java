package com.liamfarrell.android.snapbattle.notifications;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.liamfarrell.android.snapbattle.app.App;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.ui.ViewBattleActivity;
import com.liamfarrell.android.snapbattle.ui.ViewBattleFragment;

public class NewCommentNotification extends Notification {
    private String mOpponentCognitoId;
    private String mOpponentName;
    private String mBattleName;
    public NewCommentNotification(int battleID , String battleName, String opponent_cognito_id, String opponentName)
    {
        super(battleID);
        mOpponentCognitoId = opponent_cognito_id;
        mOpponentName = opponentName;
        mBattleName = battleName;
    }

    @Override
    public Intent getIntent(Context context) {
        Intent intent = new Intent(context, ViewBattleActivity.class);
        intent.putExtra(ViewBattleFragment.BATTLE_ID_EXTRA, Integer.toString(super.getBattleId()) );
        return intent;
    }

    @Override
    public SpannableStringBuilder getMessage() {
        SpannableStringBuilder longDescription = new SpannableStringBuilder();
        longDescription.append(mOpponentName);
        longDescription.setSpan(new ForegroundColorSpan(0xFFCC5500), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.append(App.getContext().getResources().getString(R.string.new_comment_notification_append, mBattleName));

        return longDescription;
    }

    @Override
    public String getOpponentCognitoId() {
        return mOpponentCognitoId;
    }
}
