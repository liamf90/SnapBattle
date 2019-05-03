package com.liamfarrell.android.snapbattle.activity;

import android.support.v4.app.Fragment;

public class ViewFollowingActivity extends SingleFragmentActivity 
{
	@Override
	protected Fragment createFragment() 
	{
		return new ViewFollowingFragment();
	
	}
}
