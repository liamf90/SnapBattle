package com.liamfarrell.android.snapbattle.activity;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.liamfarrell.android.snapbattle.R;

/**
 * Created by Liam on 5/11/2017.
 */

public class NotificationListActivity extends SingleFragmentAppCompatToolbarActivity {
    @Override
    protected Fragment createFragment() {
        return new NotificationListFragment();
    }

    @Override
    protected String getToolbarTitle() {
        return getResources().getString(R.string.notification_activity_title);
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

