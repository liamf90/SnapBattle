package com.liamfarrell.android.snapbattle.caches;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.liamfarrell.android.snapbattle.model.User;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FollowingRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetUsersRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An Singleton cache which stores all the users that the current user follows.
 * When the cache is first loaded (no cache exists), the users following list is retrieved in full from the mysql server.
 * The cache updates subsequently by comparing the caches saved following updated count to the aws dynamo nosql field 'Following_updated_count' for the CognitoId of the user.
 * If there is a difference the cache retrieves the commands to ADD, REMOVE, UPDATE_USERNAME or UPDATE_NAME for users
 *
 * This cache is used to check if the user is still following an opponent on the following battles feed. For example if an user unfollows someone, this cache will update and remove
 * the user from the following cache.
 * and an check will be done when looking at the following battles feed if the user is still following the user, since they are not following anymore, the battle will not be shown.
 *
 * The cache is stored using FollowerFile which serializes the data using normal java serialization file saving
 */

public class FollowingUserCache {
	private static final String ACTION_ADD = "ADD";
	private static final String ACTION_REMOVE = "REMOVE";
	private static final String ACTION_UPDATE_USERNAME = "UPDATE_USERNAME";
	private static final String ACTION_UPDATE_NAME = "UPDATE_NAME";
	private static final String TAG = "FollowerCache";
	
	private static FollowingUserCache sFollowingUserCache;
	private ConcurrentHashMap<String, User> mFollowerCognitoIDMap;
	private FollowingUserFile mFollowingUserFile;

    private Date mFollowingUserLastUpdated;
	private int mFollowingUpdatedCount;

	private boolean isUpdating = false;

	
	public interface CacheLoadCallbacks
	{
		 void onLoadedFromSQL();
		 void onLoadedFromFile();
		 void onCacheAlreadyLoaded();
		 void onUpdated();
		 void onNoUpdates();
	}

	public static void closeCache(){
		sFollowingUserCache = null;
	}
	
	
	public FollowingUserCache(Context context, CacheLoadCallbacks callbacks)
	{
		loadList(context, callbacks);
	}
	
	public static FollowingUserCache get(Context context, CacheLoadCallbacks callbacks)
	{
		if (sFollowingUserCache == null)
		{
			sFollowingUserCache = new FollowingUserCache(context, callbacks);
		}
		else
		{
			if (callbacks != null)
			{
				callbacks.onCacheAlreadyLoaded();
			}
		}
		return sFollowingUserCache;
	}


	public void updateCache(Context context, CacheLoadCallbacks callbacks){
	    if (!isUpdating) {
            checkForFollowersListChanges(context, callbacks);
        }
    }


	public boolean isCognitoIDFollower(String cognitoID)
	{
		Log.i(TAG, "Does it contain CognitoId: " + cognitoID + "? " +  mFollowerCognitoIDMap.containsKey(cognitoID));
		return mFollowerCognitoIDMap.containsKey(cognitoID);
	}


	public boolean isFacebookUserIDFollower(String FacebookUserID)
	{
		for (Map.Entry<String, User> entry : mFollowerCognitoIDMap.entrySet())
		{
			if (entry.getValue().getFacebookUserId().equals(FacebookUserID))
			{
				return true;
			}

		}
		return false;
	}

	
	public Collection<User> getFollowerList()
	{
	
		return  mFollowerCognitoIDMap.values();
	}
	
	
	private void loadList(final Context context, final CacheLoadCallbacks callbacks) 
	{
		mFollowerCognitoIDMap = new ConcurrentHashMap<String, User>();
		try
		{
			mFollowingUserFile = new FollowingUserFile(context);
			mFollowerCognitoIDMap = (ConcurrentHashMap<String, User>) mFollowingUserFile.getFollowerCognitoIDMap();
			mFollowingUpdatedCount = mFollowingUserFile.getUpdateCount();
			mFollowingUserLastUpdated = mFollowingUserFile.getLastTimeUpdated();
			
			if (callbacks != null)
			{
				callbacks.onLoadedFromFile();
			}

			isUpdating = true;
			checkForFollowersListChanges(context,callbacks);
			 
		}
		catch (FileNotFoundException e)
		{
			
			
				Log.i(TAG, "No file. Retrieving from sql server");
				isUpdating = true;
				Runnable runnable = new Runnable() {
					public void run() 
					{
					    try {
                            mFollowingUpdatedCount = getFollowingActionUpdateCount(context);
                        }
                        catch (AmazonClientException e)
                        {
                            //Network error. Cant load list
                            return;
                        }
						getFullFollowerListMYSQL(context, callbacks);
					}};
				Thread t1 = new Thread(runnable);
				t1.start();
			
		}
		catch (IOException e) 
		{
		e.printStackTrace();
		}
		
			
	}

