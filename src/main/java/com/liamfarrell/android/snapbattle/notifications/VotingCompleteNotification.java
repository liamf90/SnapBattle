package com.liamfarrell.android.snapbattle.notifications;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.room.Embedded;

import com.liamfarrell.android.snapbattle.app.SnapBattleApp;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.ui.ViewBattleActivity;
import com.liamfarrell.android.snapbattle.ui.ViewBattleFragment;

public class VotingCompleteNotification extends Notification {
    private String mOpponentCognitoId;
    private String mOpponentName;
    private int mVoteUser;
    private int mVoteOpponent;
    @Embedded private VotingResult mVotingResult;

    public enum VotingResult
    {
        WINNER,
        LOSER,
        DRAW
    }

    public VotingCompleteNotification(int notificationIndex,int BattleId, String opponentCognitoId, String opponentName, int voteUser, int voteOpponent, String votingResult) {
        super(notificationIndex,BattleId);
        mOpponentCognitoId = opponentCognitoId;
        mOpponentName = opponentName;
        mVoteUser = voteUser;
        mVoteOpponent = voteOpponent;
        mVotingResult = VotingResult.valueOf(votingResult);
    }


    @Override
    public Intent getIntent(Context context) {
        Intent intent = new Intent(context, ViewBattleActivity.class);
        intent.putExtra(ViewBattleFragment.BATTLE_ID_EXTRA, Integer.toString(getBattleId()) );
        return intent;
    }

    @Override
    public SpannableStringBuilder getMessage(Context context) {
        SpannableStringBuilder longDescription = new SpannableStringBuilder();

        switch (mVotingResult)
        {
            case WINNER:

                longDescription.append(mOpponentName);
                longDescription.setSpan(new ForegroundColorSpan(0xFFCC5500), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                longDescription.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                longDescription.append(context.getResources().getString(R.string.won_battle_notification_message, mVoteUser, mVoteOpponent));
                break;
            case LOSER:

                String startMessage = context.getResources().getString(R.string.lost_battle_notification_message_part1);
                longDescription.append(startMessage);
                longDescription.append(mOpponentName);
                longDescription.setSpan(new ForegroundColorSpan(0xFFCC5500), startMessage.length(), longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                longDescription.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startMessage.length(), longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                longDescription.append(context.getResources().getString(R.string.lost_battle_notification_message_part2, mVoteUser, mVoteOpponent));
                break;
            case DRAW:
                String startMessage2 = context.getResources().getString(R.string.draw_battle_notification_message_part1);
                longDescription.append(startMessage2);
                longDescription.append(mOpponentName);
                longDescription.setSpan(new ForegroundColorSpan(0xFFCC5500), startMessage2.length(), longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                longDescription.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startMessage2.length(), longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                longDescription.append(context.getResources().getString(R.string.draw_battle_notification_message_part2, mVoteUser, mVoteOpponent));
                break;
        }

        return longDescription;
    }

    @Override
    public String getOpponentCognitoId() {
        return mOpponentCognitoId;
    }

    public String getOpponentName() {
        return mOpponentName;
    }

    public int getVoteUser() {
        return mVoteUser;
    }

    public int getVoteOpponent() {
        return mVoteOpponent;
    }

    public VotingResult getVotingResult() {
        return mVotingResult;
    }
}
