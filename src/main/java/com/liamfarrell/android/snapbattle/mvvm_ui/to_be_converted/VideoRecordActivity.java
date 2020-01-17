package com.liamfarrell.android.snapbattle.mvvm_ui.to_be_converted;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.Video;
import com.liamfarrell.android.snapbattle.mvvm_ui.ViewBattleFragment;
import com.liamfarrell.android.snapbattle.views.CameraPreview;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class VideoRecordActivity extends Activity {
    private static final String TAG = "VideoRecordActivity";

    public static final String EXTRA_VIDEO_ID = "com.liamfarrell.android.snapbattle.androidvideocaptureexample.VIDEO_ID";
    public static final String EXTRA_ORIENTATION_LOCK = "com.liamfarrell.android.snapbattle.extraorientationlock";
    public static final int MAX_VIDEO_LENGTH_MILLISECONDS = 60000;

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mediaRecorder;
    private Button mCaptureButton, mSwitchCameraButton;
    private Activity  myActivity;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    private String mFilename;
    private String orientationLock = Battle.ORIENTATION_LOCK_UNDEFINED;
    private int mCameraId;
    private Chronometer mChronometer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myActivity = this;

        //Get Intent for the orientation lock
        Intent  i = getIntent();
        orientationLock = i.getStringExtra(EXTRA_ORIENTATION_LOCK);

        // TODO check for tablets (getDeviceDefaultOrientation() == Configuration.ORIENTATION_LANDSCAPE)

        mFilename =   getFilesDir().getAbsolutePath() +  "/" + i.getIntExtra(ViewBattleFragment.VIDEO_ID_EXTRA, -1) + "_snap.mp4";
        initialize();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
            super.onConfigurationChanged(newConfig);
    }

    private void deleteFile()
    {
        File file = new File(mFilename);
        file.delete();
    }

    private void checkCorrectOrientation()
    {
        // Checks the orientation of the screen
        if (!orientationLock.equals(Battle.ORIENTATION_LOCK_UNDEFINED))
        {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                if (orientationLock.equals(Battle.ORIENTATION_LOCK_PORTRAIT))
                {
                    activateRotateBlock();
                    setRotateTextViewGoPortrait();
                }
                else
                {
                    deactivateRotateBlock();
                }
            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                if (orientationLock.equals(Battle.ORIENTATION_LOCK_LANDSCAPE))
                {
                    activateRotateBlock();
                    setRotateTextViewGoLandscape();
                }
                else
                {
                    deactivateRotateBlock();
                }
            }
        }
        else
        {
            deactivateRotateBlock();
        }
    }


    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    public void onResume() {
        Log.i(TAG, "on resume");
        super.onResume();
        if (!hasCamera(myActivity)) {
            Toast toast = Toast.makeText(myActivity, R.string.no_camera_toast, Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null)
        {
            Log.i(TAG, "mCamera = null");
            // if the front facing camera does not exist
            if (findFrontFacingCamera() < 0)
            {
                Toast.makeText(this, R.string.no_front_camera, Toast.LENGTH_LONG).show();
                mSwitchCameraButton.setVisibility(View.GONE);
            }
            mCamera = Camera.open(findFrontFacingCamera());
            mCameraId =  findFrontFacingCamera();
            mPreview.refreshCamera(this,  findFrontFacingCamera(), mCamera);

        }

        checkCorrectOrientation();




    }

    public void initialize() {
        cameraPreview =  findViewById(R.id.camera_preview);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            mPreview = new CameraPreview(myActivity, mCamera, CameraPreview.orientation_landscape);
        }
        else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            mPreview = new CameraPreview(myActivity, mCamera, CameraPreview.orientation_portrait);
        }

        cameraPreview.addView(mPreview);

        mCaptureButton =  findViewById(R.id.button_capture);
        mCaptureButton.setOnClickListener(captrureListener);

        mSwitchCameraButton = findViewById(R.id.button_ChangeCamera);
        mSwitchCameraButton.setOnClickListener(switchCameraListener);

        mChronometer = findViewById(R.id.chronometer);


    }

    OnClickListener switchCameraListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the number of cameras
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    // release the old camera instance
                    // switch camera, from the front and the back and vice versa
                    releaseCamera();
                    chooseCamera();
                } else {
                    Toast toast = Toast.makeText(myActivity, R.string.phone_1_camera_toast, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    };

    public void chooseCamera() {
        // if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview
                mCamera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                mCameraId = cameraId;
                mPreview.refreshCamera(this, cameraId, mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview
                mCamera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                mCameraId = cameraId;
                mPreview.refreshCamera(this, cameraId, mCamera);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // when on Pause, release camera in order to be used from other
        // applications
        releaseCamera();
    }

    private void stopRecording()
    {
        // stop recording and release camera

        try {
            mediaRecorder.stop(); // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object

            //start chronometer
            mChronometer.stop();


            //Save fileid as an intent extra and go back to main callbacks
            Intent data = new Intent();
            data.putExtra(EXTRA_VIDEO_ID, -1);
            data.putExtra(EXTRA_ORIENTATION_LOCK, orientationLock);
            setResult(RESULT_OK, data);

            recording = false;
            finish();
        }
        catch (RuntimeException e)
        {
            //Called when stop button is pressed too quickly after record pressed.
            //delete the file and exit the callbacks
            Toast.makeText(this, R.string.generic_error_toast, Toast.LENGTH_SHORT).show();
            deleteFile();
            releaseMediaRecorder();
            setResult(Activity.RESULT_CANCELED);
            finish();


        }


    }


    private boolean hasCamera(Context context) {
        // check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    boolean recording = false;
    OnClickListener captrureListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {

            //Log.i("Cameratag", "Rotation is: " + getWindowManager().getDefaultDisplay().getRotation());


            if (recording) {
                stopRecording();

            } else {
                lockScreenOrientation();
                if (!prepareMediaRecorder()) {

                    Toast.makeText(VideoRecordActivity.this, R.string.generic_error_toast, Toast.LENGTH_LONG).show();
                    finish();
                }
                // work on UiThread for better performance
                runOnUiThread(new Runnable() {
                    @SuppressLint("NewApi") public void run() {



                        try {
                            //disable the stop button for 3 seconds
                            mCaptureButton.setEnabled(false);
                            //reenable

                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mCaptureButton.setEnabled(true);
                                }
                            }, 3000);


                            mediaRecorder.start();

                        } catch (final Exception ex) {
                            // Log.i("---","Exception in thread");
                        }
                        mChronometer.setBase(SystemClock.elapsedRealtime());

                        mChronometer.start();

                        recording = true;


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        {
                            mCaptureButton.setBackground(getDrawable(R.drawable.stop_button));
                        } else
                        {
                            mCaptureButton.setBackground(getResources().getDrawable(R.drawable.stop_button));
                        }
                    }
                });





            }


        }
    };

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object=
            mediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }
    public List<Camera.Size> getSupportedVideoSizes(Camera camera) {
        if (camera.getParameters().getSupportedVideoSizes() != null) {
            return camera.getParameters().getSupportedVideoSizes();
        } else {
            // VideoPOJO sizes may be null, which indicates that all the supported
            // preview sizes are supported for video recording.
            return camera.getParameters().getSupportedPreviewSizes();
        }
    }
    private boolean prepareMediaRecorder() {


        mediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoEncodingBitRate(5000000);
        mediaRecorder.setAudioChannels(2);
        mediaRecorder.setVideoSize(720,480);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioEncodingBitRate(96000);

        File file = new File(mFilename);
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        mediaRecorder.setMaxDuration(MAX_VIDEO_LENGTH_MILLISECONDS); // Set max duration 20 sec.
        mediaRecorder.setMaxFileSize(50000000); // Set max file size 50M
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                Log.i(TAG, "What: " + what + ", extra: " + extra);
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording();
                }





            }
        });


        mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Log.i(TAG, "What: " + what + ", extra: " + extra);
            }
        });

        mediaRecorder.setOrientationHint(Video.rotateToOrientationHint(getWindowManager().getDefaultDisplay().getRotation(), cameraFront));

        Log.i(TAG, "Screen rotation: " + getWindowManager().getDefaultDisplay().getRotation() + ", To Hint: " +Video.rotateToOrientationHint(getWindowManager().getDefaultDisplay().getRotation(), cameraFront));
        Log.i(TAG, "Display Orientation: " + getCameraDisplayOrientation(this, mCameraId, mCamera));

        //Set the orientation Lock
        //If this is the first video.

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        Log.i(TAG, "Prepared!");
        return true;

    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    private void activateRotateBlock()
    {

        mCaptureButton.setVisibility(View.INVISIBLE);
        RelativeLayout lin = (RelativeLayout) findViewById(R.id.rotateMessageLayout);
        lin.setVisibility(View.VISIBLE);

    }

    private void deactivateRotateBlock()
    {
        RelativeLayout lin =  findViewById(R.id.rotateMessageLayout);

        lin.setVisibility(View.INVISIBLE);
        mCaptureButton.setVisibility(View.VISIBLE);
    }

    private void setRotateTextViewGoLandscape()
    {

        TextView rotateText  = findViewById(R.id.rotateDeviceTextView);
        rotateText.setText(R.string.rotate_to_landscape_message);

    }

    private void setRotateTextViewGoPortrait()
    {

        TextView rotateText  = findViewById(R.id.rotateDeviceTextView);
        rotateText.setText(R.string.rotate_to_portrait_message);

    }

    private int getCameraDisplayOrientation(Activity activity,
                                            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }


    private void lockScreenOrientation(){
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }



}