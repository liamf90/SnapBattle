package com.liamfarrell.android.snapbattle.mvvm_ui.to_be_converted;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookActivity;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.di.AWSModule;
import com.liamfarrell.android.snapbattle.mvvm_ui.AuthenticatorActivity;

import timber.log.Timber;

import static com.facebook.FacebookSdk.getApplicationContext;

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
        IdentityManager.getDefaultIdentityManager().addSignInStateChangeListener(new SignInStateChangeListener() {
            @Override
            // Sign-in listener
            public void onUserSignedIn() {
                Timber.d("User Signed In");
            }

            // Sign-out listener
            @Override
            public void onUserSignedOut() {

                new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                        .Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        // return to the sign-in screen upon sign-out
                        // Due to a problem with the facebook not fully signing out the user in the cache, the logged out user will be
                        // logged back in on new user log in unless the app is restarted
                        restartApp();

                    }
                }).executeAsync();

            }
        });

       accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Timber.i("Clearing credentials");
                    IdentityManager.getDefaultIdentityManager().signOut();
                }
            }
        };

        return v;
    }

    private void restartApp(){
        Intent mStartActivity = new Intent(getActivity(), AuthenticatorActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);

    }




    @Override
    public void onDestroy()
    {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}
