package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.AddFollowerRequestWithCognitoIDs;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.RemoveFollowerRequest;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Liam on 2/07/2017.
 */

public class UsersBattlesActivity extends AppCompatActivity {
    public static final String EXTRA_COGNITO_ID = "com.liamfarrell.android.snapbattle.user_cognito_id";

    private static final String TAG = "UsersBattleActivity";

    private AppCompatButton mFollowOrUnfollowButton;
    private String mCognitoID;
    private boolean mIsFollowing;
    private TextView mNameTextView, mBattlesCountTextView, mFollowingCountTextView;
    private CircleImageView mProfilePicture;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_battles);
        mProfilePicture = findViewById(R.id.profileImageView);
        Picasso.get().cancelRequest(mProfilePicture);
        mProfilePicture.setImageResource(R.drawable.default_profile_pic);

        mNameTextView = findViewById(R.id.nameTextView);
        mBattlesCountTextView = findViewById(R.id.battles_count_textview);
        mFollowingCountTextView = findViewById(R.id.followers_count_text_view);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null)
        {
            fragment = new UsersBattlesFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mFollowOrUnfollowButton = findViewById(R.id.addOrRemoveFollowButton);
        setFollowOrUnfollowButtonOnClick();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
        }
        return true;
    }

    public void setBattlesCount(int battlesCount)
    {
        mBattlesCountTextView.setText(Integer.toString(battlesCount));
    }

    public void setFollowersCount(int followersCount)
    {
        mFollowingCountTextView.setText(Integer.toString(followersCount));
    }

    public void setName(String name)
    {
        if (name != null)
        {
            mNameTextView.setText(name);
        }

    }
    public void setCognitoId(String cognitoID)
    {
        mCognitoID = cognitoID;
    }




    public void setProfilePicture(String url)
    {
        Log.i(TAG, "Url: " + url);
        Picasso.get().load(url).error(R.drawable.default_profile_pic).placeholder(R.drawable.default_profile_pic).into(mProfilePicture);
    }

    public void setToolbarText(String text)
    {
        Log.i(TAG, "title: " + text);
        mCollapsingToolbarLayout.setTitle(text);
    }

    public void setIsFollowing(boolean isFollowing)
    {
        if (isFollowing)
        {
            mIsFollowing = true;
            mFollowOrUnfollowButton.setText(R.string.unfollow);
        }
        else
        {
            mIsFollowing = false;
            mFollowOrUnfollowButton.setText(R.string.follow);
        }
    }

    private void setFollowOrUnfollowButtonOnClick()
    {
        mFollowOrUnfollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsFollowing)
                {
                    removeFollower();
                }
                else
                {
                    followUser();
                }
            }
        });
    }

    private void followUser() {
        ArrayList<String> cognitoIDList = new ArrayList<String>();
        cognitoIDList.add(mCognitoID);
        AddFollowerRequestWithCognitoIDs request = new AddFollowerRequestWithCognitoIDs();
        request.setCognitoIDFollowList(cognitoIDList);
        new FollowUserTask(this).execute(request);
    }

    private static class FollowUserTask extends AsyncTask<AddFollowerRequestWithCognitoIDs, Void,AsyncTaskResult<DefaultResponse>>
    {
        private WeakReference<Activity> activityReference;

        FollowUserTask(Activity activity)
        {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<DefaultResponse> doInBackground(AddFollowerRequestWithCognitoIDs... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

         final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
                try {
                    DefaultResponse response =  lambdaFunctionsInterface.AddFollower(params[0]);
                    return new AsyncTaskResult<>(response);

                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                }
                catch (AmazonServiceException ase) {
                    // invalid credentials, incorrect AWS signature, etc
                    Log.i("ERROR", ase.getErrorMessage());
                    return new AsyncTaskResult<>(ase);
                }
                catch (AmazonClientException ace) {
                    // Network issue
                    Log.i("ERROR", ace.toString());
                    return new AsyncTaskResult<>(ace);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<DefaultResponse> asyncResult)
            {
            // get a reference to the activity if it is still there
                UsersBattlesActivity activity = (UsersBattlesActivity)activityReference.get();
                if (activity == null || activity.isFinishing()) return;
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
                    return;

                }

                //Update following user cache
                FollowingUserCache.get(activity.getApplicationContext(), null).updateCache(activity.getApplicationContext(),null);;
                 activity.setIsFollowing(true);
            }
    }

    private void removeFollower() {
        RemoveFollowerRequest request = new RemoveFollowerRequest();
        request.setCognitoIDUnfollow(mCognitoID);
        new RemoveFollowerTask(this).execute(request);
    }

    private static class RemoveFollowerTask extends  AsyncTask<RemoveFollowerRequest, Void, AsyncTaskResult<DefaultResponse>>
    {
        private WeakReference<Activity> activityReference;

        RemoveFollowerTask(Activity activity)
        {
            activityReference = new WeakReference<>(activity);
        }
        @Override
        protected  AsyncTaskResult<DefaultResponse> doInBackground(RemoveFollowerRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));
                final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);

                try {
                    DefaultResponse response =   lambdaFunctionsInterface.RemoveFollower(params[0]);
                    return new AsyncTaskResult<>(response);
                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);                }
                catch (AmazonServiceException ase) {
                    // invalid credentials, incorrect AWS signature, etc
                    Log.i("ERROR", ase.getErrorMessage());
                    return new AsyncTaskResult<>(ase);
                }
                catch (AmazonClientException ace) {
                    // Network issue
                    Log.i("ERROR", ace.toString());
                    return new AsyncTaskResult<>(ace);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<DefaultResponse> asyncResult)
            {
                // get a reference to the activity and fragment if it is still there
                UsersBattlesActivity activity = (UsersBattlesActivity)activityReference.get();
                if (activity == null || activity.isFinishing()) return;

                DefaultResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {

                    new HandleLambdaError().handleError(asyncResult.getError(), activity,null);
                    return;


                }


                activity.setIsFollowing(false);

            }
    }








}
