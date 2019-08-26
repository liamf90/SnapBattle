package com.liamfarrell.android.snapbattle.ui.startup;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.liamfarrell.android.snapbattle.ActivityMainNavigationDrawer;
import com.liamfarrell.android.snapbattle.R;

public class StartupActivity extends AppCompatActivity {

    public final static String EXTRA_USERNAME = "com.liamfarrell.android.snapbattle.startupactivity.extra_username";
    public final static String EXTRA_NAME = "com.liamfarrell.android.snapbattle.startupactivity.extra_name";

    public static final String TAG= "StartupActivity";

    private boolean enableNextButton = false;
    private int mCurrentFragmentIndex = -1;
    private Fragment[] mFragments;
    private String[] mFragmentNames;
    private String mUsername;
    private String mName;
    private View mProgressContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_battle);
        mUsername = getIntent().getStringExtra(EXTRA_USERNAME);
        mName = getIntent().getStringExtra(EXTRA_NAME);

        mFragments = new Fragment[]{ new AddFacebookFriendsAsFollowersStartupFragment(), new ChooseNameStartupFragment(),new ChooseProfilePictureStartupFragment(), new ChooseUsernameStartupFragment()};
        mFragmentNames = getResources().getStringArray(R.array.startup_activity_titles);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mProgressContainer = findViewById(R.id.progressContainer);

        setSupportActionBar(toolbar);
        nextFragment();
    }




    public void setEnableNextButton(boolean enabled)
    {
        enableNextButton = enabled;
        invalidateOptionsMenu();
    }
    public String getUsername()
    {
        return mUsername;
    }
    public void showProgressSpinner()
    {
        mProgressContainer.setVisibility(View.VISIBLE);
    }
    public void hideProgressSpiner()
    {
        mProgressContainer.setVisibility(View.GONE);
    }

    public void nextFragment()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, mFragments[++mCurrentFragmentIndex]).commit();
        getSupportActionBar().setTitle(mFragmentNames[mCurrentFragmentIndex]);

        if (mFragments[mCurrentFragmentIndex] instanceof AddFacebookFriendsAsFollowersStartupFragment)
        {
            setEnableNextButton(false);
        }
        else if (mFragments[mCurrentFragmentIndex] instanceof ChooseProfilePictureStartupFragment)
        {
            setEnableNextButton(false);
        }
        else if (mFragments[mCurrentFragmentIndex] instanceof ChooseUsernameStartupFragment)
        {
            setEnableNextButton(true);
        }
        else if (mFragments[mCurrentFragmentIndex] instanceof ChooseNameStartupFragment)
        {
            if (mName != null && mName != "")
            {
                setEnableNextButton(true);
            }
            else
            {
                setEnableNextButton(false);
            }
        }
    }

    public void finishStartup()
    {
        Log.i(TAG, "Finish Startup");
        Intent i = new Intent(this, ActivityMainNavigationDrawer.class);
        startActivity(i);
        this.finish();
    }


    public String getName()
    {
        return mName;
    }


    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {

        menu.findItem(R.id.action_next).setEnabled(enableNextButton);
        boolean showNextButton = true;
        menu.findItem(R.id.action_next).setVisible(showNextButton);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_startup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_skip)
        {
            if (mCurrentFragmentIndex == 3)
            {
                finishStartup();
            }
            else {
                nextFragment();
            }
        }
        else if (id == R.id.action_next)
        {
            //when next button is pressed update user properties in the background and go to next fragment.
            if (mCurrentFragmentIndex ==0)
            {
                ((AddFacebookFriendsAsFollowersStartupFragment)mFragments[0]).addFollowers();
            }
            else if (mCurrentFragmentIndex == 1)
            {
                if (mName != null && !((ChooseNameStartupFragment)mFragments[1]).getNameEditText().equals(mName))
                {
                    ((ChooseNameStartupFragment)mFragments[1]).updateName();
                }
                else
                {
                    nextFragment();
                }
            }
            else if (mCurrentFragmentIndex == 2)
            {
                ((ChooseProfilePictureStartupFragment)mFragments[2]).uploadProfilePic();
                nextFragment();
            }
            else if (mCurrentFragmentIndex == 3)
            {
                ((ChooseUsernameStartupFragment)mFragments[3]).updateUsername();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}