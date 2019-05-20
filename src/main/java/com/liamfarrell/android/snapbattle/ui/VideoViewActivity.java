package com.liamfarrell.android.snapbattle.ui;

import androidx.fragment.app.Fragment;

public class VideoViewActivity extends SingleFragmentAppCompatActivity
{

	@Override
	protected Fragment createFragment() {
		return new VideoViewFragment();
	}

}
