package com.liamfarrell.android.snapbattle.di

import com.amazonaws.services.lambda.AWSLambda
import com.liamfarrell.android.snapbattle.data.ReportingsRepository
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AWSModule::class])
class RepositoryModuleAdmin {
    @Singleton
    @Provides
    fun reportingsRepository(lambdaFunctionsInterface: LambdaFunctionsInterface): ReportingsRepository {
        return ReportingsRepository(lambdaFunctionsInterface)
    }

}

