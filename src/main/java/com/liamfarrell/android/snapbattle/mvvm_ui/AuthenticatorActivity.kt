package com.liamfarrell.android.snapbattle.mvvm_ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.*
import com.amazonaws.mobile.config.AWSConfiguration
import com.liamfarrell.android.snapbattle.MainActivity
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.StartupActivity
import timber.log.Timber


class AuthenticatorActivity : AppCompatActivity() {

   // private val launchCallback = getLaunchCallback()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //java.util.logging.Logger.getLogger("com.amazonaws").level = Level.ALL
         setContentView(R.layout.fragment_logged_in)
        val awsConfig = AWSConfiguration(applicationContext)



        AWSMobileClient.getInstance().initialize(applicationContext, awsConfig, object : Callback<UserStateDetails> {
            override fun onResult(result: UserStateDetails?) {
                if (result?.userState == UserState.SIGNED_IN) {
                    val i = Intent(this@AuthenticatorActivity, MainActivity::class.java)
                    startActivity(i)
                    finish()
                } else if (result?.userState == UserState.SIGNED_OUT) {
                    showSignIn()
                }
            }

            override fun onError(e: Exception?) {
                e?.printStackTrace()
            }
        })
    }


    private fun showSignIn() {
        try {
            AWSMobileClient.getInstance().showSignIn(this,
            SignInUIOptions.builder().nextActivity(StartupActivity::class.java).logo(R.mipmap.ic_launcher).build())
            finish()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }


    }








}



