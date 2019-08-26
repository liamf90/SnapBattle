package com.liamfarrell.android.snapbattle.caches;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import com.facebook.AccessToken;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.BattleIDSave;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetFriendsBattlesRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetFriendsBattlesRequestOld;
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetFriendsBattlesResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An Singleton cache for filling and updating the Following Battles Feed.
 * This class gets the battle id list of the following topBattles feed from the dynamo server (primary id = current users cognito id) and then retrieves the topBattles from the mysql server.
 * Checking for updates is done by comparing the last battle count on the aws dynamo table to the stored battle count. New topBattles are then added to the top of the cache list.
 * This class can also check and load more topBattles such as when the list is scrolled to the bottom.
 *
 * An Full Feed Count is also compared, if it is different, the feed is fully reloaded from mysql (this occurs when the feed is reordered such as when an new user is followed.)
 *
 * The cache is stored using FollowingUserFile which serializes the data using normal java serialization file saving
 */


public class FollowingBattleCache {
	private static final int LOAD_MORE_BATTLES_AMOUNT = 5;
	public static final String TAG = "FollowingBattleCache";
	private static boolean isLoaded;
	private static FollowingBattleCache sFollowingBattleCache;

	private LinkedList<BattleIDSave> mBattleIDList;
	private ConcurrentHashMap<Integer, Battle> mFriendBattleMap;
	private Date mLastTimeUpdated;
	private int mLastFriendBattleCount;
	private int mFullFeedUpdatesCount;

	private Handler mainHandler = new Handler(Looper.getMainLooper());

	private ExecutorService mExecutorService;


	public interface UpdateCallbacks
	{
		public void onNewBattles(List<BattleIDSave> battleIDList);
		public void onFullFeedUpdate(List<BattleIDSave> battleIDList);
		public void onUpdated(List<Integer> updatedBattleIDList);
	}
	
	public interface CacheLoadCallbacks {
		

		public void OnNoFileForUser();

		public void OnCacheLoaded(List<BattleIDSave> battleIDList);

		public void OnNoBattlesInFeed();

		public void OnNoFileLoad(List<BattleIDSave> battleIDList);
	}



	public interface LoadMoreBattlesCallback {
		void onMoreBattlesLoaded(List<BattleIDSave> moreBattles);

		void ThereIsNoMoreBattles();
	}

	public int getBattleIDFromPosition(int position)
	{
		return mBattleIDList.get(position).getBattleID();
	}

	private FollowingBattleCache(Context context) {

		mBattleIDList = new LinkedList<BattleIDSave>();
		mFriendBattleMap = (new ConcurrentHashMap<Integer, Battle>());
		isLoaded = false;
		mExecutorService = newFixedThreadPool(1);
	}

	public static FollowingBattleCache get(Context context) {
		if (sFollowingBattleCache == null)
		{
            Log.i(TAG, "sFollowingBattleCache == null");
			sFollowingBattleCache = new FollowingBattleCache(context);
		}
		else
        {
            Log.i(TAG, "sFollowingBattleCache != null");
        }
		return sFollowingBattleCache;
	}

	public Battle getFollowingBattle(int battleIndex) {

		return mFriendBattleMap.get(battleIndex);
	}

	public static void closeCache(){
		sFollowingBattleCache = null;
	}


	public LinkedList<BattleIDSave> getCacheBattleIDList() {
		return mBattleIDList;
	}

	public boolean isLoaded()
	{
		return isLoaded;
	}

