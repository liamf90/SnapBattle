package com.liamfarrell.android.snapbattle.notifications;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;

import com.liamfarrell.android.snapbattle.app.App;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.activity.ViewBattleActivity;
import com.liamfarrell.android.snapbattle.activity.ViewBattleFragment;

/**
 * Created by Liam on 10/11/2017.
 */

public class FullVideoUploadedNotification extends Notification {
    private String mBattleName;
    public FullVideoUploadedNotification(int battleID, String battleName)
    {
        super(battleID);
        mBattleName = battleName;

    }

    @Override
    public Intent getIntent(Context context) {
        Intent intent = new Intent(context, ViewBattleActivity.class);
        intent.putExtra(ViewBattleFragment.BATTLE_ID_EXTRA, Integer.toString(super.getBattleId()) );
        return intent;
    }

    @Override
    public SpannableStringBuilder getMessage()
    {
        SpannableStringBuilder longDescription = new SpannableStringBuilder();
        longDescription.append(App.getContext().getResources().getString(R.string.full_video_uploaded_notification_append, mBattleName));
        return longDescription;
    }

    @Override
    public String getOpponentCognitoId() {
        return null;
    }

}
