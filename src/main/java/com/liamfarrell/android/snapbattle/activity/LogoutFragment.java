package com.liamfarrell.android.snapbattle.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.caches.AllBattlesFeedCache;
import com.liamfarrell.android.snapbattle.caches.CurrentUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.caches.FollowingBattleCache;
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache;
import com.liamfarrell.android.snapbattle.caches.NotificationCache;
import com.liamfarrell.android.snapbattle.caches.OtherUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.caches.ThumbnailCacheHelper;

/**
 * Created by Liam on 25/10/2017.
 */

public class LogoutFragment  extends Fragment {
private final String TAG = "LogoutFragment";

    private AccessTokenTracker accessTokenTracker;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_logout, parent, false);
        CallbackManager.Factory.create();

       accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Log.i(TAG, "Clearing credentials");

                    FacebookLoginFragment.getCredentialsProvider(getActivity()).clear();

                    Intent i = new Intent(getActivity(), FacebookLoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            }
        };


        return v;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}
