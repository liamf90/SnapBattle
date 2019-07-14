package com.liamfarrell.android.snapbattle.mvvm_ui

import android.app.Activity;
import android.os.Bundle;
import com.amazonaws.mobile.auth.facebook.FacebookButton
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration

import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.liamfarrell.android.snapbattle.ActivityMainNavigationDrawer
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.ui.FacebookLoginActivity
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.config.AWSConfiguration



class AuthenticatorActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AWSMobileClient.getInstance().initialize(this) {
            val config = AuthUIConfiguration.Builder()
                                    .logoResId(com.liamfarrell.android.snapbattle.R.drawable.battle_icon_selected) // Change the logo
                                    .signInButton(FacebookButton::class.java) // Show Facebook
                                    .build()

            // Create IdentityManager and set it as the default instance.
            val idm = IdentityManager(applicationContext,
                    AWSConfiguration(applicationContext))
            IdentityManager.setDefaultIdentityManager(idm)

            val ui = AWSMobileClient.getInstance().getClient(
                    this@AuthenticatorActivity,
                    SignInUI::class.java) as SignInUI?
            ui?.login(
                    this@AuthenticatorActivity,
                    ActivityMainNavigationDrawer::class.java)?.authUIConfiguration(config)?.execute()
        }.execute()
    }

}



