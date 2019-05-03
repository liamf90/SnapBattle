package com.liamfarrell.android.snapbattle.service;

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.liamfarrell.android.snapbattle.ActivityMainNavigationDrawer;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.activity.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.notifications.BattleAcceptedNotification;
import com.liamfarrell.android.snapbattle.notifications.TaggedInCommentNotification;
import com.liamfarrell.android.snapbattle.notifications.NewBattleRequestNotification;
import com.liamfarrell.android.snapbattle.notifications.NewCommentNotification;
import com.liamfarrell.android.snapbattle.notifications.NewFollowerNotification;
import com.liamfarrell.android.snapbattle.caches.NotificationCache;
import com.liamfarrell.android.snapbattle.notifications.VideoSubmittedNotification;
import com.liamfarrell.android.snapbattle.notifications.VotingCompleteNotification;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static final String BATTLE_CHALLENGES_LIST_CLASS = "battle_challenge";
    private static final String BATTLE_CLASS = "battle";
    public static final String TYPE_VIDEO_SUBMITTED = "video_submit";
    private static final String TYPE_BATTLE_ACCEPTED = "battle_accept";
    public static final String UPLOAD_FULL_VIDEO = "full_video_uploaded";
    private static final String NEW_COMMENT = "new_comment";
    private static final String VOTE_COMPLETE = "vote_complete";
    private static final String NEW_FOLLOWER = "user_follow";
    private static final String TAGGED_IN_COMMENT = "tagged_in_comment";

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "my_notification_channel";

    public static final String ACTION_VIDEO_SUBMITTED = "com.liamfarrell.android.snapbattle.mygcmlistenerservice.videosubmittedbroadcast";
    public static final String ACTION_FULL_VIDEO_FINISHED = "com.liamfarrell.android.snapbattle.mygcmlistenerservice.fullvideofinishedbroadcast";
    public static final String PERM_PRIVATE =
            "com.liamfarrell.android.snapbattle.mygcmlistenerservice.PRIVATE";
    public static final String TYPE_INTENT_EXTRA = "com.liamfarrell.android.snapbattle.mygcmlistenerservice.type";
    public static final String BATTLE_ID_INTENT_EXTRA = "com.liamfarrell.android.snapbattle.mygcmlistenerservice.battleid";


    
    
    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
    	
    	//if (data.getString("to_facebook_id").equals(AccessToken.getCurrentAccessToken().getUserId()))
    	//{
		//Initialize Facebook SDK
        Log.i(TAG, "GCM MESSAGE RECEIVED");
        String cognitoID = FacebookLoginFragment.getCredentialsProvider(this).getCachedIdentityId();

        if (cognitoID != null) {
            String message = data.getString("message");

            switch (data.getString("type")) {
                case BATTLE_CHALLENGES_LIST_CLASS:

                    if (data.getString("challenged_cognito_id").equals(FacebookLoginFragment.getCredentialsProvider(this).getCachedIdentityId())) {
                        NewBattleRequestNotification nrn = new NewBattleRequestNotification(Integer.parseInt(data.getString("battleID")), data.getString("mChallengerCognitoId"), data.getString("challenger_username"));
                        message = nrn.getMessage().toString();
                        updateNotificationFragmentList();
                        sendNotification(message, nrn.getIntent(this));
                    }
                    break;
                case TYPE_VIDEO_SUBMITTED:
                    if (data.getString("opponent_cognito_id").equals(cognitoID)) {

                        VideoSubmittedNotification n = new VideoSubmittedNotification(Integer.parseInt(data.getString("battleID")), data.getString("uploader_cogntito_id"), data.getString("uploader_username"));
                        message = n.getMessage().toString();

                        updateNotificationFragmentList();
                        sendNotification(message, n.getIntent(this));
                        Intent actionVideoSubmittedIntent = new Intent(ACTION_VIDEO_SUBMITTED);
                        actionVideoSubmittedIntent.putExtra(TYPE_INTENT_EXTRA, TYPE_VIDEO_SUBMITTED);
                        actionVideoSubmittedIntent.putExtra(BATTLE_ID_INTENT_EXTRA, Integer.parseInt(data.getString("battleID")));
                        //send broadcast intent in case the view battle fragment is visible, so the view battle fragment will update its view
                        sendBroadcast(actionVideoSubmittedIntent, PERM_PRIVATE);
                    }
                    break;
                case TYPE_BATTLE_ACCEPTED:

                    if (data.getString("opponent_cognito_id").equals(cognitoID)) {
                        BattleAcceptedNotification ban = new BattleAcceptedNotification(Integer.parseInt(data.getString("battleID")), data.getString("opponent_cognito_id"), data.getString("opponent_username"), Boolean.valueOf(data.getString("battle_accepted")));
                        message = ban.getMessage().toString();

                        updateNotificationFragmentList();
                        sendNotification(message, ban.getIntent(this));
                    }
                    break;
                case UPLOAD_FULL_VIDEO:

                    if (data.getString("cognito_id_opponent_1").equals(cognitoID) || data.getString("cognito_id_opponent_2").equals(cognitoID)) {

                        Intent actionFinalVideoFinishedIntent = new Intent(ACTION_FULL_VIDEO_FINISHED);
                        actionFinalVideoFinishedIntent.putExtra(TYPE_INTENT_EXTRA, UPLOAD_FULL_VIDEO);
                        actionFinalVideoFinishedIntent.putExtra(BATTLE_ID_INTENT_EXTRA, Integer.parseInt(data.getString("battleID")));
                        sendBroadcast(actionFinalVideoFinishedIntent, PERM_PRIVATE);
                    }
                    break;
                case NEW_COMMENT:
                    updateNotificationFragmentList();
                    if (data.getString("cognito_id_opponent").equals(cognitoID)) {
                        NewCommentNotification ncn = new NewCommentNotification(Integer.parseInt(data.getString("battleID")), data.getString("battle_name"), data.getString("cognito_id_commenter"), data.getString("commenter_name"));
                        message = ncn.getMessage().toString();
                        sendNotification(message, ncn.getIntent(this));
                    }
                    break;
                case TAGGED_IN_COMMENT:
                    updateNotificationFragmentList();
                    if (data.getString("cognito_id_to").equals(cognitoID)) {

                        TaggedInCommentNotification ncn = new TaggedInCommentNotification(Integer.parseInt(data.getString("battleID")), data.getString("battle_name"), data.getString("cognito_id_commenter"), data.getString("commenter_name"), data.getString("challenger_username"), data.getString("challenged_username"), data.getString("challenger_cognito_id"), data.getString("challenged_cognito_id"));
                        message = ncn.getMessage().toString();
                        sendNotification(message, ncn.getIntent(this));
                    }
                        break;
                case VOTE_COMPLETE:

                    if (data.getString("cognito_id").equals(cognitoID)) {
                        VotingCompleteNotification vcn = new VotingCompleteNotification(Integer.parseInt(data.getString("battle_id")), data.getString("cognito_id_opponent"), data.getString("username_opponent"), Integer.parseInt(data.getString("vote")), Integer.parseInt(data.getString("vote_opponent")), data.getString("voting_result"));
                        message = vcn.getMessage().toString();

                        updateNotificationFragmentList();
                        sendNotification(message, vcn.getIntent(this));
                    }
                    break;
                case NEW_FOLLOWER:
                    if (data.getString("to_cognito_id").equals(cognitoID)) {
                        NewFollowerNotification nfn = new NewFollowerNotification(data.getString("follower_cognito_id"), data.getString("follower_username"));
                        message = nfn.getMessage().toString();

                        updateNotificationFragmentList();
                        sendNotification(message, nfn.getIntent(this));
                    }
                    break;
                default:
                    sendNotification(message, null);

                    }

                    Log.d(TAG, "From: " + from);
                    Log.d(TAG, "Message: " + message);
                    Log.d(TAG, "Data: " + data.toString());

            }
        }




    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, Intent intent) {


    	if ( intent == null)
        {
            intent = new Intent(this, ActivityMainNavigationDrawer.class);
        }




        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Settings such as vibration pattern specified in the NotificationChannel override those specified in the actual Notification.
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.battle_icon_selected)
                .setContentTitle("Snap Battle")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        
    }

    private void updateNotificationFragmentList()
    {
        NotificationCache.getNotificationCache().gcmUpdate(this);
    }




}