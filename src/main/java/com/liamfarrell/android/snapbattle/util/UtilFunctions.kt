package com.liamfarrell.android.snapbattle.util

import android.content.Context
import com.liamfarrell.android.snapbattle.di.AWSLambdaModule
import com.liamfarrell.android.snapbattle.di.CommentViewModelFactoryModule
import com.liamfarrell.android.snapbattle.di.DaggerAppComponent

fun getCognitoIDCurrentUser(context: Context) : String{
    val appComponent  = DaggerAppComponent.builder()
            .aWSLambdaModule(AWSLambdaModule(context))
            .build()

    return appComponent.getCognitoIDCachingProvider().cachedIdentityId
}