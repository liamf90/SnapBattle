package com.liamfarrell.android.snapbattle.ui;

import androidx.fragment.app.Fragment;

public class FacebookLoginActivity extends SingleFragmentActivity 
{

	@Override
	protected Fragment createFragment() 
	{
		return new FacebookLoginFragment();
	
	}


}
