package com.liamfarrell.android.snapbattle.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.WindowManager;

import com.liamfarrell.android.snapbattle.R;

/**
 * Created by Liam on 20/01/2018.
 */

public class FullBattleVideoPlayerActivity extends AppCompatActivity {
    public static final String EXTRA_FILE_VIDEO_PATH = "com.liamfarrell.android.snapbattle.videoplayeractivity.videopathextra";
    public static final String EXTRA_BATTLEID = "com.liamfarrell.android.snapbattle.videoplayeractivity.battleIDextra";
    public static final String EXTRA_CHALLENGER_USERNAME = "com.liamfarrell.android.snapbattle.videoplayeractivity.challengerusernameextra";
    public static final String EXTRA_CHALLENGED_USERNAME = "com.liamfarrell.android.snapbattle.videoplayeractivity.challengedusernameextra";

   private FragmentManager fm;


    static { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);
        Fragment mVideoPlayerFragment = new FullBattleVideoPlayerFragment();
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.comments_fragment_title);
        getSupportActionBar().hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null)
        {
            fragment = mVideoPlayerFragment;
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }

    public void startCommentsFragment()
    {
        fm.beginTransaction().add(R.id.fragmentContainer, new ViewCommentsFragment()).addToBackStack(null).commit();
        getSupportActionBar().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        //check if comments fragment is being viewed or not
        //if backstack count is 0 it is not being viewed
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            getSupportActionBar().hide();
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();


        }

    }




}






