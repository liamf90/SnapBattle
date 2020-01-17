package com.liamfarrell.android.snapbattle.di

//import retrofit2.Retrofit
import android.app.Application
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.ghedeon.AwsInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.ReportedBattle
import com.liamfarrell.android.snapbattle.model.ReportedComment
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.*
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


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
    fun amazonDynamoDBClient(credentialsProvider: AWSCredentialsProvider): AmazonDynamoDBClient {
        return AmazonDynamoDBClient(credentialsProvider);
    }


    @Provides
    fun awsCredentialsProvider(mobileClient: AWSMobileClient) : AWSCredentialsProvider{
        return mobileClient
    }

    @Provides
     fun awsMobileClient(application: Application) : AWSMobileClient = runBlocking(Dispatchers.IO) {
        try {
            AWSMobileClient.getInstance().isSignedIn
            return@runBlocking AWSMobileClient.getInstance()
        } catch (e: java.lang.NullPointerException) {
            val aws = getContinuation(application)
            return@runBlocking aws
        }
    }

    private suspend fun getContinuation(application: Application) : AWSMobileClient {
        return suspendCoroutine<AWSMobileClient>{continuation ->
                val awsConfig = AWSConfiguration(application)
                AWSMobileClient.getInstance().initialize(application.applicationContext, awsConfig,
                        object : Callback<UserStateDetails> {
                            override fun onResult(result: UserStateDetails?) {
                                continuation.resume(AWSMobileClient.getInstance())
                            }

                            override fun onError(e: Exception?) {
                                e?.cause?.let { continuation.resumeWith(Result.failure(it)) }
                            }
                        })
            }
    }


}