package com.liamfarrell.android.snapbattle.activity;

import android.support.v4.app.Fragment;

public class FacebookLoginActivity extends SingleFragmentActivity 
{

	@Override
	protected Fragment createFragment() 
	{
		return new FacebookLoginFragment();
	
	}


}
