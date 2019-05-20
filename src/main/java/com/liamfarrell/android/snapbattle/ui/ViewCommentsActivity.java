package com.liamfarrell.android.snapbattle.ui;

import androidx.fragment.app.Fragment;
import android.view.MenuItem;

import com.liamfarrell.android.snapbattle.R;

public class ViewCommentsActivity extends SingleFragmentAppCompatToolbarActivity {



    @Override
    protected Fragment createFragment() {
        return new ViewCommentsFragment();
    }

    @Override
    protected String getToolbarTitle() {
        return getResources().getString(R.string.comments_fragment_title);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
