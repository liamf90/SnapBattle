package com.liamfarrell.android.snapbattle.util;

import android.content.Context;


import android.util.Log;

import com.liamfarrell.android.snapbattle.caches.FollowingUserCache;

public class UserFollowingChecker {
    private final static String TAG = "UserFollowingBattle";

    //ignore warning below.
    private boolean mIsCognitoIDLoaded = false;

    public interface FollowingCognitoCallback
    {
        void onFollowingCognitoReceived(String CognitoID);
    }

    public UserFollowingChecker()
    {
        mIsCognitoIDLoaded = false;
    }

    //This method gets the cognito id of either the challenger or challenged that the user follows
    public void getFollowingCognitoID(final String challengerCognitoId, final String challengedCognitoId, final Context context, final FollowingCognitoCallback callback)
    {
        mIsCognitoIDLoaded =false;
        FollowingUserCache.CacheLoadCallbacks followerCacheCallback = new FollowingUserCache.CacheLoadCallbacks() {
            @Override
            public void onUpdated() {
                //ignore lint warning, this if statement is in a callback and it may be set true in the other callbacks.
                if (mIsCognitoIDLoaded = false)
                {
                    checkCognitoIDisFollower(challengerCognitoId, challengedCognitoId, context, callback);
                }
            }
            @Override
            public void onNoUpdates() {
                //ignore lint warning, this if statement is in a callback and it may be set true in the other callbacks.
                if (mIsCognitoIDLoaded = false)
                {
                    Log.i(TAG, "ERROR: CognitoID not found in following list");
                }
            }
            @Override
            public void onLoadedFromSQL() { checkCognitoIDisFollower(challengerCognitoId, challengedCognitoId, context, callback); }
            @Override
            public void onLoadedFromFile() { checkCognitoIDisFollower(challengerCognitoId, challengedCognitoId, context, callback); }
            @Override
            public void onCacheAlreadyLoaded() { checkCognitoIDisFollower(challengerCognitoId, challengedCognitoId, context, callback); }
        };
        FollowingUserCache.get(context, followerCacheCallback);


    }


    private void checkCognitoIDisFollower(String challengerCognitoId, String challengedCognitoId, Context context, FollowingCognitoCallback callback)
    {
        Log.i(TAG, "Checking if CognitoID is follower. Challenger: " +challengerCognitoId + ", Challenged: " +challengedCognitoId);
        final String challengerCognitoID = challengerCognitoId;
        final String challengedCognitoID = challengedCognitoId;
        FollowingUserCache fCache = FollowingUserCache.get(context, null);
        if (fCache.isCognitoIDFollower(challengerCognitoID))
        {
            Log.i(TAG, "Challenger is follower");
            mIsCognitoIDLoaded = true;
            callback.onFollowingCognitoReceived(challengerCognitoID);
        }
        else if (fCache.isCognitoIDFollower(challengedCognitoID))
        {

            Log.i(TAG, "Challenged is follower");
            mIsCognitoIDLoaded = true;
            callback.onFollowingCognitoReceived(challengedCognitoID);
        }

    }
}
