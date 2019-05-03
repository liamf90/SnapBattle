package com.liamfarrell.android.snapbattle.caches;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.liamfarrell.android.snapbattle.activity.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liamf on 27/02/2018.
 *
 * An Singleton cache for quickly retrieving the profile pic signed url for other users (not the current user).
 * The profile pic update count is also stored to check if the signed url is for the most current profile picture.
 * This cache loads and saves the ProfilePicCacheObject map using normal java file serialization / deserializtion
 *
 */

public class OtherUsersProfilePicCacheManager implements Serializable{

    private static final String TAG = "SmallProfilePicCache";

    private static final String JSON_PROFILE_PIC_COUNT_MAP = "profile_pic_count_map";
    private static final String JSON_FACEBOOK_ID = "facebookID";

    public interface SignedUrlCallback
    {
       void onSignedPicReceived(String signedUrl);
    }

    private ConcurrentHashMap<String, ProfilePicCacheObject> mProfilePicCountCacheHashMap;
    private static OtherUsersProfilePicCacheManager sProfilePicCache;
    private OtherUsersProfilePicCacheManager(Context context)
    {
        try {
            loadListFromFile(context);

        }
        catch (IOException e)
        {
            //no saved file
            mProfilePicCountCacheHashMap = new ConcurrentHashMap<>();
            saveFile(context);
        }

    }

    public static OtherUsersProfilePicCacheManager getProfilePicCache(Context context)
    {
        if (sProfilePicCache == null) {
            sProfilePicCache = new OtherUsersProfilePicCacheManager(context);
        }
        return sProfilePicCache;
    }


    public static void closeCache(){
        sProfilePicCache = null;
    }

    public String getSignedUrlProfilePicOpponent(String cognitoIDuser)
    {
        if (mProfilePicCountCacheHashMap.containsKey(cognitoIDuser)) {
            return mProfilePicCountCacheHashMap.get(cognitoIDuser).getLastSavedSignedUrl();
        }else {
            return null;
        }
    }

    public Integer getProfilePicCount(String cognitoIDUser)
    {
        if (mProfilePicCountCacheHashMap.containsKey(cognitoIDUser)) {
            return mProfilePicCountCacheHashMap.get(cognitoIDUser).getProfilePicCount();
        }else {
            return null;
        }

    }

    public void updateSignedUrlProfilePicOpponent(final String cognitoIDUser, final int newProfilePicCount, final String newSignedUrl, final Context context)
    {
        mProfilePicCountCacheHashMap.put(cognitoIDUser, new ProfilePicCacheObject(newProfilePicCount, newSignedUrl));
        saveFile(context);
    }


    public void getSignedUrlProfilePicOpponent(final String cognitoIDUser, final int newProfilePicCount, final String newSignedUrl, final Context context, final SignedUrlCallback callback)
    {
        Log.i(TAG, "Get Signed Url Pic opponent. cognitoID: " + cognitoIDUser + ", profilepiccount: " + newProfilePicCount);
        Log.i(TAG, "New signed url: " + newSignedUrl);
        if (mProfilePicCountCacheHashMap.containsKey(cognitoIDUser) && mProfilePicCountCacheHashMap.get(cognitoIDUser).getProfilePicCount() == newProfilePicCount)
        {
            //profile pic has not been updated. can use the old signed url to get the photo from cache if its still there

            final String signedUrlCache = mProfilePicCountCacheHashMap.get(cognitoIDUser).getLastSavedSignedUrl();
            Log.i(TAG, "Signed url cache: " + signedUrlCache);
            Picasso.get().load(signedUrlCache).networkPolicy(NetworkPolicy.OFFLINE).fetch(new Callback() {
              @Override
              public void onSuccess() {
                  Log.i(TAG, "On success in cache: signed url: " + signedUrlCache);
                  //profile pic in cache. use cached signed url
                  callback.onSignedPicReceived(signedUrlCache);

              }

              @Override
              public void onError(Exception e) {
                  Log.i(TAG, "on error. use new signed url: " + newSignedUrl);
                  //use new signed url
                  mProfilePicCountCacheHashMap.put(cognitoIDUser, new ProfilePicCacheObject(newProfilePicCount, newSignedUrl));
                  saveFile(context);
                  callback.onSignedPicReceived(newSignedUrl);
              }
          });



        }
        else
        {
            Log.i(TAG, "Not in cache. update the cache map. use new signed url");
            //not in cache
            //profile pic changed. use new signedurl and update the cache map.
            mProfilePicCountCacheHashMap.put(cognitoIDUser, new ProfilePicCacheObject(newProfilePicCount, newSignedUrl));
            saveFile(context);
            callback.onSignedPicReceived(newSignedUrl);
        }
    }




    private void loadListFromFile(Context context) throws IOException
    {
        try
        {
            File file = new File(context.getFilesDir(), getFilename(context));
            JSONObject content = new JSONObject(FileUtils.readFileToString(file));
            setJSONValues(content);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveFile(Context context)
    {
        try {
            File file = new File(context.getFilesDir(), getFilename(context));
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
        JSONObject json = new JSONObject();
        json.put(JSON_FACEBOOK_ID, AccessToken.getCurrentAccessToken().getUserId() );
        json.put(JSON_PROFILE_PIC_COUNT_MAP,  new Gson().toJsonTree(mProfilePicCountCacheHashMap));
        return json;
    }

    private void setJSONValues(JSONObject json) throws JSONException{
        Log.i(TAG, "Json string: " + json.toString());
        Gson gson = new Gson();
        String facebookID = json.getString(JSON_FACEBOOK_ID);
        if (!facebookID.equals(AccessToken.getCurrentAccessToken().getUserId()))
        {
            //throw new error.. File verification failed
            throw new Error ("Cache File Corruption Detected");
        }
        mProfilePicCountCacheHashMap = gson.fromJson(json.getString(JSON_PROFILE_PIC_COUNT_MAP), new com.google.gson.reflect.TypeToken<ConcurrentHashMap<String, ProfilePicCacheObject>>(){}.getType());
    }


    @NonNull
    private String getFilename(Context context)
    {
        return  FacebookLoginFragment.getCredentialsProvider(context).getIdentityId() + "-" + "SmallProfilePicCacheJson";
    }


    private class ProfilePicCacheObject
    {
        private int mProfilePicCount;
        private String mLastSavedSignedUrl;

        private ProfilePicCacheObject(int profilePicCount, String signedUrl)
        {
            mProfilePicCount = profilePicCount;
            mLastSavedSignedUrl = signedUrl;
        }
        private int getProfilePicCount() {
            return mProfilePicCount;
        }

        private String getLastSavedSignedUrl() {
            return mLastSavedSignedUrl;
        }


    }





}
