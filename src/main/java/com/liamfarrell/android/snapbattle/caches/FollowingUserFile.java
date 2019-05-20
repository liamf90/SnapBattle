package com.liamfarrell.android.snapbattle.caches;

import android.content.Context;

import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.model.User;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serializes and deserializes the FollowingUserCache using normal java file serialization/deserialization.
 */
public class FollowingUserFile
{

	private static final String JSON_FACEBOOK_ID = "facebook_id";
	private static final String JSON_UPDATE_COUNT = "update_count";
	private static final String JSON_LAST_TIME_UPDATED = "last_time_updated";
	private static final String JSON_FOLLOWER_COGNITO_MAP = "follower_cognito_map";

	private ConcurrentHashMap<String, User> mFollowerCognitoIDMap;
	private int mUpdateCount;
	private Date mLastTimeUpdated;
	private Context mContext;
	
	
	public FollowingUserFile(Context context, ConcurrentHashMap<String, User> followerCognitoIDMap, int updatedActionCount, Date lastTimeUpdated)
	{
		mContext = context;
		mFollowerCognitoIDMap =  new ConcurrentHashMap<> (followerCognitoIDMap);
		mUpdateCount = updatedActionCount;
		mLastTimeUpdated = lastTimeUpdated;
		
	}
	public FollowingUserFile(Context context)throws FileNotFoundException, IOException
	{
		mContext = context;
		loadListFromFile();
	}
	public Date getLastTimeUpdated()
	{
		return mLastTimeUpdated;
	}
	
	public ConcurrentHashMap<String, User> getFollowerCognitoIDMap()
	{
		return mFollowerCognitoIDMap;
	}
	
	public int getUpdateCount()
	{
		return mUpdateCount;
	}
	
	
	private String getFilename()
	{
		return FacebookLoginFragment.getCredentialsProvider(mContext).getCachedIdentityId() + "-FollowerListJson";

	}




	private void loadListFromFile() throws IOException
	{
		try
		{
			File file = new File(mContext.getFilesDir(), getFilename());
			JSONObject content = new JSONObject(FileUtils.readFileToString(file));
			setJSONValues(content);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void saveListToFile(Context context)
	{

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
		json.put(JSON_FACEBOOK_ID, AccessToken.getCurrentAccessToken().getUserId() );
		json.put(JSON_UPDATE_COUNT, mUpdateCount);
		json.put(JSON_LAST_TIME_UPDATED, new Gson().toJson(mLastTimeUpdated));
		json.put(JSON_FOLLOWER_COGNITO_MAP, new Gson().toJsonTree(mFollowerCognitoIDMap));
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
		mUpdateCount = json.getInt(JSON_UPDATE_COUNT);
		mLastTimeUpdated = gson.fromJson(json.getString(JSON_LAST_TIME_UPDATED), new com.google.gson.reflect.TypeToken<Date>(){}.getType());
		mFollowerCognitoIDMap = gson.fromJson(json.getString(JSON_FOLLOWER_COGNITO_MAP), new com.google.gson.reflect.TypeToken<ConcurrentHashMap<String, User>>(){}.getType());
	}

}
