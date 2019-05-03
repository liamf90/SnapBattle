package com.liamfarrell.android.snapbattle.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	public static final int orientation_portrait = 0;
	public static final int orientation_landscape = 1;
	
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private Activity activity;
	private int mCameraId;
	
	public CameraPreview(Context context, Camera camera, int orientation) {
		super(context);
		mCamera = camera;

		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		
		try {
			// create the surface and start camera preview
			if (mCamera == null) {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
				
			
			}
		} catch (IOException e) {
			Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	public void refreshCamera(Activity activity, int cameraId, Camera camera) {
		
		
		
		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}
		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}
		// set preview size and make any resize, rotate or
		// reformatting changes here
		// start preview with new settings
		setCamera(camera);
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
			setCameraDisplayOrientation(activity, cameraId, camera); 
		} catch (Exception e) {
			Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		refreshCamera(activity, mCameraId, mCamera);
	}

	public void setCamera(Camera camera) {
		//method to set a camera instance
		mCamera = camera;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// mCamera.release();

	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.GINGERBREAD) 
	public static void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) 
	{
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
	     camera.setDisplayOrientation(result);
	 }
}