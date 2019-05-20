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

/**
 * Created by Liam on 9/11/2017.
 */

public class BattleAcceptedNotification extends Notification {
    private String mOpponentCogntioId;
    private String mOpponentName;
    private boolean mBattleAccepted;
    public BattleAcceptedNotification(int battleId, String opponentCognitoId, String opponentName, boolean battleAccepted)
    {
        super(battleId);
        mOpponentCogntioId = opponentCognitoId;
        mOpponentName = opponentName;
        mBattleAccepted = battleAccepted;

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
        longDescription.append(mOpponentName);
        longDescription.setSpan(new ForegroundColorSpan(0xFFCC5500), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (mBattleAccepted)
        {
            longDescription.append(App.getContext().getResources().getString(R.string.accepted_the_battle));
        }
        else
        {
            longDescription.append(App.getContext().getResources().getString(R.string.declined_the_battle));
        }

        return longDescription;

    }

    @Override
    public String getOpponentCognitoId() {
        return mOpponentCogntioId;
    }


}
