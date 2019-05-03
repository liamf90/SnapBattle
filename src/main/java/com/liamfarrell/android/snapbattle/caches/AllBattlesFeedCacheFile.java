package com.liamfarrell.android.snapbattle.caches;

import android.content.Context;
import android.util.Log;

import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.liamfarrell.android.snapbattle.model.Battle;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serializes and deserializes the AllBattlesFeedCache using normal java file serialization/deserialization.
 */

public class AllBattlesFeedCacheFile {

    public static final String TAG = "AllBattlesFeedCacheFile";

    private static final String JSON_FACEBOOK_ID = "facebookID";
    private static final String JSON_LAST_ALL_BATTLES_COUNT = "last_all_battles_count";
    private static final String JSON_LAST_TIME_UPDATED = "last_time_updated";
    private static final String JSON_BATTLE_LIST = "battle_list";
    private static final String JSON_ALL_BATTLES_MAP = "all_battles_map";

    private Context context;
    private ConcurrentHashMap<Integer, Battle> mAllBattlesMap;
    private LinkedList<Integer> mBattleIDList;
    private Date mLastTimeUpdated;
    private int mLastAllBattlesCount;
    public static final int FILE_MAX_CAPACITY = 15;

    public AllBattlesFeedCacheFile(Context Context, LinkedList<Integer> mBattleIDList2, ConcurrentHashMap<Integer, Battle> mAllBattlesMap2, int lastAllBattlesCount, Date LastTimeUpdated)
    {
        context = Context;
        mAllBattlesMap = new ConcurrentHashMap<Integer, Battle>(mAllBattlesMap2);
        mBattleIDList = new LinkedList<Integer>(mBattleIDList2);
        mLastAllBattlesCount = lastAllBattlesCount;
        mLastTimeUpdated = LastTimeUpdated;
    }

    public AllBattlesFeedCacheFile(Context context) throws FileNotFoundException, IOException, InvalidClassException
    {
        this.context = context;
        loadListFromFile();
    }

    public static void deleteFile(Context context) {
        context.deleteFile(getFilename());
    }




    public  LinkedList<Integer> getBattleList()
    {
        return mBattleIDList;
    }

    public   ConcurrentHashMap<Integer, Battle> getBattleMap()
    {
        return mAllBattlesMap;
    }

    public Battle getBattle(int friendBattleID)
    {
        return mAllBattlesMap.get(friendBattleID);
    }
    public int getLastAllBattlesCount()
    {
        return mLastAllBattlesCount;
    }


    private static String getFilename()
    {
        return  AccessToken.getCurrentAccessToken().getUserId() + "-" + "allBattles2";
    }

    public Date getLastTimeUpdated()
    {
        return mLastTimeUpdated;
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
        for (int x= mBattleIDList.size() -1; x >= FILE_MAX_CAPACITY; x--)
        {
            Log.i(TAG, "Removing from mAllBattlesMap before save: " + mBattleIDList.get(x));
            mAllBattlesMap.remove(mBattleIDList.remove(x));
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
        JSONObject json = new JSONObject();
        json.put(JSON_FACEBOOK_ID,AccessToken.getCurrentAccessToken().getUserId() );
        json.put(JSON_LAST_ALL_BATTLES_COUNT, mLastAllBattlesCount);
        json.put(JSON_LAST_TIME_UPDATED, new Gson().toJson(mLastTimeUpdated));
        json.put(JSON_BATTLE_LIST, new Gson().toJsonTree(mBattleIDList));
        json.put(JSON_ALL_BATTLES_MAP, new Gson().toJsonTree(mAllBattlesMap));
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
        mLastAllBattlesCount = json.getInt(JSON_LAST_ALL_BATTLES_COUNT);
        mLastTimeUpdated = gson.fromJson(json.getString(JSON_LAST_TIME_UPDATED), new com.google.gson.reflect.TypeToken<Date>(){}.getType());
        mBattleIDList  = gson.fromJson(json.getString(JSON_BATTLE_LIST), new com.google.gson.reflect.TypeToken<LinkedList<Integer>>(){}.getType());
        mAllBattlesMap = gson.fromJson(json.getString(JSON_ALL_BATTLES_MAP), new com.google.gson.reflect.TypeToken<ConcurrentHashMap<Integer, Battle>>(){}.getType());
    }


}





