package com.liamfarrell.android.snapbattle.app;

import android.app.Application;
import android.content.Context;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.liamfarrell.android.snapbattle.workers.MyWorkerFactory;

import javax.inject.Inject;

public class App extends Application {

    private static Context mContext;
    @Inject MyWorkerFactory myWorkerFactory;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;


        WorkManager.initialize(
                this,
                new Configuration.Builder()
                        .setWorkerFactory(myWorkerFactory)
                        .build()
        );
    }

    public static Context getContext(){
        return mContext;
    }
}