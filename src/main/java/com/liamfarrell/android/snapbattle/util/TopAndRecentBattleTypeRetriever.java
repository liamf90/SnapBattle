package com.liamfarrell.android.snapbattle.util;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.pjos.RecentBattleNamePOJO;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.RecentBattleResponse;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Liam on 4/05/2017.
 */

public class TopAndRecentBattleTypeRetriever
{
    private Activity mActivity;
    public TopAndRecentBattleTypeRetriever(Activity activity)
    {mActivity = activity;}

    public interface BattleTypeCallbacks
    {
         void onTopBattlesReceived(ArrayList<String> arrayList);
         void onRecentBattlesReceived(ArrayList<String> arrayList);
    }

    public void getTopAndRecentBattles(BattleTypeCallbacks callback)
    {
        getRecentListFromSQL(callback);
        getTopBattlesTask topTask = new getTopBattlesTask(callback);
        topTask.execute();

    }


    private void getRecentListFromSQL(final BattleTypeCallbacks battletypecallback) {
        new GetRecentListFromSqlTask(mActivity, battletypecallback).execute();
    }

    private static class GetRecentListFromSqlTask extends AsyncTask<Void, Void, AsyncTaskResult<RecentBattleResponse>>
    {
        private WeakReference<Activity> activityReference;
        private BattleTypeCallbacks battletypecallback;

        GetRecentListFromSqlTask(Activity activity, BattleTypeCallbacks battletypecallback)
        {
            activityReference = new WeakReference<>(activity);
            this.battletypecallback = battletypecallback;
        }
        @Override
        protected AsyncTaskResult<RecentBattleResponse> doInBackground(Void... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                AWSMobileClient.getInstance());
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
                try {
                    RecentBattleResponse response =  lambdaFunctionsInterface.GetRecentBattleNames();
                    return new AsyncTaskResult<>(response);

                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                }
                catch (AmazonServiceException ase) {
                    // invalid credentials, incorrect AWS signature, etc
                    Log.i("ERROR", ase.getErrorMessage());
                    return new AsyncTaskResult<>(ase);
                }
                catch (AmazonClientException ace) {
                    // Network issue
                    Log.i("ERROR", ace.toString());
                    return new AsyncTaskResult<>(ace);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<RecentBattleResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                Activity activity = activityReference.get();
                if (activity == null || activity.isFinishing()) return;

                RecentBattleResponse result = asyncResult.getResult();
                ArrayList<String> battleTypeList = new ArrayList<String>();

                if (result != null) {
                    for (RecentBattleNamePOJO sug : result.getSqlResult()) {
                        String s = sug.getBattle_name();
                        battleTypeList.add(s);
                    }
                }
                battletypecallback.onRecentBattlesReceived(battleTypeList);
            }

    }



    private List<String> getTopBattlesListFromDynamo(Context context)
    {
        try {
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(
                    AWSMobileClient.getInstance());
            Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
            AttributeValue val = new AttributeValue();
            val.setS("english");
            key.put("Language", val);
            GetItemRequest spec = new GetItemRequest()
                    .withTableName("Top_Battles").withKey(key).withAttributesToGet("BattleType");
            GetItemResult result = ddbClient.getItem(spec);
            Map<String, AttributeValue> res = result.getItem();
            AttributeValue list = res.get("BattleType");
            List<AttributeValue> TopBattleTypeListAttribute = list.getL();
            List<String> TopBattleTypeList = new ArrayList<String>();
            String battleType;
            for (int i = 0; i < TopBattleTypeListAttribute.size(); i++) {
                battleType = TopBattleTypeListAttribute.get(i).getS();
                TopBattleTypeList.add(battleType);
            }
            return TopBattleTypeList;
        }catch(AmazonClientException e)
        {
            //Network error. Return empty list
            return new ArrayList<>();
        }



    }

    private class getTopBattlesTask extends AsyncTask<Void,  ArrayList<String>, ArrayList<String>>
    {
        private BattleTypeCallbacks mCallbacks;
        private getTopBattlesTask(BattleTypeCallbacks callbacks)
        {mCallbacks = callbacks;}

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            return (ArrayList<String>)getTopBattlesListFromDynamo(mActivity);
        }

        @Override
        protected void onPostExecute(ArrayList<String> topBattlesList)
        {
            mCallbacks.onTopBattlesReceived(topBattlesList);
        }
    }
}
