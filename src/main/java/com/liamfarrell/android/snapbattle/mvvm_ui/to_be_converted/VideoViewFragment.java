package com.liamfarrell.android.snapbattle.mvvm_ui.to_be_converted;


import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.liamfarrell.android.snapbattle.HideAndShowBottomNavigation;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.views.VideoController;

import timber.log.Timber;


public class VideoViewFragment extends VideoPlayerAbstractFragment
{

    private static final String TAG = "VideoPlayerAbstractFrag";

	public static final String VIDEO_TYPE_EXTRA = "com.liamfarrell.android.snapbattle.videoviewfragment.video_type_extra";
	public static final String VIDEO_ROTATION_LOCK_EXTRA = "com.liamfarrell.android.snapbattle.videoviewfragment.rotation_lock_extra";
    public static final String VIDEO_FILEPATH_EXTRA = "com.liamfarrell.android.snapbattle.videoviewfragment.video_filepath_extra";

	public static final int VIDEO_TYPE_LOCAL = 1;
	public static final int VIDEO_TYPE_STREAM = 2;

	private String filepath;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //filepath = getActivity().getIntent().getStringExtra(VIDEO_FILEPATH_EXTRA);
        filepath = getArguments().getString("filepath");
    }



    @Override
    protected void setVideoFilepath() {
        mVideoView.setVideoURI(Uri.parse(filepath));

        mVideoView.setOnCompletionListener(new OnCompletionListener()
        {

            @Override
            public void onCompletion(MediaPlayer mp) {
                getActivity().finish();
            }

        });



        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Timber.i("ON ERROR: what= " + what + "extra: " + extra);

                //if the signed url has expired. reset the video filepath. else exit callbacks
                if (what == 1) {
                    getActivity().finish();
                }
                else {
                    getActivity().finish();
                }
                return true;
            }
        });
        if (isPaused) {
            mMediaPlayer.seekTo(pausedPosition);
        }
        else{
            mVideoView.start();
        }
    }

    @Override
    protected void setVideoController(View v) {
        mVideoController = new VideoController(getActivity());
        mVideoController.setAnchorView((FrameLayout) v.findViewById(R.id.videoContainer));
    }

    @Override
    public void onResume() {
        super.onResume();
        //getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        ((HideAndShowBottomNavigation)getActivity()).hideBottomNavigation();
    }


    @Override
    public void onPause() {
        super.onPause();
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ((HideAndShowBottomNavigation) getActivity()).showBottomNavigation();
    }

}
