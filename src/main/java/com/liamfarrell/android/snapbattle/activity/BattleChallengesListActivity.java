package com.liamfarrell.android.snapbattle.activity;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.liamfarrell.android.snapbattle.R;

public class BattleChallengesListActivity extends SingleFragmentAppCompatToolbarActivity
{

	@Override
	protected Fragment createFragment() 
	{
		return new BattleChallengesListFragment();
	
	}

	@Override
	protected String getToolbarTitle() {
		return getResources().getString(R.string.nav_challenges);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if (id == android.R.id.home) {
			super.onBackPressed();
		}
		return true;
	}


}
