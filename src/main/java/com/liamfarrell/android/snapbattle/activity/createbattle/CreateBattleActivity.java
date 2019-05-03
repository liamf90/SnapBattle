package com.liamfarrell.android.snapbattle.activity.createbattle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.User;

import java.util.Arrays;
import java.util.Set;

public class CreateBattleActivity extends AppCompatActivity {

	private boolean mEnableNextButton = false;
	private boolean mShowNextButton = true;
	private String mBattleName;
	private User mOpponent;
	private ChooseVotingFragment.VotingChoice mVotingChoice;
	private ChooseVotingFragment.VotingLength mVotingLength;
	//private ArrayList<Opponent> chosenJudges; << future implementation
	private int mRounds;
	private int mCurrentFragmentIndex = -1;
	private Fragment[] mFragments;
	private String[] mFragmentNames;
    CallbackManager mCallbackManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_battle);

		mFragments = new Fragment[]{ new ChooseBattleTypeFragment(),new ChooseOpponentFragment(), new ChooseRoundsFragment(), new ChooseVotingFragment(), new VerifyBattleFragment()};
		mFragmentNames = getResources().getStringArray(R.array.nav_create_battle_fragment_titles);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		//go to first fragment
		nextFragment();
	}
	public void setEnableNextButton(boolean enabled)
	{
		mEnableNextButton = enabled;
		invalidateOptionsMenu();
	}

	public void nextFragment()
	{

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.container_body, mFragments[++mCurrentFragmentIndex]).commit();
		getSupportActionBar().setTitle(mFragmentNames[mCurrentFragmentIndex]);

		if (mFragments[mCurrentFragmentIndex] instanceof ChooseBattleTypeFragment)
		{
			setEnableNextButton(false);
		}
		else if (mFragments[mCurrentFragmentIndex] instanceof ChooseOpponentFragment)
		{
			setEnableNextButton(false);
		}
		else if (mFragments[mCurrentFragmentIndex] instanceof ChooseRoundsFragment)
		{
			setEnableNextButton(true);
		}
		else if (mFragments[mCurrentFragmentIndex] instanceof ChooseVotingFragment)
		{
			setEnableNextButton(true);
		}
		else if (mFragments[mCurrentFragmentIndex] instanceof VerifyBattleFragment)
		{
			setEnableNextButton(false);
		}

	}


	public void setOpponent(User o)
	{
		mOpponent = o;
		nextFragment();
	}

	public void setRounds(int rounds)
	{
		this.mRounds = rounds;
		nextFragment();
	}

	public void setBattleName(String battleName)
	{
		this.mBattleName = battleName;
		nextFragment();
	}

	public ChooseVotingFragment.VotingChoice getVotingChoice()
    {
        return mVotingChoice;
    }

    public ChooseVotingFragment.VotingLength getVotingLength()
    {
        return mVotingLength;
    }


	public int getRounds()
	{
		return mRounds;
	}

	public User getOpponent()
	{
		return mOpponent;
	}

	public String getBattleName()
	{
		return mBattleName;
	}


	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
			menu.findItem(R.id.action_next).setEnabled(mEnableNextButton);
		    menu.findItem(R.id.action_next).setVisible(mShowNextButton);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_create_battle, menu);


		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == android.R.id.home)
		{
			this.finish();
		}
		else if (id == R.id.action_next)
		{
			if (mCurrentFragmentIndex ==0)
			{
				mBattleName = ((ChooseBattleTypeFragment)mFragments[0]).getBattleName();
				nextFragment();
			}
			else if (mCurrentFragmentIndex == 2)
			{
				mRounds = ((ChooseRoundsFragment)mFragments[2]).getRounds();
				invalidateOptionsMenu();
				nextFragment();
			}
			else if (mCurrentFragmentIndex == 3)
            {
                mVotingChoice = ((ChooseVotingFragment)mFragments[3]).getChosenVotingChoice();
                mVotingLength = ((ChooseVotingFragment)mFragments[3]).getChosenVotingLength();
                if (mVotingChoice.equals(ChooseVotingFragment.VotingChoice.MUTUAL_FACEBOOK)){
					if (!doesUserHaveUserFriendsPermission()){
						Toast.makeText(this, R.string.need_accept_permission_user_friends, Toast.LENGTH_SHORT).show();
						requestUserFriendsPermission();
					}
                    else {
                        nextFragment();
                    }
				}else{
                    nextFragment();
                }
            }

            else if(mCurrentFragmentIndex == 4)
            {
                mShowNextButton = false;
                invalidateOptionsMenu();
            }
		}

		return super.onOptionsItemSelected(item);
	}

	private boolean doesUserHaveUserFriendsPermission(){
		Set<String> declinedPermissions = AccessToken.getCurrentAccessToken().getDeclinedPermissions();
		 return !declinedPermissions.contains("user_friends");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
		Log.d("CreateBattleActivity", "Activity result");
	}


	private void requestUserFriendsPermission(){
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        nextFragment();
                    }

                    @Override
                    public void onCancel() {
                        //User Friends Not accepted.. Do nothing
                    }


                    @Override
                    public void onError(FacebookException e) {
                        Toast.makeText(getApplicationContext(), R.string.server_error_toast, Toast.LENGTH_SHORT).show();
                    }
                });


        LoginManager.getInstance().logInWithReadPermissions(
				this,
				Arrays.asList("user_friends"));

	}



}