package com.liamfarrell.android.snapbattle.util;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.google.gson.JsonParser;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;

public class HandleLambdaError {
    public static final String ALREADY_FOLLOWING_ERROR = "ALREADY_FOLLOWING_USER_ERROR";
    public void handleError(Exception mError, Context activity, View progressSpinner)
    {
        if (mError instanceof AmazonServiceException)
        {

            Toast.makeText(activity, activity.getResources().getString(R.string.server_error_toast), Toast.LENGTH_LONG).show();
            if (progressSpinner != null) {
                progressSpinner.setVisibility(View.INVISIBLE);
            }
        }
        else if (mError instanceof LambdaFunctionException)
        {
            JsonParser parser = new JsonParser();
            if (parser.parse(((LambdaFunctionException) mError).getDetails()).getAsJsonObject().get("errorType") != null) {
                String errorType = parser.parse(((LambdaFunctionException) mError).getDetails()).getAsJsonObject().get("errorType").getAsString();
                if (errorType.equals(LambdaFunctionsInterface.UPGRADE_REQUIRED_ERROR_MESSAGE))
                {
                    Toast.makeText(activity, R.string.upgrade_required_toast_message, Toast.LENGTH_LONG).show();
                    if (progressSpinner != null) {
                        progressSpinner.setVisibility(View.INVISIBLE);
                    }
                }
                else if (errorType.equals(ALREADY_FOLLOWING_ERROR))
                {
                    Toast.makeText(activity, R.string.already_following_error, Toast.LENGTH_LONG).show();
                    if (progressSpinner != null) {
                        progressSpinner.setVisibility(View.INVISIBLE);
                    }
                }
                else
                {
                    Toast.makeText(activity, R.string.server_error_toast, Toast.LENGTH_LONG).show();
                    if (progressSpinner != null) {
                        progressSpinner.setVisibility(View.INVISIBLE);
                    }
                }
            }
            else
            {
                Toast.makeText(activity, R.string.server_error_toast, Toast.LENGTH_LONG).show();
                if (progressSpinner != null) {
                    progressSpinner.setVisibility(View.INVISIBLE);
                }
            }
        }
        else if (mError instanceof AmazonClientException)
        {

            Toast.makeText(activity, R.string.no_internet_connection_toast, Toast.LENGTH_LONG).show();
            if (progressSpinner != null) {
                progressSpinner.setVisibility(View.INVISIBLE);
            }
        }
    }

}
