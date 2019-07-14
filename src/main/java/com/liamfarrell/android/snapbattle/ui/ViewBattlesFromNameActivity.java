package com.liamfarrell.android.snapbattle.ui;

import android.content.res.Resources;
import androidx.fragment.app.Fragment;
import android.view.MenuItem;

import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.mvvm_ui.BattlesFromNameFragment;

/**
 * Created by Liam on 4/08/2017.
 */

public class ViewBattlesFromNameActivity extends SingleFragmentAppCompatToolbarActivity
{
    public static final String EXTRA_BATTLE_NAME = "com.liamfarrell.android.snapbattle.viewbattlesfromnameactivity.battlename";

    @Override
    protected Fragment createFragment()
    {
        return new BattlesFromNameFragment();

    }

    @Override
    protected String getToolbarTitle() {
        Resources res = getResources();
        String battleName =  getIntent().getStringExtra(EXTRA_BATTLE_NAME);
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

