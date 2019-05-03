package com.liamfarrell.android.snapbattle.activity;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.liamfarrell.android.snapbattle.R;

/**
 * Created by Liam on 4/08/2017.
 */

public class ViewBattlesFromNameActivity extends SingleFragmentAppCompatToolbarActivity
{
    @Override
    protected Fragment createFragment()
    {
        return new ViewBattlesFromNameFragment();

    }

    @Override
    protected String getToolbarTitle() {
        Resources res = getResources();
        String battleName =  getIntent().getStringExtra(ViewBattlesFromNameFragment.EXTRA_BATTLE_NAME);
        String battleNameFirstLetterCapital = battleName.substring(0,1).toUpperCase() + battleName.substring(1).toLowerCase();
        return res.getString(R.string.battle_name_plural, battleNameFirstLetterCapital);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
        }
        return true;
    }
}

