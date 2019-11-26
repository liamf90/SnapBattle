package com.liamfarrell.android.snapbattle.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
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

public class BattleAcceptedNotification extends Notification {
    private String mOpponentCogntioId;
    private String mOpponentName;
    private boolean mBattleAccepted;
    public BattleAcceptedNotification(int notificationIndex, int battleId, String opponentCognitoId, String opponentName, boolean battleAccepted)
    {
        super(notificationIndex, battleId);
        mOpponentCogntioId = opponentCognitoId;
        mOpponentName = opponentName;
        mBattleAccepted = battleAccepted;
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

//        Intent intent = new Intent(context, ViewBattleActivity.class);
//        intent.putExtra(ViewBattleFragment.BATTLE_ID_EXTRA, Integer.toString(super.getBattleId()) );
//        return intent;


    }

    @Override
    public SpannableStringBuilder getMessage(Context context)
    {
        SpannableStringBuilder longDescription = new SpannableStringBuilder();
        longDescription.append(mOpponentName);
        longDescription.setSpan(new ForegroundColorSpan(0xFFCC5500), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (mBattleAccepted)
        {
            longDescription.append(context.getResources().getString(R.string.accepted_the_battle));
        }
        else
        {
            longDescription.append(context.getResources().getString(R.string.declined_the_battle));
        }

        return longDescription;

    }

    @Override
    public String getOpponentCognitoId() {
        return mOpponentCogntioId;
    }

    public String getOpponentCogntioId() {
        return mOpponentCogntioId;
    }

    public String getOpponentName() {
        return mOpponentName;
    }

    public boolean isBattleAccepted() {
        return mBattleAccepted;
    }
}
