package com.liamfarrell.android.snapbattle.mvvm_ui.to_be_converted;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.UserStateListener;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.mvvm_ui.AuthenticatorActivity;

import timber.log.Timber;

/**
 * Created by Liam on 25/10/2017.
 */

public class LogoutFragment  extends Fragment {

    private AccessTokenTracker accessTokenTracker;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_logout, parent, false);
        CallbackManager.Factory.create();

        AWSMobileClient.getInstance().addUserStateListener(new UserStateListener() {
            @Override
            public void onUserStateChanged(UserStateDetails details) {
                if (details.getUserState() == UserState.SIGNED_OUT){
                    new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                            .Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {
                            // return to the sign-in screen upon sign-out
                            // Due to a problem with the facebook not fully signing out the user in the cache, the logged out user will be
                            // logged back in on new user log in unless the app is restarted
                            //restartApp();
                            ProcessPhoenix.triggerRebirth(getActivity());

                        }
                    }).executeAsync();
                }
            }

        });

       accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Timber.i("Clearing credentials");
                    AWSMobileClient.getInstance().signOut();

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
