package com.liamfarrell.android.snapbattle.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.liamfarrell.android.snapbattle.R;

public class ViewBattleActivity extends AppCompatActivity
{

	public static final String TAG = "ViewBattleActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_battle);


		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if (fragment == null)
		{
			fm.beginTransaction().add(R.id.fragmentContainer,  new ViewBattleFragment()).commit();
		}

		Toolbar mToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(R.string.view_battle_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return true;
	}


}

