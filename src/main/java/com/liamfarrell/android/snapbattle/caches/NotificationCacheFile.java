package com.liamfarrell.android.snapbattle.caches;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import android.content.Context;
import android.util.Log;
import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.RuntimeTypeAdapterFactory;
import com.liamfarrell.android.snapbattle.notifications.BattleAcceptedNotification;
import com.liamfarrell.android.snapbattle.notifications.FullVideoUploadedNotification;
import com.liamfarrell.android.snapbattle.notifications.NewBattleRequestNotification;
import com.liamfarrell.android.snapbattle.notifications.NewCommentNotification;
import com.liamfarrell.android.snapbattle.notifications.NewFollowerNotification;
import com.liamfarrell.android.snapbattle.notifications.Notification;
import com.liamfarrell.android.snapbattle.notifications.TaggedInCommentNotification;
import com.liamfarrell.android.snapbattle.notifications.VideoSubmittedNotification;
import com.liamfarrell.android.snapbattle.notifications.VotingCompleteNotification;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import static com.liamfarrell.android.snapbattle.caches.ThumbnailCacheHelper.JSON_FACEBOOK_ID;

/**
 * Serializes and deserializes the NotificationLoader using normal java file serialization/deserialization.
 */
public class NotificationCacheFile {

    public static final String TAG = "NotificationCacheFile";

    private static final String JSON_NOTIFICATION_ID_LIST= "notification_id_list";
    private static final String JSON_HAS_ALL_NOTIFICATIONS_BEEN_SEEN= "has_all_notifications_been_seen";
    private static final String JSON_LAST_NOTIFICATION_COUNT = "last_notification_count";

    private Context context;
    private LinkedList<Notification> mNotificationIDList;
    private int mLastNotificationCount;
    private boolean mNotificationsHasBeenAllSeen;
    public static final int FILE_MAX_CAPACITY = 15;

    public NotificationCacheFile( Context Context, LinkedList<Notification> mNotificationIDList2, int LastNotificationCount, boolean allNotificationsSeen)
    {
        context = Context;

        mNotificationIDList = (LinkedList<Notification>) mNotificationIDList2.clone();
        mLastNotificationCount = LastNotificationCount;
        mNotificationsHasBeenAllSeen = allNotificationsSeen;

    }

    public NotificationCacheFile(Context context) throws FileNotFoundException, IOException
    {
        this.context = context;
        loadListFromFile();
    }


    public  LinkedList<Notification> getNotificationList()
    {
        return mNotificationIDList;
    }

    public boolean hasAllNotificationsSeen(){
        return mNotificationsHasBeenAllSeen;
    }

    public int getLastNotificationCount()
    {
        return mLastNotificationCount;
    }

    private String getFilename()
    {
        return  FacebookLoginFragment.getCredentialsProvider(context).getIdentityId() + "-" + "NotificationsJson";
    }



    private void loadListFromFile() throws IOException
    {
        try
        {
            File file = new File(context.getFilesDir(), getFilename());
            JSONObject content = new JSONObject(FileUtils.readFileToString(file));
            setJSONValues(content);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveListToFile()
    {
        while (mNotificationIDList.size() > FILE_MAX_CAPACITY)
        {
            mNotificationIDList.remove(mNotificationIDList.size() - 1);
        }

        try {

            File file = new File(context.getFilesDir(), getFilename());
            Writer output = new BufferedWriter( new FileWriter(file));
            output.write(getJSONObjectOfClass().toString());
            output.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    private JSONObject getJSONObjectOfClass() throws JSONException {
        Gson gson = getGsonWithRuntimeAdapter();
        JSONObject json = new JSONObject();
        json.put(JSON_FACEBOOK_ID,AccessToken.getCurrentAccessToken().getUserId() );
        json.put(JSON_LAST_NOTIFICATION_COUNT, mLastNotificationCount);
        json.put(JSON_HAS_ALL_NOTIFICATIONS_BEEN_SEEN, gson.toJson(mNotificationsHasBeenAllSeen));
        json.put(JSON_NOTIFICATION_ID_LIST, gson.toJsonTree(mNotificationIDList,  new com.google.gson.reflect.TypeToken<LinkedList<Notification>>(){}.getType()));
        Log.i(TAG, "JSON GET: " + json.toString());
        return json;
    }

    private void setJSONValues(JSONObject json) throws JSONException{
        Gson gson = getGsonWithRuntimeAdapter();
        Log.i(TAG, "Json string: " + json.toString());

        String facebookID = json.getString(JSON_FACEBOOK_ID);
        if (!facebookID.equals(AccessToken.getCurrentAccessToken().getUserId()))
        {
            //throw new error.. File verification failed
            throw new Error ("Cache File Corruption Detected");
        }
        mLastNotificationCount = json.getInt(JSON_LAST_NOTIFICATION_COUNT);
        mNotificationsHasBeenAllSeen = json.getBoolean(JSON_HAS_ALL_NOTIFICATIONS_BEEN_SEEN);
        mNotificationIDList  = gson.fromJson(json.getString(JSON_NOTIFICATION_ID_LIST), new com.google.gson.reflect.TypeToken<LinkedList<Notification>>(){}.getType());
    }

    private Gson getGsonWithRuntimeAdapter(){
        RuntimeTypeAdapterFactory<Notification> notificationRuntTimeAdapterFactory = RuntimeTypeAdapterFactory.of(Notification.class, "type")
                .registerSubtype(BattleAcceptedNotification.class, "battle_accepted")
                .registerSubtype(FullVideoUploadedNotification.class, "full_video_uploaded")
                .registerSubtype(NewBattleRequestNotification.class, "new_battle_request")
                .registerSubtype(NewCommentNotification.class, "new_comment")
                .registerSubtype(NewFollowerNotification.class, "new_follower")
                .registerSubtype(TaggedInCommentNotification.class, "tagged_in_comment")
                .registerSubtype(VideoSubmittedNotification.class, "video_submitted")
                .registerSubtype(VotingCompleteNotification.class, "voting_complete");

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(notificationRuntTimeAdapterFactory).create();
        return gson;
    }

}





