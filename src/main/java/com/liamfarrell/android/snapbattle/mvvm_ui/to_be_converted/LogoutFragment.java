package com.liamfarrell.android.snapbattle.mvvm_ui.to_be_converted;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
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

        IdentityManager.getDefaultIdentityManager().addSignInStateChangeListener(new SignInStateChangeListener() {
            @Override
            // Sign-in listener
            public void onUserSignedIn() {
                Timber.d("User Signed In");
            }

            // Sign-out listener
            @Override
            public void onUserSignedOut() {
                // return to the sign-in screen upon sign-out
                Intent i = new Intent(getActivity(), AuthenticatorActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}
