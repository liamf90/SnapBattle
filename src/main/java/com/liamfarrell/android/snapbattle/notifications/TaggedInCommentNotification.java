package com.liamfarrell.android.snapbattle.notifications;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.liamfarrell.android.snapbattle.activity.FullBattleVideoPlayerActivity;
import com.liamfarrell.android.snapbattle.app.App;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.activity.ViewBattleActivity;
import com.liamfarrell.android.snapbattle.activity.ViewBattleFragment;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.util.UserFollowingChecker;

public class TaggedInCommentNotification extends Notification {
    private String mOpponentCognitoId;
    private String mOpponentName;
    private String mBattleName;
    private String mChallengerUsername;
    private String mChallengedUsername;
    private String mChallengerCognitoId;
    private String mChallengedCognitoId;

    public TaggedInCommentNotification(int battleID , String battleName, String opponent_cognito_id, String opponentName, String challengerUsername, String challengedUsername, String challengerCognitoId, String challengedCognitoId)
    {
        super(battleID);
        mOpponentCognitoId = opponent_cognito_id;
        mOpponentName = opponentName;
        mBattleName = battleName;
        mChallengerUsername = challengerUsername;
        mChallengedUsername = challengedUsername;
        mChallengerCognitoId = challengerCognitoId;
        mChallengedCognitoId = challengedCognitoId;
    }

    @Override
    public Intent getIntent(Context context) {

        Intent intent = new Intent(context, FullBattleVideoPlayerActivity.class);
        intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_BATTLEID, super.getBattleId());
        intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_FILE_VIDEO_PATH, Battle.getServerFinalVideoUrlStatic(mChallengerCognitoId, super.getBattleId()));
        intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGER_USERNAME, mChallengerUsername);
        intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGED_USERNAME, mChallengedUsername);
        return intent;
    }

    @Override
    public SpannableStringBuilder getMessage() {
        SpannableStringBuilder longDescription = new SpannableStringBuilder();
        longDescription.append(mOpponentName);
        longDescription.setSpan(new ForegroundColorSpan(0xFFCC5500), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, longDescription.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.append(App.getContext().getResources().getString(R.string.tagged_in_comment_notification_append, mBattleName));

        return longDescription;
    }

    @Override
    public String getOpponentCognitoId() {
        return mOpponentCognitoId;
    }
}