	public void loadList(final Context context,
						 final CacheLoadCallbacks callback, final UpdateCallbacks updateCallback) {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				loadListGo(context, callback, updateCallback, false);
			}
		};
		mExecutorService.submit(r);


	}

    public Date getLastTimeUpdated() {
        return mLastTimeUpdated;
    }

    private void loadListGo(final Context context,
                            final CacheLoadCallbacks callback, final UpdateCallbacks updateCallback, final boolean fullListLoad) {



				try {
					Log.i(TAG, "Loading file..");
					//IF sFRIENDBATTLECACHE ALREADY EXISTS... DO NOT LOAD FROM FILE.

					mBattleIDList.clear();
					mFriendBattleMap.clear();
					FollowingBattleCacheFile file = new FollowingBattleCacheFile(context);

					mFriendBattleMap.putAll(file.getFriendBattleMap());
					mBattleIDList.addAll(file.getBattleList());

					Log.i(TAG, mFriendBattleMap.toString());
					mLastTimeUpdated = file.getLastTimeUpdated();
					mLastFriendBattleCount = file.getLastFriendBattleCount();
					mFullFeedUpdatesCount = file.getFullFeedUpdateCount();
					Log.i(TAG, "Cache Loaded");
					isLoaded = true;
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							callback.OnCacheLoaded(mBattleIDList);
						}
					});

					updateList(context, updateCallback);
					
				} catch (FileNotFoundException | InvalidClassException e) {
					Log.i(TAG, "No file. Retrieving from server");
					if (callback != null) {
						mainHandler.post(new Runnable() {
							@Override
							public void run() {
								callback.OnNoFileForUser();
							}
						});


					}
					// Load from dynamo DB
                    try {
                        List<Integer> arrayList = loadListFromDynamo(context, 0,
                                FollowingBattleCacheFile.FILE_MAX_CAPACITY - 1);
                        int battlesCountDynamo = getBattlesCountDynamo(context);
                        mFullFeedUpdatesCount = getFeedFullUpdateCountDynamo(context);


                        if (arrayList.size() == 0) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.OnNoBattlesInFeed();
                                }
                            });
                        }


                        addFriendBattlesFromSQLtoTop(context, true, fullListLoad,
                                (ArrayList<Integer>) arrayList,
                                battlesCountDynamo, callback, updateCallback);
                        isLoaded = true;
                    }
                    catch (AmazonClientException ace)
                    {
                        //Network error
                        //Do not load from server.
                        //TODO return callback onError. Display error snackbar
                    }


				} catch (IOException e) {
					e.printStackTrace();
				}



		//Thread mythread = new Thread(runnable);
		//mythread.start();


	}



	public void updateList(final Context context, final UpdateCallbacks callback)
	{
		Runnable r = new Runnable() {
			@Override
			public void run() {

			    try {
                    int fullFeedCountDynamo = getFeedFullUpdateCountDynamo(context);
                    // get the last battle from the dynamodb feed. Compare it to the last
                    // known in the cache.


                    if (fullFeedCountDynamo != mFullFeedUpdatesCount) {

                        //download the full feed again
                        FollowingBattleCacheFile.deleteFile(context);

                        //Callback..
                        loadListGo(context, null, callback, true);

                    } else {
                        if (!isThereMoreBattles(context)) {
                            // NO NEW BATTLES
                            Log.i(TAG, "No more topBattles from cache to dynamo list");
                            // callback.OnNoNewBattles();

                            updateFriendBattlesFromSQL(context, mBattleIDList,
                                    callback);

                        } else {
                            Log.i(TAG,
                                    "More topBattles from dynamo list. Getting these..");
                            // to update list = old topBattles minus new topBattles
                            List<BattleIDSave> oldBattlesList = mBattleIDList;

                            // NEW BATTLES
                            // get new list
                            int battlesCountDynamo = getBattlesCountDynamo(context);


                            List<Integer> newBattlesList = loadListFromDynamo(
                                    context, 0, battlesCountDynamo
                                            - mLastFriendBattleCount - 1);

                            // add list to mFriendsBattleFile
                            addFriendBattlesFromSQLtoTop(context, false, false,
                                    (ArrayList<Integer>) newBattlesList,
                                    battlesCountDynamo, null, callback);


                            // Update old topBattles
                            updateFriendBattlesFromSQL(context, oldBattlesList,
                                    callback);

                        }
                    }
                } catch (AmazonClientException e)
                {
                    //Network error
                    //Abort
                }
			}
		};

		mExecutorService.submit(r);


	}
	
	private void updateFriendBattlesFromSQL(final Context context,
			List<BattleIDSave> battleIDList, final UpdateCallbacks callback) {
		// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
		LambdaInvokerFactory factory = new LambdaInvokerFactory(
				context.getApplicationContext(),
				Regions.US_EAST_1,
				FacebookLoginFragment.getCredentialsProvider(context));

		// Create the Lambda proxy object with default Json data binder.
		// You can provide your own data binder by implementing
		// LambdaDataBinder
		final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class,  new CustomLambdaDataBinder());
		if (battleIDList.size() > 0) {
			GetFriendsBattlesRequestOld battle = new GetFriendsBattlesRequestOld();
			List<Integer> justBattleIDList = new ArrayList<Integer>();
			for (BattleIDSave b : battleIDList){
			    justBattleIDList.add(b.getBattleID());
            }
			battle.setBattleIDList(justBattleIDList);
			if (mLastTimeUpdated != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				battle.setLastUpdatedDate(sdf.format(mLastTimeUpdated));
                Log.i("FollowingBattleCache", "battle.lasttimeupdated: " + sdf.format(mLastTimeUpdated));
			}

			try {
                Log.i("FollowingBattleCache", "Updating cache.");
				GetFriendsBattlesResponse response = lambdaFunctionsInterface.GetFriendsBattles(battle);
				ArrayList<Battle> resultList = getFriendBattleListFromSQLResult(response);
				final ArrayList<Integer> resultBattleIDList = new ArrayList<>();
				for (int i = 0; i < resultList.size(); i++) {
                    Log.i("FollowingBattleCache", "Updating battle" + resultList.get(i).getBattleId());
					resultBattleIDList.add(resultList.get(i).getBattleId());
					mFriendBattleMap.put(resultList.get(i).getBattleId(),
							resultList.get(i));

				}
				Calendar cal = Calendar.getInstance();
				cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date timeNow = cal.getTime();
				mLastTimeUpdated = timeNow;

				saveToFile(context);
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
						Log.i(TAG, "On Updated");
						callback.onUpdated(resultBattleIDList);
					}
				});


			} catch (LambdaFunctionException lfe) {
				Log.i("ERROR", lfe.getDetails());
				Log.i("ERROR", lfe.getStackTrace().toString());
				lfe.printStackTrace();


			} catch (AmazonServiceException ase) {
				// invalid credentials, incorrect AWS signature, etc
				Log.i("ERROR", ase.getErrorMessage());

			} catch (AmazonClientException ace) {
				// Network issue
				Log.i("ERROR", ace.toString());

			}

		}
		else
		{
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					Log.i(TAG, "On Updated");
					callback.onUpdated(new ArrayList<Integer>());
				}
			});
		}



	}

	private void addFriendBattlesFromSQLtoTop(final Context context,
			final boolean noFileLoad, final boolean fullFeedUpdate, final ArrayList<Integer> battleIDList,
			final int totalFriendBattleCount, final CacheLoadCallbacks callback,
			final UpdateCallbacks updateCallback) {

		mLastFriendBattleCount = totalFriendBattleCount;

		if (battleIDList.size() == 0)
		{
			saveToFile(context);
		}
		else
		{
			// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
			LambdaInvokerFactory factory = new LambdaInvokerFactory(
					context.getApplicationContext(),
					Regions.US_EAST_1,
					FacebookLoginFragment.getCredentialsProvider(context));

			// Create the Lambda proxy object with default Json data binder.
			// You can provide your own data binder by implementing
			// LambdaDataBinder
			final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class, new CustomLambdaDataBinder());
			GetFriendsBattlesRequest battle = new GetFriendsBattlesRequest();
			battle.setBattleIDList(battleIDList);
			try {
				GetFriendsBattlesResponse response =  lambdaFunctionsInterface.GetFriendsBattles(battle);
				final ArrayList<Battle> resultList = getFriendBattleListFromSQLResult(response);

                // add topBattles to map
                for (int i = 0; i < resultList.size(); i++) {
                    mFriendBattleMap.put(resultList.get(i).getBattleId(),
                            resultList.get(i));

                }
				// add topBattles to start of battle id
				for (int i = battleIDList.size() - 1; i >= 0; i--) {

					mBattleIDList.addFirst(new BattleIDSave(battleIDList
							.get(i)));
				}


				// Save List
				saveToFile(context);
				// if Fragment.Callbacks is not null, callback with
				// battleIDList.
				if (fullFeedUpdate){
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							updateCallback.onFullFeedUpdate(mBattleIDList);
						}
					});

				}
				if (noFileLoad) {
					if (callback != null) {
						Log.i(TAG, "Callback: On No File Load");
						mainHandler.post(new Runnable() {
							@Override
							public void run() {
								callback.OnNoFileLoad(mBattleIDList);
							}
						});

					}
				} else {
					Log.i(TAG, "Callback: On New Battles");
					final List<BattleIDSave> returnedBattleIDList = new ArrayList<>();
					for (Integer b : battleIDList){
                        returnedBattleIDList.add(new BattleIDSave(b));
                    }
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							updateCallback.onNewBattles(returnedBattleIDList);
						}
					});


				}

			} catch (LambdaFunctionException lfe) {
				Log.i("ERROR", lfe.getDetails());
				Log.i("ERROR",lfe.getStackTrace().toString());
				lfe.printStackTrace();


			}
			catch (AmazonServiceException ase) {
				// invalid credentials, incorrect AWS signature, etc
				Log.i("ERROR", ase.getErrorMessage());

			}
			catch (AmazonClientException ace) {
				// Network issue
				Log.i("ERROR", ace.toString());

			}
		}







	}




	private void addFriendBattlesFromSQLtoEnd(final Context context,
			final ArrayList<Integer> battleIDList,
			final LoadMoreBattlesCallback callback) {


		// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
		LambdaInvokerFactory factory = new LambdaInvokerFactory(
				context.getApplicationContext(),
				Regions.US_EAST_1,
				FacebookLoginFragment.getCredentialsProvider(context));

		// Create the Lambda proxy object with default Json data binder.
		// You can provide your own data binder by implementing
		// LambdaDataBinder
		final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class, new CustomLambdaDataBinder());
		GetFriendsBattlesRequest battle = new GetFriendsBattlesRequest();
		battle.setBattleIDList(battleIDList);

		try {
			GetFriendsBattlesResponse response =  lambdaFunctionsInterface.GetFriendsBattles(battle);
			ArrayList<Battle> resultList = getFriendBattleListFromSQLResult(response);

            // add topBattles to map
            for (int i = 0; i < resultList.size(); i++) {
                mFriendBattleMap.put(resultList.get(i).getBattleId(),
                        resultList.get(i));

            }

            final List<BattleIDSave> callbackBattleIDList = new ArrayList<>();
            for (Integer battleID: battleIDList){
                mBattleIDList.add(new BattleIDSave(battleID));
                callbackBattleIDList.add(new BattleIDSave(battleID));
            }



			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					callback.onMoreBattlesLoaded(callbackBattleIDList);
				}
			});


		} catch (LambdaFunctionException lfe) {
			Log.i("ERROR", lfe.getDetails());
			Log.i("ERROR",lfe.getStackTrace().toString());
			lfe.printStackTrace();


		}
		catch (AmazonServiceException ase) {
			// invalid credentials, incorrect AWS signature, etc
			Log.i("ERROR", ase.getErrorMessage());

		}
		catch (AmazonClientException ace) {
			// Network issue
			Log.i("ERROR", ace.toString());

		}



	}

	public void updateUserHasVoted(Context context, int battleid)
	{
		if (isLoaded()) {
			if (mFriendBattleMap.containsKey(battleid)){
				Battle b = mFriendBattleMap.get(battleid);
				b.setUserHasVoted(true);
				mFriendBattleMap.put(battleid, b);
				saveToFile(context);
			}
		}
	}



	private boolean isThereMoreBattles(Context context) throws AmazonClientException {
	    Log.i(TAG, "Getting topBattles count dynamo");


	    int battlesCountDynamo = getBattlesCountDynamo(context);
		// get the last battle from the dynamodb feed. Compare it to the last
		// known in the cache.
		Log.i(TAG, "Battles count Dynamo: " + battlesCountDynamo + ", mlastfriendbattlecount: " + mLastFriendBattleCount);
		if (battlesCountDynamo == mLastFriendBattleCount) {
			return false;
		} else {
			return true;
		}

	}

	private int getBattlesCountDynamo(Context context) throws AmazonClientException {


		AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(
				FacebookLoginFragment.getCredentialsProvider(context));

		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		AttributeValue val = new AttributeValue();
		val.setS(FacebookLoginFragment.getCredentialsProvider(context).getIdentityId());
		key.put("CognitoID", val);
		String projectionExpression = "battle_count";
		GetItemRequest spec = new GetItemRequest()
				.withProjectionExpression(projectionExpression)
				.withTableName("Battle_Activity_Feed").withKey(key);
		int battle_count = 0;

			GetItemResult result = ddbClient.getItem(spec);
			Map<String, AttributeValue> res = result.getItem();

			if (res != null && res.size() > 0) {
				AttributeValue item_count = res.get("battle_count");
				battle_count = Integer.parseInt(item_count.getN());

			}


		return battle_count;

	}



	private int getFeedFullUpdateCountDynamo(Context context) throws AmazonClientException {
        Log.i(TAG, "Getting Full Feed Update COunt Dynamo");


		AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(
				FacebookLoginFragment.getCredentialsProvider(context));

		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		AttributeValue val = new AttributeValue();
		val.setS(FacebookLoginFragment.getCredentialsProvider(context).getIdentityId());
		key.put("CognitoID", val);
		String projectionExpression = "feed_full_update_count";
		GetItemRequest spec = new GetItemRequest()
				.withProjectionExpression(projectionExpression)
				.withTableName("Battle_Activity_Feed").withKey(key);

			GetItemResult result = ddbClient.getItem(spec);

			int full_update_count = 0;
			try {
				Map<String, AttributeValue> res = result.getItem();
				AttributeValue item_count = res.get("feed_full_update_count");
				full_update_count = Integer.parseInt(item_count.getN());
				return full_update_count;
			} catch (NullPointerException e) {
				return 0;
			}




	}

	private ArrayList<Battle> getFriendBattleListFromSQLResult(
			GetFriendsBattlesResponse result) {

		Battle b;
		ArrayList<Battle> mBattles = new ArrayList<Battle>();
		if (result == null)
		{
			return mBattles;
		}
		mBattles.addAll(result.getSqlResult());
		return mBattles;

	}






	private List<Integer> loadListFromDynamo(final Context context,
			final int startIndex, final int endIndex) throws AmazonClientException
			{
		AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(
				FacebookLoginFragment.getCredentialsProvider(context));

		// DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
		// BattleFeed selectedBattle = mapper.load(BattleFeed.class, "45");
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		AttributeValue val = new AttributeValue();
		val.setS(FacebookLoginFragment.getCredentialsProvider(context).getIdentityId());
		key.put("CognitoID", val);
		Log.i(TAG, "Facebook_User_ID: "
				+ AccessToken.getCurrentAccessToken().getUserId());
		String projectionExpression = "";
		for (int i = startIndex; i <= endIndex; i++) {
			projectionExpression = projectionExpression + "BattleID[" + i + "].battleid";
			if (i != endIndex) {
				projectionExpression = projectionExpression + ",";
			}

		}

		Log.i(TAG, "Projection Expression: " + projectionExpression);

		GetItemRequest spec = new GetItemRequest()
				.withProjectionExpression(projectionExpression)
				.withTableName("Battle_Activity_Feed").withKey(key);

				List<Integer> battleIDList = new ArrayList<Integer>();

			GetItemResult result = ddbClient.getItem(spec);
			Log.i("FriendBattleCahce", "Get item Result: " + result.toString());

			Map<String, AttributeValue> res = result.getItem();

			if (res != null && res.containsKey("BattleID")) {
				// Log.i("BattleCache", "Map Strings: " + res.toString());

				AttributeValue list = res.get("BattleID");

				List<AttributeValue> battleIDs = list.getL();

				for (int i = 0; i < battleIDs.size(); i++) {
					battleIDList.add(Integer.parseInt((battleIDs.get(i).getM().get("battleid").getN())));
				}

			}
			return battleIDList;


	}


	

	private void saveToFile(Context context) {
		Log.i(TAG, "Saving the cache file..");
		FollowingBattleCacheFile file = new FollowingBattleCacheFile(context, mBattleIDList,
				mFriendBattleMap, mLastFriendBattleCount, mFullFeedUpdatesCount, mLastTimeUpdated);
		file.saveListToFile();
	}
	
	
	
	public void loadMoreBattles(final Context context,
			final LoadMoreBattlesCallback callback) 
	{
		Runnable r = new Runnable() {
			@Override
			public void run() {
			    try {
                    boolean noMoreBattles = false;

                    Log.i(TAG, "Loading more topBattles.. mlastfriendbattlecount = " + mLastFriendBattleCount + ", " +
                            "topBattles count dynamo" + getBattlesCountDynamo(context) + ", mbattlesize: " + mBattleIDList.size());

                    int totalBattlesCountDynamo = getBattlesCountDynamo(context);

                    if (totalBattlesCountDynamo != mBattleIDList.size()) {
                        int startIndex = totalBattlesCountDynamo
                                - mLastFriendBattleCount
                                + mBattleIDList.size();
                        int endIndex = startIndex + LOAD_MORE_BATTLES_AMOUNT - 1;


                        ArrayList<Integer> moreBattlesList = new ArrayList<Integer>(
                                loadListFromDynamo(context, startIndex, endIndex));
                        addFriendBattlesFromSQLtoEnd(context, moreBattlesList,
                                callback);

                        if (moreBattlesList.size() != LOAD_MORE_BATTLES_AMOUNT) {
                            noMoreBattles = true;
                        }
                    } else {
                        noMoreBattles = true;
                    }

                    if (noMoreBattles) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.ThereIsNoMoreBattles();
                            }
                        });

                    }
                }
                catch(AmazonClientException e)
                {
                    //Network error. Do not load More
                    //TODO callback there is error
                }
			}
		};
		mExecutorService.submit(r);


	}

	public static ExecutorService newFixedThreadPool(int nThreads) {
		return new ThreadPoolExecutor(nThreads, nThreads,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}


}
