package com.liamfarrell.android.snapbattle.activity;

import android.support.v4.app.Fragment;

public class VideoViewActivity extends SingleFragmentAppCompatActivity
{

	@Override
	protected Fragment createFragment() {
		return new VideoViewFragment();
	}

}