	public void updateSignedUrl(Context context, String cognitoID, int profilePicCount, String newSignedUrl)
    {
        User f = mFollowerCognitoIDMap.get(cognitoID);
        f.setProfilePicCount(profilePicCount);
        f.setProfilePicSignedUrl(newSignedUrl);
        mFollowerCognitoIDMap.put(cognitoID, f);
        FollowingUserFile file = new FollowingUserFile(context, mFollowerCognitoIDMap, mFollowingUpdatedCount, mFollowingUserLastUpdated);
        file.saveListToFile(context);

    }

    public User getFollowing(String cognitoID)
    {
        return mFollowerCognitoIDMap.get(cognitoID);
    }




    public Date getFollowingUserCacheLastUpdated() {
        return mFollowingUserLastUpdated;
    }

    private void getFullFollowerListMYSQL(final Context context, final CacheLoadCallbacks callbacks) {
		FollowingRequest request = new FollowingRequest();
		request.setShouldGetProfilePic(true);
		new GetFullFollowerListMysqlTask(context, this, callbacks).execute(request);

	}

	private static class GetFullFollowerListMysqlTask extends AsyncTask<FollowingRequest, Void, AsyncTaskResult<ResponseFollowing>>
	{
		private  CacheLoadCallbacks callbacks;
		private WeakReference<Context> contextReference;
		private WeakReference<FollowingUserCache> classReference;

		GetFullFollowerListMysqlTask(Context context, FollowingUserCache thisClass, CacheLoadCallbacks callbacks)
		{
			contextReference = new WeakReference<>(context);
			classReference = new WeakReference<>(thisClass);
			this.callbacks = callbacks;
		}
		@Override
		protected AsyncTaskResult<ResponseFollowing> doInBackground(FollowingRequest... params) {
		// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
		LambdaInvokerFactory factory = new LambdaInvokerFactory(
				contextReference.get().getApplicationContext(),
				Regions.US_EAST_1,
				FacebookLoginFragment.getCredentialsProvider(contextReference.get()));

		// Create the Lambda proxy object with default Json data binder.
		// You can provide your own data binder by implementing
		// LambdaDataBinder
		final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);

