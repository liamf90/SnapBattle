package com.liamfarrell.android.snapbattle.mvvm_ui

import android.app.Activity;
import android.content.Intent
import android.os.Bundle;
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.amazonaws.mobile.auth.facebook.FacebookButton
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration

import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.liamfarrell.android.snapbattle.MainActivity
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.StartupActivity


class AuthenticatorActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //java.util.logging.Logger.getLogger("com.amazonaws").level = Level.ALL
         setContentView(R.layout.fragment_logged_in)
        window.statusBarColor = ContextCompat.getColor(this, R.color.Black)
        AWSMobileClient.getInstance().initialize(this) {
            if (it.isIdentityIdAvailable){
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                finish()
            } else {
                val config = AuthUIConfiguration.Builder()
                        .logoResId(com.liamfarrell.android.snapbattle.R.mipmap.ic_launcher) // Change the logo
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



