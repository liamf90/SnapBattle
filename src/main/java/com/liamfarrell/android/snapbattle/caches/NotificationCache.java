package com.liamfarrell.android.snapbattle.caches;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.facebook.AccessToken;
import com.liamfarrell.android.snapbattle.activity.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.notifications.BattleAcceptedNotification;
import com.liamfarrell.android.snapbattle.notifications.FullVideoUploadedNotification;
import com.liamfarrell.android.snapbattle.notifications.NewBattleRequestNotification;
import com.liamfarrell.android.snapbattle.notifications.NewCommentNotification;
import com.liamfarrell.android.snapbattle.notifications.NewFollowerNotification;
import com.liamfarrell.android.snapbattle.notifications.Notification;
import com.liamfarrell.android.snapbattle.notifications.TaggedInCommentNotification;
import com.liamfarrell.android.snapbattle.notifications.VideoSubmittedNotification;
import com.liamfarrell.android.snapbattle.notifications.VotingCompleteNotification;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 *An singleton cache which loads, saves and updates the notifications for user.
 * Notifications updates are checked on the dynamo db server for user. If the current notification count on the db server is greater then the caches count,
 * the difference is retrieved from the dynamo db.
 * The boolean indicating weather all the notifications has been seen (so a red dot can be put on the notifications button in the actionbar) is also updated and saved.
 * This class deals with the deserializtion of the dynamo list directly. TODO use a seperate deserialization class
 *
 * The cache is stored using NotificationCacheFile which serializes the data using normal java serialization file saving
 *
 */

public class NotificationCache {
        public static final int LOAD_MORE_NOTIFICATIONS_AMOUNT = 10;
        public static final String TAG = "NotificationLoader";

        private int mLastNotificationCount;
        private LinkedList<Notification> mNotificationList;
        private static NotificationCache sNotificationCache;
        private GCMUpdatesCallback gcmUpdatesCallback;
        private boolean mHasAllNotificationsSeen = true;





        public interface GCMUpdatesCallback
         {
           void onUpdates( boolean hasAllNotificationsBeenSeen);
         }


        public interface LoadMoreNotificationsCallback {
            void onNotificationsLoaded(List<Notification> notifications);
            void onNoMoreNotificationsAvailable();
        }
        public interface LoadNotificationsCallback
        {
            void onNoUpdates(boolean hasAllNotificationsBeenSeen);
            void onLoaded(LinkedList<Notification> notificationList, boolean hasAllNotificationsBeenSeen);
            void onUpdates(LinkedList<Notification> notificationUpdatesForTop, boolean hasAllNotificationsBeenSeen);
        }

        public static NotificationCache getNotificationCache()
        {
            if (sNotificationCache == null)
            {
                sNotificationCache = new NotificationCache();
            }
            return sNotificationCache;

        }



    public static void closeCache(){
        sNotificationCache = null;
    }



    public LinkedList<Notification> getNotificationList()
        {
            return mNotificationList;
        }


        public void setGCMUpdateCallback(GCMUpdatesCallback callback)
        {
            gcmUpdatesCallback = callback;

        }

        public void gcmUpdate(Context context)
        {
            //this method is called when a google cloud message is received notifiying of an new notification
            LoadListFromFile(context, new LoadNotificationsCallback() {


                @Override
                public void onNoUpdates(boolean hasAllNotificationsBeenSeen) {

                }

                @Override
                public void onLoaded(LinkedList<Notification> notificationList, boolean hasAllNotificationsBeenSeen) {

                }

                @Override
                public void onUpdates(LinkedList<Notification> notificationUpdatesForTop, boolean hasAllNotificationsBeenSeen) {
                    gcmUpdatesCallback.onUpdates(mHasAllNotificationsSeen);
                }

            });
        }

