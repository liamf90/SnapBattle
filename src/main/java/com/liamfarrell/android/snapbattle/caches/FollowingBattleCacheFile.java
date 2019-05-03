package com.liamfarrell.android.snapbattle.caches;

import android.content.Context;
import android.util.Log;

import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.BattleIDSave;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serializes and deserializes the FollowingBattleCache using normal java file serialization/deserialization.
 */

public class FollowingBattleCacheFile {
	
	public static final String TAG = "FollowingBattleCacheFil";

	private static final String JSON_FACEBOOK_ID = "facebookID";
	private static final String JSON_LAST_FOLLOWING_BATTLE_COUNT = "last_following_battles_count";
	private static final String JSON_LAST_TIME_UPDATED = "last_time_updated";
	private static final String JSON_BATTLE_LIST = "battle_list";
	private static final String JSON_FOLLOWING_BATTLE_MAP = "following_battles_map";
	public static final String JSON_FULL_FIELD_UPDATE_COUNT = "full_field_update_count";

	private Context context;
	private ConcurrentHashMap<Integer, Battle> mFriendBattleMap;
	private LinkedList<BattleIDSave> mBattleIDList;
	private Date mLastTimeUpdated;
	private int mLastFriendBattleCount;
	private int mFullFeedUpdateCount;
	public static final int FILE_MAX_CAPACITY = 15;

	public FollowingBattleCacheFile(Context Context, LinkedList<BattleIDSave> mBattleIDList2, ConcurrentHashMap<Integer, Battle> mFriendBattleMap2, int LastFriendBattleCount, int fullfeedupdateCount, Date LastTimeUpdated)
	{
		context = Context;
		mFriendBattleMap = new ConcurrentHashMap<> (mFriendBattleMap2);
		mBattleIDList = (LinkedList<BattleIDSave>) mBattleIDList2.clone();
		mLastFriendBattleCount = LastFriendBattleCount;
		mLastTimeUpdated = LastTimeUpdated;
		mFullFeedUpdateCount = fullfeedupdateCount;
	}
	
	public FollowingBattleCacheFile(Context context) throws FileNotFoundException, IOException, InvalidClassException
	{
		this.context = context;
		loadListFromFile();
	}

	public static void deleteFile(Context context) {
		context.deleteFile(getFilename());
	}




	public  LinkedList<BattleIDSave> getBattleList()
	{
		return mBattleIDList;
	}
	
	public   ConcurrentHashMap<Integer, Battle> getFriendBattleMap()
	{
		return mFriendBattleMap;
	}
	
	public Battle getFriendBattle(int friendBattleID)
	{
		return mFriendBattleMap.get(friendBattleID);
	}
	public int getLastFriendBattleCount()
	{
		return mLastFriendBattleCount;
	}

	public int getFullFeedUpdateCount()
	{
		return mFullFeedUpdateCount;
	}
	
	private static String getFilename()
	{
		return  AccessToken.getCurrentAccessToken().getUserId() + "-" + "friendsBattlesjson";
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
		for (int x= mBattleIDList.size() -1; x >= FILE_MAX_CAPACITY; x--) {
			Log.i(TAG, "Removing from mFriendBattleMap before save: " + mBattleIDList.get(x));
			BattleIDSave battleIDSaveRemoved = mBattleIDList.remove(x);
			mFriendBattleMap.remove(battleIDSaveRemoved.getBattleID());
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
		json.put(JSON_FULL_FIELD_UPDATE_COUNT, mFullFeedUpdateCount);
		json.put(JSON_LAST_FOLLOWING_BATTLE_COUNT, mLastFriendBattleCount);
		json.put(JSON_LAST_TIME_UPDATED, new Gson().toJson(mLastTimeUpdated));
		json.put(JSON_BATTLE_LIST, new Gson().toJsonTree(mBattleIDList));
		json.put(JSON_FOLLOWING_BATTLE_MAP, new Gson().toJsonTree(mFriendBattleMap));
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
		mLastFriendBattleCount = json.getInt(JSON_LAST_FOLLOWING_BATTLE_COUNT);
		mLastTimeUpdated = gson.fromJson(json.getString(JSON_LAST_TIME_UPDATED), new com.google.gson.reflect.TypeToken<Date>(){}.getType());
		mBattleIDList  = gson.fromJson(json.getString(JSON_BATTLE_LIST), new com.google.gson.reflect.TypeToken<LinkedList<BattleIDSave>>(){}.getType());
		mFriendBattleMap = gson.fromJson(json.getString(JSON_FOLLOWING_BATTLE_MAP), new com.google.gson.reflect.TypeToken<ConcurrentHashMap<Integer, Battle>>(){}.getType());
	}
	
}
	
	
	
	
	
	