package com.liamfarrell.android.snapbattle.util

import android.content.Context
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException
import com.google.gson.JsonParser
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.util.HandleLambdaError.ALREADY_FOLLOWING_ERROR
import java.util.*



fun getErrorMessage(context: Context, error: Exception): String {
    return when (error) {
        is AmazonServiceException -> context.getString(R.string.server_error_toast)
        is LambdaFunctionException -> {
            val parser = JsonParser()
            if (parser.parse(error.details).asJsonObject.get("errorType") != null) {
                val errorType = parser.parse(error.details).asJsonObject.get("errorType").asString
                if (errorType == LambdaFunctionsInterface.UPGRADE_REQUIRED_ERROR_MESSAGE) {
                    return context.getString(R.string.upgrade_required_toast_message)
                } else if (errorType == ALREADY_FOLLOWING_ERROR) {
                    return context.getString(R.string.already_following_error)
                } else {
                    return context.getString(R.string.server_error_toast)
                }
            } else {
                return context.getString(R.string.server_error_toast)
            }
        }
        is AmazonClientException -> context.getString(R.string.no_internet_connection_toast)
        is CustomError -> error.getErrorToastMessage(context)
        else -> context.getString(R.string.server_error_toast)
    }
}

fun getErrorMessage(context: Context, error: Throwable): String {
    return when (error) {
        is AmazonServiceException -> context.getString(R.string.server_error_toast)
        is LambdaFunctionException -> {
            val parser = JsonParser()
            if (parser.parse(error.details).asJsonObject.get("errorType") != null) {
                val errorType = parser.parse(error.details).asJsonObject.get("errorType").asString
                if (errorType == LambdaFunctionsInterface.UPGRADE_REQUIRED_ERROR_MESSAGE) {
                    return context.getString(R.string.upgrade_required_toast_message)
                } else if (errorType == ALREADY_FOLLOWING_ERROR) {
                    return context.getString(R.string.already_following_error)
                } else {
                    return context.getString(R.string.server_error_toast)
                }
            } else {
                return context.getString(R.string.server_error_toast)
            }
        }
        is AmazonClientException -> context.getString(R.string.no_internet_connection_toast)
        is CustomError -> error.getErrorToastMessage(context)
        else -> context.getString(R.string.generic_error_toast)
    }
}


abstract class CustomError() : Exception(){
    abstract fun getErrorToastMessage(context: Context) : String
}

class AlreadyFollowingError() : CustomError(){
    override fun getErrorToastMessage(context: Context): String {
        return context.getString(R.string.need_accept_permission_user_friends)
    }
}

class BannedError(val banTimeEnd : Date) : CustomError(){
    override fun getErrorToastMessage(context: Context): String {
        return context.getString(R.string.banned_toast, banTimeEnd.toString())
    }
}