				try {
                    ResponseFollowing response = lambdaFunctionsInterface.GetFollowing(params[0]);
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
			protected void onPostExecute(AsyncTaskResult<ResponseFollowing> asyncResult) {
				FollowingUserCache thisClass = classReference.get();
				Context context = contextReference.get();
				if (thisClass == null) return;
				if (context == null) return;


                ResponseFollowing result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    if (asyncResult.getError() instanceof AmazonClientException)
                    {
                        return;
                    }
                    else if (asyncResult.getError() instanceof AmazonServiceException || asyncResult.getError() instanceof LambdaFunctionException)
                    {

                        return;
                    }
                }

				for (User fol : result.getSqlResult())
				{
					thisClass.mFollowerCognitoIDMap.put(fol.getCognitoId(),fol);
				}

                thisClass.mFollowingUserLastUpdated = Calendar.getInstance().getTime();
				FollowingUserFile file = new FollowingUserFile(context, thisClass.mFollowerCognitoIDMap, thisClass.mFollowingUpdatedCount, thisClass.mFollowingUserLastUpdated);
				file.saveListToFile(context);
				if (callbacks != null)
				{
					callbacks.onLoadedFromSQL();
				}

				thisClass.isUpdating = false;

			}

	}

	

	
	private int getFollowingActionUpdateCount(Context context) throws AmazonClientException
	{
		{
			Log.i(TAG, "Following action update count dynamo");
			AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(
					FacebookLoginFragment.getCredentialsProvider(context));

			Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
			AttributeValue val = new AttributeValue();
			val.setS(FacebookLoginFragment.getCredentialsProvider(context).getIdentityId());
			key.put("CognitoID", val);
			String projectionExpression = "Following_updated_count";
			GetItemRequest spec = new GetItemRequest()
					.withProjectionExpression(projectionExpression)
					.withTableName("Battle_Activity_Feed").withKey(key);
			Log.i(TAG, "Following action update Get result");


            int update_count = 0;
            Map<String, AttributeValue> res = null;
			try {
                GetItemResult result = ddbClient.getItem(spec);
                Log.i(TAG, "Following action update Get result");
                res = result.getItem();
            }
            catch (AmazonClientException e)
            {
                //Nework Error
                throw e;
            }


			if (res != null)
			{
				AttributeValue item_count = res.get("Following_updated_count");
				if (item_count != null)
				{
					 update_count = Integer.parseInt(item_count.getN());
				}
				else {
					update_count = 0;
				}

				return update_count;
			}
			else
			{
				return 0;
			}
		}
	}
	
	
	private void checkForFollowersListChanges(final Context context,final CacheLoadCallbacks callbacks ) {

	    isUpdating = true;
		Runnable runnable = new Runnable() {
			public void run() 
			{

			    try {
                    final int dynamoFollowingActionUpdateCount = getFollowingActionUpdateCount(context);
                    if (dynamoFollowingActionUpdateCount != mFollowingUpdatedCount)
                    {
                        //There is follower changes.
                        //Get update list
                        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(
                                FacebookLoginFragment.getCredentialsProvider(context));
                        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
                        AttributeValue val = new AttributeValue();
                        val.setS(FacebookLoginFragment.getCredentialsProvider(context).getIdentityId());
                        key.put("CognitoID", val);

                        int startIndex = 0;
                        int endIndex = dynamoFollowingActionUpdateCount- mFollowingUpdatedCount - 1;


                        String projectionExpression = "";
                        for (int i = startIndex; i <= endIndex; i++)
                        {
                            projectionExpression = projectionExpression + "Following_Action_List[" + i + "]";
                            if (i != endIndex) {
                                projectionExpression = projectionExpression + ",";
                            }
                        }
                        Log.i(TAG, "Projection Expression: " + projectionExpression);
                        GetItemRequest spec = new GetItemRequest()
                                .withProjectionExpression(projectionExpression)
                                .withTableName("Battle_Activity_Feed").withKey(key);

                        GetItemResult result = ddbClient.getItem(spec);
                        Map<String, AttributeValue> res = result.getItem();
                        Log.i(TAG, "Result: " + result.toString());
                        AttributeValue list = res.get("Following_Action_List");
                        final List<AttributeValue> ActionList = list.getL();

                        Map<String, AttributeValue> actionMap;
                        String action;
                        String cognitoID;
                        //Get all the cognitoId's of users to add, so we can get the information to retrieve from mysql
                        ArrayList<String> addFollowersCognitoIDList = new ArrayList<String>();
                        for (int i =0; i< ActionList.size(); i++)
                        {
                            actionMap = ActionList.get(i).getM();
                            action = actionMap.get("ACTION").getS();
                            cognitoID = actionMap.get("COGNITO_ID").getS();
                            Log.i(TAG, "action: " + action + ", CognitoId: " + cognitoID);
                            if (action.equals(ACTION_ADD))
                            {
                                addFollowersCognitoIDList.add(cognitoID);
                            }
                        }


                        GetUsersRequest request = new GetUsersRequest();
                        request.setUserCognitoIDList(addFollowersCognitoIDList);

                        if (addFollowersCognitoIDList.size() > 0)
                        {
                            new GetFollowerTask(context, sFollowingUserCache,   ActionList, dynamoFollowingActionUpdateCount,  callbacks).execute(request);
                        }
                        else
                        {
                            applyUpdates(context, ActionList, null, dynamoFollowingActionUpdateCount,callbacks);
                        }




                    }
                    else
                    {
                        isUpdating = false;
                        if (callbacks != null)
                        {
                            callbacks.onNoUpdates();
                        }
                        Log.i("FollowerCache", "There are no follower updates in dynamo");
                    }
                }
                catch (AmazonClientException e)
                {
                    //Network error. Abort

                }



				
			}};
			
			Thread thread = new Thread(runnable);
			thread.start();
		
			
		
	
	}

	private static class GetFollowerTask extends  AsyncTask<GetUsersRequest, Void,  AsyncTaskResult<GetUsersResponse>>
    {
        private WeakReference<Context> contextReference;
        private WeakReference<FollowingUserCache> classReference;
        private List<AttributeValue> actionList;
        private int dynamoFollowingActionUpdateCount;
        private CacheLoadCallbacks callbacks;


        GetFollowerTask(Context context, FollowingUserCache thisClass, List<AttributeValue> actionList,
						int dynamoFollowingActionUpdateCount, CacheLoadCallbacks callbacks)
        {
            contextReference = new WeakReference<>(context);
            classReference = new WeakReference<>(thisClass);
            this.actionList =actionList;
            this.dynamoFollowingActionUpdateCount =dynamoFollowingActionUpdateCount;
            this.callbacks =callbacks;
        }

