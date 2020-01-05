package com.liamfarrell.android.snapbattle.di

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import dagger.Module
import dagger.Provides
//import retrofit2.Retrofit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import retrofit2.Retrofit
import com.ghedeon.AwsInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.ReportedBattle
import com.liamfarrell.android.snapbattle.model.ReportedComment
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


@Module
class AWSModule{
    @Singleton
    @Provides
    fun provideSnapBattleService(gson: Gson, okHttpClient: OkHttpClient): SnapBattleApiService {
        return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(" https://rmy3h4rt99.execute-api.us-east-1.amazonaws.com/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(SnapBattleApiService::class.java)
    }

    @Provides
    fun provideGson(): Gson{
        val builder = GsonBuilder()
        builder.registerTypeAdapter(Battle::class.java, BattleDeserializer())
        builder.registerTypeAdapter(ReportedBattle::class.java, ReportedBattleDeserializer())
        builder.registerTypeAdapter(ReportedComment::class.java, ReportedCommentDeserializer())
        builder.registerTypeAdapter(Boolean::class.java, CustomBooleanTypeAdapter())
        builder.registerTypeAdapter(Boolean::class.javaPrimitiveType, CustomBooleanTypeAdapter())
        builder.registerTypeAdapter(Date::class.java, GsonUTCDateAdapter())
        return builder.create()
    }

    @Provides
    fun provideOkHttpClient(credentialsProvider : AWSCredentialsProvider) : OkHttpClient {
        val awsInterceptor = AwsInterceptor(credentialsProvider, "execute-api", "us-east-1")
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.HEADERS
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(awsInterceptor)
        httpClient.addInterceptor(logging)
        return httpClient.build()
    }



    @Provides
    fun identityManager() : IdentityManager {
        return IdentityManager.getDefaultIdentityManager()
    }

    @Provides
    fun amazonDynamoDBClient(credentialsProvider: AWSCredentialsProvider): AmazonDynamoDBClient {
        return AmazonDynamoDBClient(credentialsProvider);
    }

//    @Provides
//    @Singleton
//    fun lambdaInterface(factory: LambdaInvokerFactory) : LambdaFunctionsInterface{
//       return  factory.build(LambdaFunctionsInterface::class.java, CustomLambdaDataBinder())
//    }
//
//    @Provides
//    @Singleton
//    fun lambdaFactory(context: Application, cognitoCachingCredentialsProvider: AWSCredentialsProvider ) : LambdaInvokerFactory {
//        return LambdaInvokerFactory(
//                context,
//                Regions.US_EAST_1,
//                cognitoCachingCredentialsProvider)
//    }

    @Provides
    fun credentialsProvider() : AWSCredentialsProvider {
        return AWSMobileClient.getInstance().credentialsProvider
    }
}