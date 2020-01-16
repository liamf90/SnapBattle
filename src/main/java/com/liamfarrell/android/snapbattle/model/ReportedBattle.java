package com.liamfarrell.android.snapbattle.model;

public class ReportedBattle extends Battle
{


    private boolean mBattleIgnored = false;
    private boolean mChallengerBanned = false;
    private boolean mChallengedBanned = false;
    private boolean mBattleDeleted = false;

    public ReportedBattle(int battleID, String challengerCognitoId, String challengedCognitoId, String battleName, int rounds, String usernameChallenger, String nameChallenger, String usernameChallenged, String nameChallenged, String thumbnailSignedUrl) {
        super(battleID, challengerCognitoId, challengedCognitoId, battleName, rounds);
        super.setChallengerUsername(usernameChallenger);
        super.setChallengerName(nameChallenger);
        super.setChallengedUsername(usernameChallenged);
        super.setChallengedName(nameChallenged);
        super.setSignedThumbnailUrl(thumbnailSignedUrl);
    }

    public boolean isBattleIgnored() {
        return mBattleIgnored;
    }

    public void setBattleIgnored(boolean battleIgnored) {
        this.mBattleIgnored = battleIgnored;
    }

    public boolean isChallengerBanned() {
        return mChallengerBanned;
    }

    public void setChallengerBanned(boolean challengerBanned) {
        this.mChallengerBanned = challengerBanned;
    }

    public boolean isChallengedBanned() {
        return mChallengedBanned;
    }

    public void setChallengedBanned(boolean challengedBanned) {
        this.mChallengedBanned = challengedBanned;
    }

    public boolean isBattleDeleted() {
        return mBattleDeleted;
    }

    public void setBattleDeleted(boolean battleDeleted) {
        this.mBattleDeleted = battleDeleted;
    }
}
