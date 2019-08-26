package com.liamfarrell.android.snapbattle.mvvm_ui

import android.app.Activity;
import android.content.Intent
import android.os.Bundle;
import android.util.Log
import com.amazonaws.mobile.auth.facebook.FacebookButton
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration

import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.liamfarrell.android.snapbattle.ActivityMainNavigationDrawer
import com.liamfarrell.android.snapbattle.MainActivity
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.StartupActivity
import java.util.logging.Level


class AuthenticatorActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //java.util.logging.Logger.getLogger("com.amazonaws").level = Level.ALL

        AWSMobileClient.getInstance().initialize(this) {
            if (it.isIdentityIdAvailable){
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
            } else {
                val config = AuthUIConfiguration.Builder()
                        .logoResId(com.liamfarrell.android.snapbattle.R.drawable.battle_icon_selected) // Change the logo
                        .signInButton(FacebookButton::class.java) // Show Facebook
                        .build()
                val ui = AWSMobileClient.getInstance().getClient(
                        this@AuthenticatorActivity,
                        SignInUI::class.java) as SignInUI?

                ui?.login(
                        this@AuthenticatorActivity,
                        StartupActivity::class.java)?.authUIConfiguration(config)?.execute()
            }

        }.execute()
    }

}



