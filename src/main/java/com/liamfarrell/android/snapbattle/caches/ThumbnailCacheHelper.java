package com.liamfarrell.android.snapbattle.caches;

import android.content.Context;
import android.util.Log;

import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.liamfarrell.android.snapbattle.activity.FacebookLoginFragment;

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
import java.io.Writer;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Liam on 31/01/2018.
 *
 * An singleton which stores the signed urls for battle thumbnails in a map (Key = battleid, Object = Signed Url)
 *
 */

public class ThumbnailCacheHelper {
    public static final String JSON_FACEBOOK_ID = "facebook_id";
    public static final String JSON_THUMBNAIL_URL_MAP = "thumbnail_url_map";
    private ConcurrentHashMap<Integer, String> mImageUrlMap;
    private static  ThumbnailCacheHelper sThumbnailCacheHelper;
    public static ThumbnailCacheHelper get(Context context)
    {
        if (sThumbnailCacheHelper == null)
        {
            sThumbnailCacheHelper = new ThumbnailCacheHelper(context);
        }

        return sThumbnailCacheHelper;

    }

    public static void closeCache(){
        sThumbnailCacheHelper = null;
    }

    private ThumbnailCacheHelper(Context context)
    {

        try
        {
            loadCacheMap(context);
        }
        catch (IOException e)
        {
            Log.i("ThumbnailHelper", "IO Exception Load");
            e.printStackTrace();
            mImageUrlMap = new ConcurrentHashMap<>();
        }
    }







    private void loadCacheMap(Context context) throws IOException
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

    public void saveCacheMap(Context context)
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
        json.put(JSON_THUMBNAIL_URL_MAP, new Gson().toJsonTree(mImageUrlMap));
        return json;
    }

    private void setJSONValues(JSONObject json) throws JSONException{
        Gson gson = new Gson();
        String facebookID = json.getString(JSON_FACEBOOK_ID);
        if (!facebookID.equals(AccessToken.getCurrentAccessToken().getUserId()))
        {
            //throw new error.. File verification failed
            throw new Error ("Cache File Corruption Detected");
        }
        mImageUrlMap = gson.fromJson(json.getString(JSON_THUMBNAIL_URL_MAP), new com.google.gson.reflect.TypeToken< ConcurrentHashMap<Integer, String>>(){}.getType());
    }


    private String getFilename(Context context)
    {
        return FacebookLoginFragment.getCredentialsProvider(context).getIdentityId() + "-thumbnailImageCacheJson";
    }

    public String getThumbnailPicOldUrl(int battleID)
    {

            return mImageUrlMap.get(battleID);

    }

    public void putSignedUrl(Context context, int battleID, String newSignedUrl)
    {
        mImageUrlMap.put(battleID, newSignedUrl);
        saveCacheMap(context);
    }













}
