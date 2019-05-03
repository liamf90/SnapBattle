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
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.activity.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetFriendsBattlesResponse;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.GetFriendsBattlesRequest;

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
 * An Singleton cache for filling and updating the All Battles Feed.
 * This class gets the battle id list of the all battles feed from the dynamo server, and then retrieves the battles from the mysql server.
 * Checking for updates is done by comparing the last battle count on the aws dynamo table to the stored battle count.
 * The cache is stored using AllBattlesFeedCacheFile which serializes the data using normal java serialization file saving
 */

public class AllBattlesFeedCache {
    private static final int LOAD_MORE_BATTLES_AMOUNT = 5;
    private static final int FULL_FEED_UPDATE_AMOUNT = 15;
    public static final String TAG = "AllBattlesFeedCache";
    private static boolean isLoaded;
    private static AllBattlesFeedCache sAllBattlesFeedCache;

    private LinkedList<Integer> mBattleIDList;
    private ConcurrentHashMap<Integer, Battle> mAllBattlesMap;
    private Date mLastTimeUpdated;
    private int mLastAllBattleCount;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean isCurrentlyLoadingOrUpdating;

    private ExecutorService mExecutorService;


    public interface UpdateCallbacks
    {
         void onNewBattles(List<Integer> battleIDList);
         void onUpdated(List<Integer> updatedBattleIDList);
    }

    public interface CacheLoadCallbacks {
         void OnNoFileForUser();
         void OnCacheLoaded(List<Integer> battleIDList);
         void OnNoBattlesInFeed();
         void OnNoFileLoad(List<Integer> battleIDList);
    }



    public interface LoadMoreBattlesCallback {
        void onMoreBattlesLoaded(List<Integer> moreBattles);

        void ThereIsNoMoreBattles();
    }

    public int getBattleIDFromPosition(int position)
    {
        return mBattleIDList.get(position);
    }

    private AllBattlesFeedCache(Context context) {

        mBattleIDList = new LinkedList<Integer>();
        mAllBattlesMap = (new ConcurrentHashMap<Integer, Battle>());
        isLoaded = false;
        mExecutorService = newFixedThreadPool(1);
    }

    public static AllBattlesFeedCache get(Context context) {
        if (sAllBattlesFeedCache == null)
        {
            Log.i(TAG, "sAllBattlesFeedCache == null");
            sAllBattlesFeedCache = new AllBattlesFeedCache(context);
        }
        else
        {
            Log.i(TAG, "sAllBattlesFeedCache != null");
        }
        return sAllBattlesFeedCache;
    }

    public static void closeCache(){
        sAllBattlesFeedCache = null;
    }



    public Battle getBattle(int battleIndex) {

        return mAllBattlesMap.get(battleIndex);
    }