        @Override
        protected  AsyncTaskResult<GetUsersResponse> doInBackground(GetUsersRequest... params) {
            // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
            LambdaInvokerFactory factory = new LambdaInvokerFactory(
                    contextReference.get().getApplicationContext(),
                    Regions.US_EAST_1,
                    FacebookLoginFragment.getCredentialsProvider(contextReference.get()));

            // Create the Lambda proxy object with default Json data binder.
            // You can provide your own data binder by implementing
            // LambdaDataBinder
            final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
            try {
                GetUsersResponse response = lambdaFunctionsInterface.GetUsers(params[0]);
                return new AsyncTaskResult<>(response);

            } catch (LambdaFunctionException lfe) {
                Log.i("ERROR", lfe.getDetails());
                Log.i("ERROR",lfe.getStackTrace().toString());
                lfe.printStackTrace();

                return new AsyncTaskResult<>(lfe);                                    }
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
        protected void onPostExecute( AsyncTaskResult<GetUsersResponse> asyncResult) {
            FollowingUserCache thisClass = classReference.get();
            Context context = contextReference.get();
            if (thisClass == null) return;
            if (context == null) return;

            GetUsersResponse result = asyncResult.getResult();
            if (asyncResult.getError() != null)
            {
                if (asyncResult.getError() instanceof AmazonClientException)
                {
                    return;
                }
                else if (asyncResult.getError() instanceof AmazonServiceException || asyncResult.getError() instanceof LambdaFunctionException)
                {
                    return;
                }

            }


            ConcurrentHashMap<String, User> followerMap = new ConcurrentHashMap<String, User>();
            User f;
            for (User user: result.getSqlResult())
            {
                followerMap.put(user.getCognitoId(), user);
            }
            thisClass.applyUpdates(context,actionList, followerMap, dynamoFollowingActionUpdateCount,callbacks);
            //Toast.makeText(getActivity(), "Error: " + result.getSqlResult().getBattleid()), Toast.LENGTH_LONG).show();
            //Toast.makeText(getActivity(), "Battleid: " + result.sql_result.get(0).battleid, Toast.LENGTH_SHORT).show();

        }


    }
	
	private void applyUpdates(final Context context, final List<AttributeValue> ActionList, final ConcurrentHashMap<String, User> followerMap, final int dynamoFollowingActionUpdateCount, final CacheLoadCallbacks callbacks)
	{
		
		//Apply updates in main thread
		Handler mainHandler = new Handler(context.getMainLooper()); 
		 
		Runnable myRunnable = new Runnable() {
		    @Override
		    public void run() 
		    {
		    	Map<String, AttributeValue> actionMap = new HashMap<String, AttributeValue>() {
                };
				String action;
				String cognitoID;
				for (int i = ActionList.size() - 1; i >= 0; i--) 
				{
                    actionMap.clear();
                    actionMap = ActionList.get(i).getM();
                    Log.i("FollowerCache", "Action: " + actionMap.toString());
					action = actionMap.get("ACTION").getS();
					cognitoID = actionMap.get("COGNITO_ID").getS();

					switch (action) {
						case ACTION_ADD:
							mFollowerCognitoIDMap.put(cognitoID, followerMap.get(cognitoID));
							break;
						case ACTION_REMOVE:
							mFollowerCognitoIDMap.remove(cognitoID);
							break;
						case ACTION_UPDATE_NAME: {
							String newName = actionMap.get("NAME").getS();
							User followerToUpdate = mFollowerCognitoIDMap.get(cognitoID);
							followerToUpdate.setFacebookName(newName);

							//mFollowerCognitoIDMap.replace(cognitoID, followerToUpdate);
							mFollowerCognitoIDMap.put(cognitoID, followerToUpdate);
							break;
						}
						case ACTION_UPDATE_USERNAME: {
							String newUsername = actionMap.get("USERNAME").getS();
							Log.i("FollowerCache", "Size: " + mFollowerCognitoIDMap.size() + ", " + mFollowerCognitoIDMap.keySet().toString());
							User followerToUpdate = mFollowerCognitoIDMap.get(cognitoID);
							followerToUpdate.setUsername(newUsername);
							//mFollowerCognitoIDMap.replace(cognitoID, followerToUpdate);
							mFollowerCognitoIDMap.put(cognitoID, followerToUpdate);
							break;
						}
					}
					
					mFollowingUpdatedCount = dynamoFollowingActionUpdateCount;
				}
				mFollowingUserLastUpdated = Calendar.getInstance().getTime();
				mFollowingUserFile = new FollowingUserFile(context, mFollowerCognitoIDMap, mFollowingUpdatedCount, mFollowingUserLastUpdated);
				mFollowingUserFile.saveListToFile(context);
				if (callbacks != null)
				{
					callbacks.onUpdated();
				}

				isUpdating = false;
		    	
		    	
		    	
		    } 
		}; 
		mainHandler.post(myRunnable);

	}
	
	 
	
	
	
	
	
}
