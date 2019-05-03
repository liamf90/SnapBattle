package com.liamfarrell.android.snapbattle.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.liamfarrell.android.snapbattle.app.App;
import com.liamfarrell.android.snapbattle.R;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Video 
{
	private int mVideoID;
	private Date mDateUploaded;
	private int mVideoNumber;
	private String mCreatorCognito_id;
	private String mCreatorName;
	private boolean mUploaded;

	public Video(int videoID, Date dateUploaded, int videoNumber, String creatorCognito_id, String creatorName, boolean uploaded) {
		mVideoID = videoID;
		mDateUploaded = dateUploaded;
		mVideoNumber = videoNumber;
		mCreatorCognito_id = creatorCognito_id;
		mCreatorName = creatorName;
		mUploaded = uploaded;
	}

	

	public static String getTimeSince(Date dateBeforeNow)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date timeNow = cal.getTime();
		return getTimeBetween(dateBeforeNow, timeNow, false) + " " +  App.getContext().getResources().getText(R.string.ago);
	}
	public static String getTimeSinceShorthand(Date dateBeforeNow)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date timeNow = cal.getTime();
		return getTimeBetween(dateBeforeNow, timeNow, true) + " ";
	}
	//TimeBefore and TimeAfter
	public static String getTimeUntil(Date dateAfterNow)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date timeNow = cal.getTime();
		
		return getTimeBetween(timeNow, dateAfterNow, false);
	}

	private static String getTimeBetween(Date before, Date after, boolean shortHandVersion)
	{
		if (after == null || before == null)
		{
			return "";
		}




		long timeDifference = after.getTime() - before.getTime();
		long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long yearsInMilli = daysInMilli * 365;
        
        long totalMinutes = timeDifference / minutesInMilli;
        long totalHours = timeDifference / hoursInMilli;
        long totalDays = timeDifference / daysInMilli;
        long totalYears = timeDifference / yearsInMilli;
       	long elapsedYears = timeDifference / yearsInMilli;
        
        long elapsedDays = timeDifference / daysInMilli;
        timeDifference = timeDifference % daysInMilli;
 
        long elapsedHours = timeDifference / hoursInMilli;
        timeDifference = timeDifference % hoursInMilli;
 
        long elapsedMinutes = timeDifference / minutesInMilli;
        String timeSinceString = "";
		Resources res = App.getContext().getResources();
        
        if (totalMinutes ==1 )
        {
			if (shortHandVersion) {
				timeSinceString = elapsedMinutes + " " + res.getText(R.string.minute_shorthand).toString();
			} else{
				timeSinceString = elapsedMinutes + " " + res.getText(R.string.minute).toString();
			}
        }
        else if (totalMinutes < 60)
        {
			if (shortHandVersion) {
				timeSinceString = elapsedMinutes +" " +  res.getText(R.string.minute_shorthand).toString();
			} else{
				timeSinceString = elapsedMinutes +" " +  res.getText(R.string.minutes).toString();
			}
        }
        else if (totalHours == 1)
        {
			if (shortHandVersion) {
				timeSinceString = elapsedHours  + " " + res.getText(R.string.hour_shorthand).toString();
			} else{
				timeSinceString = elapsedHours  + " " + res.getText(R.string.hour).toString();
			}
        }
        else if (totalHours < 24)
        {
			if (shortHandVersion) {
				timeSinceString = elapsedHours  + " " + res.getText(R.string.hour_shorthand).toString();
			} else{
				timeSinceString = elapsedHours  + " " + res.getText(R.string.hours).toString();
			}
        }
        else if (totalDays == 1)
        {
			if (shortHandVersion) {
				timeSinceString = elapsedDays  +" " +  res.getText(R.string.days_shorthand).toString();
			} else{
				timeSinceString = elapsedDays  +" " +  res.getText(R.string.day).toString();
			}
        }
        else if (totalDays < 365)
        {
			if (shortHandVersion) {
				timeSinceString = elapsedDays  + " " + res.getText(R.string.days_shorthand).toString();
			} else{
				timeSinceString = elapsedDays  + " " + res.getText(R.string.days).toString();
			}
        }
        else if (totalYears == 1)
        {
			if (shortHandVersion) {
				timeSinceString = elapsedYears + " " + res.getText(R.string.years_shorthand).toString();
			} else{
				timeSinceString = elapsedYears + " " + res.getText(R.string.year).toString();
			}
        }
        else if (totalYears > 1)
        {
			if (shortHandVersion) {
				timeSinceString = elapsedYears  + " " +  res.getText(R.string.years_shorthand).toString();
			} else{
				timeSinceString = elapsedYears  + " " +  res.getText(R.string.years).toString();
			}
        }
        return timeSinceString;
	}
	
	public String getTimeSinceUploaded()
	{
		return getTimeSince(mDateUploaded);

	}
	
	public int getVideoID() {
		return mVideoID;
	}
	
	public boolean isUploaded()
	{
		return mUploaded;
	}

	public void setVideoID(int videoID) {
		mVideoID = videoID;
	}

	public String getVideoFilename() 
	{
		return mVideoID + "_snap.mp4";
	}
	
	public String getVideoFilepath(Context context)
	{
		return context.getFilesDir().getAbsolutePath() + "/" + getVideoFilename();
	}
	
	public enum videoStatus
	{
		SENT,
		RECEIVED,
		YOUR_TURN,
		OPPONENT_TURN,
		ERROR,
		OPPONENT_FUTURE,
		YOUR_FUTURE
	}
	
	
	public videoStatus getVideoStatus(int videoNumberUploaded, String currentUserCognitoID)
	{	
		

		videoStatus vidStatus = videoStatus.ERROR;
	
		if ((videoNumberUploaded == (mVideoNumber - 1)) &&
				mCreatorCognito_id.equals(currentUserCognitoID))

		{
			vidStatus = videoStatus.YOUR_TURN;
		}
		else if ((videoNumberUploaded == (mVideoNumber - 1)) &&
				!mCreatorCognito_id.equals(currentUserCognitoID))
		{
			vidStatus = videoStatus.OPPONENT_TURN;
		}
		else if ((videoNumberUploaded >= mVideoNumber)
				&& mCreatorCognito_id.equals(currentUserCognitoID))
		{
			vidStatus = videoStatus.SENT;
		}
		else if ((videoNumberUploaded >= mVideoNumber)
				&& !mCreatorCognito_id.equals(currentUserCognitoID))
		{
			vidStatus = videoStatus.RECEIVED;
		}
		else if ((mVideoNumber > (videoNumberUploaded + 1))
				&& !mCreatorCognito_id.equals(currentUserCognitoID))
		{
			vidStatus = videoStatus.OPPONENT_FUTURE;
		}
		else if ((mVideoNumber > (videoNumberUploaded + 1))
				&& mCreatorCognito_id.equals(currentUserCognitoID))
		{
			vidStatus = videoStatus.YOUR_FUTURE;
		}
		return vidStatus;
	}


	public int getVideoNumber() {
		return mVideoNumber;
	}

	public void setVideoNumber(int videoNumber) {
		mVideoNumber = videoNumber;
	}

	public String getCreatorCognito_id() {
		return mCreatorCognito_id;
	}

	public void setCreatorCognito_id(String creatorCognito_id) {
		mCreatorCognito_id = creatorCognito_id;
	}
	
	public String getVideoOwnerName()
	{
		return mCreatorName;
	}
	
	public int getRoundNumber()
	{
		switch (mVideoNumber)
		{
			case 1 : return 1;
			case 2 : return 1;
			case 3 : return 2;
			case 4 : return 2;
			case 5 : return 3;
			case 6 : return 3;
			case 7 : return 4;
			case 8 : return 4;
			case 9 : return 5;
			case 10 : return 5;
			default : return 0;
		}
	}

	public boolean isCurrentUser(String currentUserCognitoID)
	{
		if (mCreatorCognito_id.equals(currentUserCognitoID))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	public boolean displayPlayButton(Activity activity)
	{
		//Display play button if video has been completed.
		//If the video has no filename, it has not been completed
		
		File file  = new File(activity.getFilesDir().getAbsolutePath() + "/" + getVideoFilename());
		Log.i("Round", "DISPLAY PLAY BUTTON. File exists: " + file.exists());
		//The mIslocal boolean here so we can notifydatasetchanged after we record a video
		//this enabled us to display the play button after a video is recorded as the data actually changes.
		
		if (file.exists()|| isUploaded())
		{
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean displayRecordButton(int battleVideoNumberCompleted, String currentUserCognitoID)
	{
		//Display record button if video is the current users video.
		//and if it is the next video
		//The next video is 1 video after the completed video number
		if ((mVideoNumber == (battleVideoNumberCompleted + 1)) &&
				mCreatorCognito_id.equals(currentUserCognitoID))
		{
			return true;
		}
		else
		{	
			
			return false;
		}
	}
	
	public boolean displaySubmitButton(Activity activity, int battleVideoNumberCompleted, String currentUserCognitoID)
	{
		//Display submit button if the video is next video and is current users video
		//and if it has just been recorded.
		File file  = new File(activity.getFilesDir().getAbsolutePath() +  "/"   + getVideoFilename());
		return (mVideoNumber == (battleVideoNumberCompleted + 1))
				&& mCreatorCognito_id.equals(currentUserCognitoID)
				&& file.exists();
			
	}
	
	public static int rotateToOrientationHint(int rotation, boolean frontFacingCamera)
	{
		if (frontFacingCamera)
		{
			if (rotation == android.view.Surface.ROTATION_0)
			{
				//natural tall screen. This is portrait.
				return 270;
			}
			if (rotation == android.view.Surface.ROTATION_90)
			{
				//landscape
				return 0;
			}
			if (rotation == android.view.Surface.ROTATION_180)
			{
				
				//vertical
				return 90;
			}
			if (rotation == android.view.Surface.ROTATION_270)
			{
				//landscape
				return 180;
			}
		}
		else if (!frontFacingCamera)
		{
			if (rotation == android.view.Surface.ROTATION_0)
			{
				//natural tall screen. This is portrait.
				return 90;
			}
			if (rotation == android.view.Surface.ROTATION_90)
			{
				//landscape
				return 0;
			}
			if (rotation == android.view.Surface.ROTATION_180)
			{
				
				//vertical
				return 270;
			}
			if (rotation == android.view.Surface.ROTATION_270)
			{
				//landscape
				return 180;
			}
		}
		return 0;
	}
	public static String orientationHintToLock(int rotation)
	{
		if (rotation == 270)
		{
			return Battle.ORIENTATION_LOCK_PORTRAIT;
		}
		if (rotation == 0)
		{
			return Battle.ORIENTATION_LOCK_LANDSCAPE;
		}
		if (rotation == 180)
		{
			return Battle.ORIENTATION_LOCK_LANDSCAPE;
		}
		if (rotation == 90)
		{
			return Battle.ORIENTATION_LOCK_PORTRAIT;
		}
		return Battle.ORIENTATION_LOCK_UNDEFINED;
	}
	
	public static String getVideoRotation(Context con, Video vid)
	{
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		File file = new File(con.getFilesDir().getAbsolutePath() + "/" + vid.getVideoFilename());
		mmr.setDataSource(con.getFilesDir().getAbsolutePath() + "/" + vid.getVideoFilename());
		return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
	}

	public void deleteVideo(Context context)
	{
		File f = new File(getVideoFilepath(context));
		f.delete();
	}

	

}
