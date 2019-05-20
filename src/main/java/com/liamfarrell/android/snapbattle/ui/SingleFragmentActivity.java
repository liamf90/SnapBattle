package com.liamfarrell.android.snapbattle.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.liamfarrell.android.snapbattle.R;


public abstract class SingleFragmentActivity extends FragmentActivity {
	 protected abstract Fragment createFragment();
	
	 protected int getLayoutResId()
	 {
		 return R.layout.activity_fragment;
	 }
	@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(getLayoutResId());
	        
	        
	        FragmentManager fm = getSupportFragmentManager();
	        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
	        
	        if (fragment == null)
	        {
	        	fragment = createFragment();
	        	fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
	        }
	        
	    }
}
