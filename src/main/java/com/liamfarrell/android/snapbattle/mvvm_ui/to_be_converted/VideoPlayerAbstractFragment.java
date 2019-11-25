package com.liamfarrell.android.snapbattle.mvvm_ui.to_be_converted;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.VideoView;

import com.liamfarrell.android.snapbattle.HideAndShowBottomNavigation;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.views.VideoController;

import java.lang.reflect.Field;

/**
 * Created by Liam on 20/01/2018.
 */

public abstract class VideoPlayerAbstractFragment extends Fragment implements VideoController.MediaPlayerControl {
    private static final String TAG = "FullBattleVidActivity";

    protected VideoView mVideoView;
    protected VideoController mVideoController;
    protected MediaPlayer mMediaPlayer;
    protected boolean isPaused = false;
    protected int pausedPosition = 0;
    private Thread increaseVideoViewDelayThread;



    public interface ViewCountCallback
    {
        void onViewCountIncrease();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_video_player, container, false);

        //show and hide the controller on video view click
        mVideoView = v.findViewById(R.id.VideoView);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    if (mVideoController.isShowing())
                    {
                        mVideoController.hide();
                    }
                    else
                    {
                        mVideoController.show();
                    }

                }
                return false;
            }
        });

        setVideoController(v);
        setVideoFilepath();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {

            public void onPrepared(MediaPlayer mp)
            {
                Log.i(TAG, "VideoView On Prepared");
                mMediaPlayer = mp;
                mVideoController.setMediaPlayer(mVideoView);

                if (isPaused)
                {
                    Log.i(TAG, "VideoView On Prepared - is PAUSED");
                    mMediaPlayer.seekTo(pausedPosition);
                    mVideoView.pause();
                }
                else {
                    Log.i(TAG, "VideoView On Prepared - is NOT Paused");
                    mMediaPlayer.start();
                    mVideoController.updatePausePlay();
                }
            }
        });


        return v;
    }


    protected abstract void setVideoFilepath();
    protected abstract void setVideoController(View v);


    protected void setUpVideoViewCountIncreaser(final ViewCountCallback viewCountIncreaseCallback)
    {
        //if user watches the video for more than 5 seconds, add video view count for the battle
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {


                    int elapsedTimeMillis = 0;
                    while(mMediaPlayer == null)
                    {
                        Thread.sleep(1);
                    }
                    boolean done = false;
                    while(mMediaPlayer != null && !done)
                    {

                        while (mMediaPlayer.isPlaying()) {

                            Thread.sleep(1);
                            elapsedTimeMillis++;
                            if (elapsedTimeMillis == 5000) {
                                viewCountIncreaseCallback.onViewCountIncrease();
                                done = true;
                                break;
                            }

                        }

                    }


                }

                 catch (InterruptedException e) {
                    Log.i(TAG, "Interrupted");

                }
                catch (IllegalStateException e)
                {
                    Log.i(TAG, "interrupted");
                }

            }


        };
        increaseVideoViewDelayThread = new Thread(r);
        increaseVideoViewDelayThread.start();

    }



    @Override
    public void start() {
        Log.i(TAG, "Start Pressed");
        mVideoView.start();
    }
    @Override
    public void onStop()
    {
        super.onStop();
        if (increaseVideoViewDelayThread != null){
            increaseVideoViewDelayThread.interrupt();
        }
    }

    @Override
    public void pause() {

        Log.i(TAG, "Pause Pressed.");
        Uri mUri = null;
        try {
            Field mUriField = VideoView.class.getDeclaredField("mUri");
            mUriField.setAccessible(true);
            mUri = (Uri)mUriField.get(mVideoView);
            Log.i(TAG, "URI: " + mUri.toString());
        } catch(Exception e) {}
        mVideoView.pause();
    }

    @Override
    public int getDuration() {
        //return player.getDuration();
        return mVideoView.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        //return player.getCurrentPosition();
        return mVideoView.getCurrentPosition();
    }


    @Override
    public void seekTo(int pos) {
        //player.seekTo(pos);
        mVideoView.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        //return player.isPlaying();
        Log.i(TAG, "Is Playing??");
        return mVideoView.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }




    @Override
    public void onPause()
    {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                Log.i(TAG, "MediaPLayer is playing");
                pausedPosition = mMediaPlayer.getCurrentPosition();
                mMediaPlayer.pause();
                isPaused = true;
            }
        } catch (java.lang.IllegalStateException e)
        {
            //do nothing.
        }


        super.onPause();

    }













}