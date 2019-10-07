package com.liamfarrell.android.snapbattle.di

import android.app.Application
import android.content.Context
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AWSModule{
    @Provides
    fun amazonDynamoDBClient(credentialsProvider: AWSCredentialsProvider): AmazonDynamoDBClient {
        return AmazonDynamoDBClient(credentialsProvider);
    }

    @Provides
    @Singleton
    fun lambdaInterface(factory: LambdaInvokerFactory) : LambdaFunctionsInterface{
       return  factory.build(LambdaFunctionsInterface::class.java, CustomLambdaDataBinder())
    }

    @Provides
    @Singleton
    fun lambdaFactory(context: Application, cognitoCachingCredentialsProvider: AWSCredentialsProvider ) : LambdaInvokerFactory {
        return LambdaInvokerFactory(
                context,
                Regions.US_EAST_1,
                cognitoCachingCredentialsProvider)
    }

    @Provides
    @Singleton
    fun credentialsProvider() : AWSCredentialsProvider {
        return AWSMobileClient.getInstance().credentialsProvider
//        return CognitoCachingCredentialsProvider(
//                context, /* get the context for the current callbacks */
//                "us-east-1:e6478f31-2dbe-4ad8-aadd-b4964691350c", /* Identity Pool ID */
//                Regions.US_EAST_1          /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
//        );
    }
}