        public void LoadListFromFile(final Context context, final LoadNotificationsCallback callback) {

            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        Log.i(TAG, "Loading file..");

                        mNotificationList = new LinkedList<Notification>();
                        NotificationCacheFile file = new NotificationCacheFile(context);
                        mLastNotificationCount = file.getLastNotificationCount();
                        mHasAllNotificationsSeen = file.hasAllNotificationsSeen();
                        mNotificationList.addAll(file.getNotificationList());
                        Log.i(TAG, "Loaded file");

                        callback.onLoaded(mNotificationList, mHasAllNotificationsSeen);



                        //check for updates

                        int newNotificationCountDynamo = getNotificationsCountDynamo(context);

                        mHasAllNotificationsSeen = getDynamoHasSeenAllNotifications(context);

                        if (newNotificationCountDynamo > mLastNotificationCount)
                        {
                            final List<Notification> arrayList = loadListFromDynamo(context, 0, newNotificationCountDynamo - mLastNotificationCount -1 );
                            mNotificationList.addAll(0, arrayList);

                            NotificationCacheFile fileSave = new NotificationCacheFile(context, mNotificationList,newNotificationCountDynamo, mHasAllNotificationsSeen );
                            fileSave.saveListToFile();
                            Handler mainHandler = new Handler(context.getMainLooper());
                            mainHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    callback.onUpdates((LinkedList)arrayList, mHasAllNotificationsSeen);
                                }
                            });



                        }
                        else
                        {
                            NotificationCacheFile fileSave = new NotificationCacheFile(context, mNotificationList,newNotificationCountDynamo, mHasAllNotificationsSeen );
                            fileSave.saveListToFile();
                            Handler mainHandler = new Handler(context.getMainLooper());
                            mainHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    callback.onNoUpdates(mHasAllNotificationsSeen);
                                }
                            });


                        }



                    } catch (FileNotFoundException e) {
                        Log.i(TAG, "No file. Retrieving from server");
                        try {
                            mLastNotificationCount = getNotificationsCountDynamo(context);
                            mHasAllNotificationsSeen = getDynamoHasSeenAllNotifications(context);
                        }
                        catch(AmazonClientException f)
                        {
                            //Network error. Abort getting notifications
                            return;
                        }

                        int lastIndex;
                        if (mLastNotificationCount < FollowingBattleCacheFile.FILE_MAX_CAPACITY)
                        {
                            lastIndex = mLastNotificationCount -1 ;
                        }
                        else
                        {
                            lastIndex = FollowingBattleCacheFile.FILE_MAX_CAPACITY - 1;
                        }


                        // Load from dynamo DB
                        final List<Notification> arrayList = loadListFromDynamo(context, 0,
                                lastIndex);
                        mNotificationList.addAll(arrayList);

                        NotificationCacheFile file = new NotificationCacheFile(context, (LinkedList)arrayList, mLastNotificationCount, mHasAllNotificationsSeen);
                        file.saveListToFile();
                        Handler mainHandler = new Handler(context.getMainLooper());
                        mainHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                callback.onLoaded((LinkedList)arrayList, mHasAllNotificationsSeen);
                            }
                        });




                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (AmazonClientException e)
                    {
                        //Network error. Abort checking for updates
                    }

                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();

        }


        public boolean hasAllNotificationsBeenSeen()
        {
            return mHasAllNotificationsSeen;
        }



        private int getNotificationsCountDynamo(Context context) throws AmazonClientException {
            Log.i(TAG, "Getting notifications count dynamo");
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(
                    FacebookLoginFragment.getCredentialsProvider(context));

            Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
            AttributeValue val = new AttributeValue();
            val.setS(FacebookLoginFragment.getCredentialsProvider(context).getIdentityId());
            key.put("CognitoID", val);
            String projectionExpression = "notification_count";
            GetItemRequest spec = new GetItemRequest()
                    .withProjectionExpression(projectionExpression)
                    .withTableName("Battle_Activity_Feed").withKey(key);



                GetItemResult result = ddbClient.getItem(spec);
                Map<String, AttributeValue> res = result.getItem();
                if (res == null)
                {
                    return 0;
                }
                else
                {

                    AttributeValue item_count = res.get("notification_count");
                    int battle_count = 0;
                    if (item_count != null)
                    {
                        battle_count = Integer.parseInt(item_count.getN());
                    }

                    return battle_count;
                }




        }


    public void updateDynamoSeenAllNotifications(final Context context) {

        final Runnable runnable = new Runnable() {
            public void run() {
                if (!mHasAllNotificationsSeen)
                {
                    AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(
                            FacebookLoginFragment.getCredentialsProvider(context));

                    Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
                    AttributeValue val = new AttributeValue();
                    val.setS(FacebookLoginFragment.getCredentialsProvider(context).getIdentityId());
                    key.put("CognitoID", val);

                    Map<String, AttributeValue> notificationSeenAttribute = new HashMap<String, AttributeValue>();

                    AttributeValue notificationsSeenFalse = new AttributeValue();
                    notificationsSeenFalse.setBOOL(true);
                    notificationSeenAttribute.put(":val1", notificationsSeenFalse);

                    String updateExpression = "SET notifications_seen = :val1";
                    UpdateItemRequest uir = new UpdateItemRequest().withTableName("Battle_Activity_Feed").withKey(key).withUpdateExpression(updateExpression).withExpressionAttributeValues(notificationSeenAttribute);
                    try {
                        ddbClient.updateItem(uir);
                        mHasAllNotificationsSeen = true;
                        NotificationCacheFile file = new NotificationCacheFile(context, mNotificationList, mLastNotificationCount, mHasAllNotificationsSeen);
                        file.saveListToFile();
                    }catch(AmazonClientException e)
                    {
                        //Network error. Leave as is
                    }

                }
            }};

            Thread t1 = new Thread(runnable);
            t1.start();



    }

    private boolean getDynamoHasSeenAllNotifications(Context context) throws AmazonClientException {

        //This method checks on dynamoDB if the  user has seen all his notifications or not
        //So we know weather to display the unseen notification red dot or not

        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(
                FacebookLoginFragment.getCredentialsProvider(context));

        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        AttributeValue val = new AttributeValue();
        val.setS(FacebookLoginFragment.getCredentialsProvider(context).getIdentityId());
        key.put("CognitoID", val);
        String projectionExpression = "notifications_seen";
        GetItemRequest spec = new GetItemRequest()
                .withProjectionExpression(projectionExpression)
                .withTableName("Battle_Activity_Feed").withKey(key);

           GetItemResult result = ddbClient.getItem(spec);
           Map<String, AttributeValue> res = result.getItem();
           if (res == null) {
               return true;
           } else {

               AttributeValue hasSeenNotifications = res.get("notifications_seen");
               if (hasSeenNotifications != null) {
                   return hasSeenNotifications.getBOOL();
               } else {
                   return true;
               }
           }

    }



        private List<Notification> loadListFromDynamo(final Context context,
                                                 final int startIndex, final int endIndex)
        {
            //this method loads notifications from dynamodb between indexes

            List<Notification> notificationList = new LinkedList<Notification>();
            if (endIndex < startIndex)
            {
                return notificationList;
            }
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(
                    FacebookLoginFragment.getCredentialsProvider(context));

            // DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            // NotificationFeed selectedNotification = mapper.load(NotificationFeed.class, "45");
            Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
            AttributeValue val = new AttributeValue();
            val.setS(FacebookLoginFragment.getCredentialsProvider(context).getIdentityId());
            key.put("CognitoID", val);
            Log.i(TAG, "Facebook_User_ID: "
                    + AccessToken.getCurrentAccessToken().getUserId());
            String projectionExpression = "";
            for (int i = startIndex; i <= endIndex; i++) {
                projectionExpression = projectionExpression + "NotificationList[" + i + "]";
                if (i != endIndex) {
                    projectionExpression = projectionExpression + ",";
                }

            }

            Log.i(TAG, "Projection Expression: " + projectionExpression);

            GetItemRequest spec = new GetItemRequest()
                    .withProjectionExpression(projectionExpression)
                    .withTableName("Battle_Activity_Feed").withKey(key);
            try {
                GetItemResult result = ddbClient.getItem(spec);

                Map<String, AttributeValue> res = result.getItem();


                // Log.i("NotificationLoader", "Map Strings: " + res.toString());


                AttributeValue list = res.get("NotificationList");

                if (list != null) {
                    List<AttributeValue> notificationsDynamo = list.getL();
                    Log.i(TAG, "Notifications Dynamo: " + notificationsDynamo.toString());
                    for (int i = 0; i < notificationsDynamo.size(); i++) {
                        NotificationType notificationType = NotificationType.valueOf(notificationsDynamo.get(i).getM().get("TYPE").getS());

                        Notification notification;

                        switch(notificationType) {
                              case NEW_BATTLE_REQUEST : {
                                    int battleId = Integer.parseInt(notificationsDynamo.get(i).getM().get("BATTLE_ID").getN());
                                    String cognitoIdChallenger = notificationsDynamo.get(i).getM().get("COGNITO_ID_CHALLENGER").getS();
                                    String challengerName = notificationsDynamo.get(i).getM().get("CHALLENGER_NAME").getS();

                                    notification = new NewBattleRequestNotification(battleId, cognitoIdChallenger, challengerName);
                                    notificationList.add(notification);
                                    break;
                               }
                              case VIDEO_SUBMITTED : {
                                  int battleId = Integer.parseInt(notificationsDynamo.get(i).getM().get("BATTLE_ID").getN());
                                  String cognitoIdChallenger = notificationsDynamo.get(i).getM().get("COGNITO_ID_OPPONENT").getS();
                                  String challengerName = notificationsDynamo.get(i).getM().get("OPPONENT_NAME").getS();

                                  notification = new VideoSubmittedNotification(battleId, cognitoIdChallenger, challengerName);
                                  notificationList.add(notification);
                                  break;
                              }
                            case CHALLENGE_ACCEPTED : {
                                int battleId = Integer.parseInt(notificationsDynamo.get(i).getM().get("BATTLE_ID").getN());
                                String cognitoIdOpponent = notificationsDynamo.get(i).getM().get("COGNITO_ID_OPPONENT").getS();
                                String opponentName = notificationsDynamo.get(i).getM().get("OPPONENT_NAME").getS();
                                boolean battleAccepted = notificationsDynamo.get(i).getM().get("ACCEPTED").getBOOL();

                                notification = new BattleAcceptedNotification(battleId, cognitoIdOpponent, opponentName, battleAccepted);
                                notificationList.add(notification);
                                break;
                            }
                            case FULL_VIDEO_CREATED: {
                                int battleId = Integer.parseInt(notificationsDynamo.get(i).getM().get("BATTLE_ID").getN());
                                String battleName = notificationsDynamo.get(i).getM().get("BATTLE_NAME").getS();
                                notification = new FullVideoUploadedNotification(battleId, battleName);
                                notificationList.add(notification);
                                break;
                            }

                            case NEW_COMMENT : {
                                int battleID = Integer.parseInt(notificationsDynamo.get(i).getM().get("BATTLE_ID").getN());
                                String battleName = notificationsDynamo.get(i).getM().get("BATTLE_NAME").getS();
                                String cognitoIdCommenter = notificationsDynamo.get(i).getM().get("COGNITO_ID_COMMENTER").getS();
                                String commenterName = notificationsDynamo.get(i).getM().get("COMMENTER_NAME").getS();
                                notification = new NewCommentNotification(battleID, battleName, cognitoIdCommenter, commenterName);
                                notificationList.add(notification);
                                break;
                            }
                            case TAGGED_IN_COMMENT: {
                                int battleID = Integer.parseInt(notificationsDynamo.get(i).getM().get("BATTLE_ID").getN());
                                String battleName = notificationsDynamo.get(i).getM().get("BATTLE_NAME").getS();
                                String cognitoIdCommenter = notificationsDynamo.get(i).getM().get("COGNITO_ID_COMMENTER").getS();
                                String commenterName = notificationsDynamo.get(i).getM().get("COMMENTER_NAME").getS();
                                String cognitoIdChallenger =  notificationsDynamo.get(i).getM().get("COGNITO_ID_CHALLENGER").getS();
                                String cognitoIdChallenged =  notificationsDynamo.get(i).getM().get("COGNITO_ID_CHALLENGED").getS();
                                String usernameChallenger =  notificationsDynamo.get(i).getM().get("USERNAME_CHALLENGER").getS();
                                String usernameChallenged =  notificationsDynamo.get(i).getM().get("USERNAME_CHALLENGED").getS();
                                notification = new TaggedInCommentNotification(battleID, battleName, cognitoIdCommenter, commenterName,usernameChallenger,usernameChallenged, cognitoIdChallenger, cognitoIdChallenged  );
                                notificationList.add(notification);
                                break;
                            }
                            case NEW_FOLLOWER : {
                                String cognitoIdFollower = notificationsDynamo.get(i).getM().get("COGNITO_ID_FOLLOWER").getS();
                                String followerName = notificationsDynamo.get(i).getM().get("FOLLOWER_NAME").getS();
                                notification = new NewFollowerNotification(cognitoIdFollower, followerName);
                                notificationList.add(notification);
                                break;
                            }
                            case VOTE_COMPLETE : {
                                int battleid = Integer.parseInt(notificationsDynamo.get(i).getM().get("BATTLE_ID").getN());
                                String cognitoIdOpponent = notificationsDynamo.get(i).getM().get("COGNITO_ID_OPPONENT").getS();
                                String opponentName = notificationsDynamo.get(i).getM().get("OPPONENT_NAME").getS();
                                int vote = Integer.parseInt(notificationsDynamo.get(i).getM().get("VOTE").getN());
                                int voteOpponent = Integer.parseInt(notificationsDynamo.get(i).getM().get("VOTE_OPPONENT").getN());
                                String votingResult = notificationsDynamo.get(i).getM().get("RESULT").getS();
                                notification = new VotingCompleteNotification(battleid, cognitoIdOpponent, opponentName, vote, voteOpponent, votingResult);
                                notificationList.add(notification);
                                break;
                            }
                        }


                    }

                }
            }
            catch (AmazonClientException e)
            {

                //Network error

            }

            return notificationList;



            // List<Integer> NotificationList = selectedNotification.getNotificationIDList();
            // Log.i(TAG, "Id'S: " + battleIDList.toString());
        }





        public void LoadMoreNotifications(final Context context,
                                           final int notificationListSize, final LoadMoreNotificationsCallback callback)
        {

            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        int totalNotificationsCountDynamo = getNotificationsCountDynamo(context);

                        if (totalNotificationsCountDynamo != notificationListSize) {
                            int startIndex = totalNotificationsCountDynamo
                                    - mLastNotificationCount
                                    + notificationListSize;
                            int endIndex = startIndex + LOAD_MORE_NOTIFICATIONS_AMOUNT - 1;


                            final LinkedList<Notification> moreNotificationsList = new LinkedList<Notification>(
                                    loadListFromDynamo(context, startIndex, endIndex));
                            Handler mainHandler = new Handler(context.getMainLooper());
                            mainHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    callback.onNotificationsLoaded(moreNotificationsList);
                                }
                            });


                        } else {
                            Handler mainHandler = new Handler(context.getMainLooper());
                            mainHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    callback.onNoMoreNotificationsAvailable();
                                }
                            });

                        }
                    }
                    catch (AmazonClientException e)
                    {
                        //Network error from getNotificationsCountDynamo or load list from dynamo function
                        //abort loading more.

                    }


                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();

        }







}