    public LinkedList<Integer> getCacheBattleIDList() {
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

    private void loadListGo(final Context context,
                            final CacheLoadCallbacks callback, final UpdateCallbacks updateCallback, final boolean fullListLoad) {



        try {
            Log.i(TAG, "Loading file..");

            mBattleIDList.clear();
            mAllBattlesMap.clear();
            AllBattlesFeedCacheFile file = new AllBattlesFeedCacheFile(context);



            mAllBattlesMap.putAll(file.getBattleMap());
            mBattleIDList.addAll(file.getBattleList());

            Log.i(TAG, mAllBattlesMap.toString());
            mLastTimeUpdated = file.getLastTimeUpdated();
            mLastAllBattleCount = file.getLastAllBattlesCount();
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
                        AllBattlesFeedCacheFile.FILE_MAX_CAPACITY - 1);
                int battlesCountDynamo = getBattlesCountDynamo(context);

                if (arrayList.size() == 0) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.OnNoBattlesInFeed();
                        }
                    });
                }


                addBattlesFromSQLtoTop(context, true,
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
                        if (!isThereMoreBattles(context)) {
                            // NO NEW BATTLES
                            Log.i(TAG, "No more battles from cache to dynamo list");
                            // callback.OnNoNewBattles();

                            updateBattlesFromSQL(context, mBattleIDList,
                                    callback);

                        } else {
                            Log.i(TAG,
                                    "More battles from dynamo list. Getting these..");
                            // to update list = old battles minus new battles
                            List<Integer> oldBattlesList = mBattleIDList;

                            // NEW BATTLES
                            // get new list
                            int battlesCountDynamo = getBattlesCountDynamo(context);


                            List<Integer> newBattlesList = loadListFromDynamo(
                                    context, 0, battlesCountDynamo
                                            - mLastAllBattleCount - 1);

                            // add list to mFriendsBattleFile
                            addBattlesFromSQLtoTop(context, false,
                                    (ArrayList<Integer>) newBattlesList,
                                    battlesCountDynamo, null, callback);


                            // Update old battles
                            updateBattlesFromSQL(context, oldBattlesList,
                                    callback);

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

    private void updateBattlesFromSQL(final Context context,
                                      List<Integer> battleIDList, final UpdateCallbacks callback) {
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
            GetFriendsBattlesRequest battle = new GetFriendsBattlesRequest();
            battle.setBattleIDList(battleIDList);
            if (mLastTimeUpdated != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                battle.setLastUpdatedDate(sdf.format(mLastTimeUpdated));
            }

            try {
                GetFriendsBattlesResponse response = lambdaFunctionsInterface.GetFriendsBattles(battle);
                ArrayList<Battle> resultList = getBattleListFromSQLResult(response);
                final ArrayList<Integer> resultBattleIDList = new ArrayList<>();
                for (int i = 0; i < resultList.size(); i++) {
                    resultBattleIDList.add(resultList.get(i).getBattleId());
                    mAllBattlesMap.put(resultList.get(i).getBattleId(),
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

    private void addBattlesFromSQLtoTop(final Context context,
                                        final boolean noFileLoad, final ArrayList<Integer> battleIDList,
                                        final int totalAllBattlesCount, final CacheLoadCallbacks callback,
                                        final UpdateCallbacks updateCallback) {

        mLastAllBattleCount = totalAllBattlesCount;

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
                final ArrayList<Battle> resultList = getBattleListFromSQLResult(response);

                // add battles to map
                for (int i = 0; i < resultList.size(); i++) {
                    mAllBattlesMap.put(resultList.get(i).getBattleId(),
                            resultList.get(i));

                }
                // add battles to start of battle id
                for (int i = battleIDList.size() - 1; i >= 0; i--) {

                    mBattleIDList.addFirst(battleIDList
                            .get(i));
                }


                // Save List
                saveToFile(context);
                // if Fragment.Callbacks is not null, callback with
                // battleIDList.

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
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateCallback.onNewBattles(battleIDList);
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

    public void updateUserHasVoted(Context context, int battleid)
    {
        if (isLoaded()) {
            if (mAllBattlesMap.containsKey(battleid)){
                Battle b = mAllBattlesMap.get(battleid);
                b.setUserHasVoted(true);
                mAllBattlesMap.put(battleid, b);
                saveToFile(context);
            }
        }
    }




    private void addBattlesFromSQLtoEnd(final Context context,
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
            ArrayList<Battle> resultList = getBattleListFromSQLResult(response);

            // add battles to map
            for (int i = 0; i < resultList.size(); i++) {
                mAllBattlesMap.put(resultList.get(i).getBattleId(),
                        resultList.get(i));

            }
            mBattleIDList.addAll(battleIDList);


            Log.i(TAG, "More battles loaded. " + mBattleIDList.toString());
            Log.i(TAG, "F Map: " + mAllBattlesMap.toString());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onMoreBattlesLoaded(battleIDList);
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





    private boolean isThereMoreBattles(Context context) throws AmazonClientException {
        Log.i(TAG, "Getting battles count dynamo");


        int battlesCountDynamo = getBattlesCountDynamo(context);
        // get the last battle from the dynamodb feed. Compare it to the last
        // known in the cache.
        Log.i(TAG, "Battles count Dynamo: " + battlesCountDynamo + ", mlastbattlecount: " + mLastAllBattleCount);
        if (battlesCountDynamo == mLastAllBattleCount) {
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
        val.setS("main");
        key.put("table", val);
        String projectionExpression = "battle_count";
        GetItemRequest spec = new GetItemRequest()
                .withProjectionExpression(projectionExpression)
                .withTableName("All_Battles_Feed").withKey(key);
        int battle_count = 0;

        GetItemResult result = ddbClient.getItem(spec);
        Map<String, AttributeValue> res = result.getItem();

        if (res != null && res.size() > 0) {
            AttributeValue item_count = res.get("battle_count");
            battle_count = Integer.parseInt(item_count.getN());

        }


        return battle_count;

    }



    private ArrayList<Battle> getBattleListFromSQLResult(
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
        val.setS("main");
        key.put("table", val);

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
                .withTableName("All_Battles_Feed").withKey(key);

        List<Integer> battleIDList = new ArrayList<Integer>();

        GetItemResult result = ddbClient.getItem(spec);

        Map<String, AttributeValue> res = result.getItem();

        if (res != null && res.containsKey("BattleID")) {
            //Log.i("BattleCache", "Map Strings: " + res.toString());

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
        AllBattlesFeedCacheFile file = new AllBattlesFeedCacheFile(context, mBattleIDList,
                mAllBattlesMap, mLastAllBattleCount, mLastTimeUpdated);
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

                    Log.i(TAG, "Loading more battles.. mlastbattlecount = " + mLastAllBattleCount + ", " +
                            "battles count dynamo" + getBattlesCountDynamo(context) + ", mbattlesize: " + mBattleIDList.size());

                    int totalBattlesCountDynamo = getBattlesCountDynamo(context);

                    if (totalBattlesCountDynamo != mBattleIDList.size()) {
                        int startIndex = totalBattlesCountDynamo
                                - mLastAllBattleCount
                                + mBattleIDList.size();
                        int endIndex = startIndex + LOAD_MORE_BATTLES_AMOUNT - 1;


                        ArrayList<Integer> moreBattlesList = new ArrayList<Integer>(
                                loadListFromDynamo(context, startIndex, endIndex));
                        addBattlesFromSQLtoEnd(context, moreBattlesList,
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
