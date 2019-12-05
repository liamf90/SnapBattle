package com.liamfarrell.android.snapbattle.di

import android.app.Application
import android.content.Context
import com.liamfarrell.android.snapbattle.data.ReportingsRepository
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule(val context: Application) {
    @Provides
    fun getApplicationContext() : Application {
        return context
    }

}