package com.liamfarrell.android.snapbattle.di

import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import dagger.Module
import dagger.Provides

@Module
class AmazonDynamoDBClientModule {
    @Provides
    fun amazonDynamoDBClient(credentialsProvider: CognitoCachingCredentialsProvider): AmazonDynamoDBClient {
        return AmazonDynamoDBClient(credentialsProvider);
    }
}