package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache;
import com.liamfarrell.android.snapbattle.model.User;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.caches.OtherUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.AddFollowerRequestWithUsername;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FollowingRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.liamfarrell.android.snapbattle.views.LinearDividerItemDecoration;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewFollowingFragment extends Fragment
{
	private static final String TAG = "ViewFollowingFragment";
    private View mProgressContainer;
	private List<User> followersList;
	private FollowerAdapter mAdapter;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setRetainInstance(true);
		followersList = new ArrayList<User>();



		
	}


	private void getFollowing() {
        followersList.clear();
        mProgressContainer.setVisibility(View.VISIBLE);
        FollowingRequest request = new FollowingRequest();
        request.setShouldGetProfilePic(true);
        new GetFollowingTask(getActivity(), this).execute(request);
    }

    private static class GetFollowingTask extends AsyncTask<FollowingRequest, Void, AsyncTaskResult<ResponseFollowing>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ViewFollowingFragment> fragmentReference;

        GetFollowingTask(Activity activity, ViewFollowingFragment fragment) {
        fragmentReference = new WeakReference<>(fragment);
        activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<ResponseFollowing> doInBackground(FollowingRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);



        // The Lambda function invocation results in a network call
        // Make sure it is not called from the main thread

                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
					ResponseFollowing response =   lambdaFunctionsInterface.GetFollowing(params[0]);
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
            protected void onPostExecute(AsyncTaskResult<ResponseFollowing> asyncResult) {

                ViewFollowingFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

				ResponseFollowing result = asyncResult.getResult();
				if (asyncResult.getError() != null)
				{
					new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
					return;
				}

                fragment.followersList.clear();
                fragment.followersList.addAll(result.getSqlResult());
                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
                fragment.mAdapter.notifyDataSetChanged();
            }
    }




	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_view_followers, parent, false);
		mProgressContainer = v.findViewById(R.id.follower_list_progressContainer);
		RecyclerView recyclerView = v.findViewById(R.id.recyclerList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
		Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider);
		RecyclerView.ItemDecoration dividerItemDecoration = new LinearDividerItemDecoration(dividerDrawable);
		recyclerView.addItemDecoration(dividerItemDecoration);
        mAdapter = new FollowerAdapter(followersList);
        recyclerView.setAdapter(mAdapter);
        mProgressContainer.setVisibility(View.INVISIBLE);

        Button enterUsernameButton = (Button) v.findViewById(R.id.EnterUsernameManuallyButton);
		enterUsernameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View v2 = getActivity().getLayoutInflater()
						.inflate(R.layout.username_dialog, null);
				final EditText usernameEditText = (EditText)v2.findViewById(R.id.usernameTextView);

				new AlertDialog.Builder(getActivity())
						.setView(v2)
						.setTitle(R.string.enter_username_dialog_title)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Log.i(TAG, "Username: " + usernameEditText.getText().toString());
								dialog.cancel();
								addFollowerFromUsername(usernameEditText.getText().toString());

							}
						})
						.create().show();
			}
		});
		getFollowing();
		return v;
	}

    private void removeFollower(String cognitoIDUnfollow) {
        com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.RemoveFollowerRequest request = new com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.RemoveFollowerRequest();
        request.setCognitoIDUnfollow(cognitoIDUnfollow);
        new RemoveFollowerTask(getActivity(), this).execute(request);
    }

    private static class RemoveFollowerTask extends  AsyncTask<com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.RemoveFollowerRequest, Void, AsyncTaskResult<DefaultResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ViewFollowingFragment> fragmentReference;

        public RemoveFollowerTask(Activity activity, ViewFollowingFragment  fragment) {
            this.activityReference = new WeakReference<>(activity);
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected  AsyncTaskResult<DefaultResponse> doInBackground(com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.RemoveFollowerRequest... params) {
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
            ViewFollowingFragment fragment = fragmentReference.get();
            Activity activity = activityReference.get();
            if (fragment == null || fragment.isRemoving()) return;
            if (activity == null || activity.isFinishing()) return;

            if (asyncResult.getError() != null)
            {
                new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
                return;
            }

            //Update following user cache
            FollowingUserCache.get(activity.getApplicationContext(), null).updateCache(activity.getApplicationContext(),null);;

            //TODO: GET DATA FOR ADDED USER INSTEAD OF RELOADING ALL THE FOLLOWING
            fragment.getFollowing();
        }
    }

	private void addFollowerFromUsername(String username) {
        AddFollowerRequestWithUsername request = new AddFollowerRequestWithUsername();
        request.setUsernameFollow(username);
        new addFollowerFromUsernameTask(getActivity(), this).execute(request);
	}

	private static class addFollowerFromUsernameTask extends AsyncTask<AddFollowerRequestWithUsername, Void, AsyncTaskResult<DefaultResponse>>
	{

        private WeakReference<Activity> activityReference;
        private WeakReference<ViewFollowingFragment> fragmentReference;

        public addFollowerFromUsernameTask(Activity activity, ViewFollowingFragment  fragment) {
            this.activityReference = new WeakReference<>(activity);
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
		protected AsyncTaskResult<DefaultResponse> doInBackground(AddFollowerRequestWithUsername... params)
		{
		// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
		LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
				Regions.US_EAST_1,
				FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

		// Create the Lambda proxy object with default Json data binder.
		// You can provide your own data binder by implementing
		// LambdaDataBinder
		final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
				// invoke "echo" method. In case it fails, it will throw a
				// LambdaFunctionException.
				try {
                    DefaultResponse response = lambdaFunctionsInterface.AddFollower(params[0]);
                    return new AsyncTaskResult<DefaultResponse>(response);

				} catch (LambdaFunctionException lfe) {
					Log.i("ERROR", lfe.getDetails());
					Log.i("ERROR",lfe.getStackTrace().toString());
					lfe.printStackTrace();

                    return new AsyncTaskResult<DefaultResponse>(lfe);
				}
				catch (AmazonServiceException ase) {
					// invalid credentials, incorrect AWS signature, etc
					Log.i("ERROR", ase.getErrorMessage());
                    return new AsyncTaskResult<DefaultResponse>(ase);
				}
				catch (AmazonClientException ace) {
					// Network issue
					Log.i("ERROR", ace.toString());
                    return new AsyncTaskResult<DefaultResponse>(ace);
				}
			}

			@Override
			protected void onPostExecute(AsyncTaskResult<DefaultResponse> asyncResult)
			{

                ViewFollowingFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

				if (asyncResult.getError() != null)
				{
					new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
					return;
				}

				//Update following user cache
                FollowingUserCache.get(activity.getApplicationContext(), null).updateCache(activity.getApplicationContext(),null);;

			    //TODO: GET DATA FOR ADDED USER INSTEAD OF RELOADING ALL THE FOLLOWING
				fragment.getFollowing();
			}

	}


	
	private class FollowerAdapter extends RecyclerView.Adapter<FollowerAdapter.MyViewHolder>
	{
		private List<User> followingList;


		public FollowerAdapter(List<User> followingList)
        {
            this.followingList  = followingList;
        }

        @Override
        public FollowerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_following, parent, false);


            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final FollowerAdapter.MyViewHolder holder, final int position) {
            final User following = this.followingList.get(position);
            holder.username.setText(following.getUsername());
            holder.name.setText(following.getFacebookName());
            Picasso.get().cancelRequest( holder.circleImageView);
           holder.circleImageView.setImageResource(R.drawable.default_profile_pic100x100);
            holder.circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent  i = new Intent(getActivity(), UsersBattlesActivity.class);
                    i.putExtra(UsersBattlesActivity.EXTRA_COGNITO_ID, following.getCognitoId());
                    startActivity(i);
                }
            });
            OtherUsersProfilePicCacheManager.getProfilePicCache(getActivity()).getSignedUrlProfilePicOpponent(following.getCognitoId(), following.getProfilePicCount(), following.getProfilePicSignedUrl(), getActivity(), new OtherUsersProfilePicCacheManager.SignedUrlCallback() {
                @Override
                public void onSignedPicReceived(String signedUrl) {
                    Log.i("ViewFollowing", "Signed url: " + signedUrl);
                    Picasso.get().load(signedUrl).placeholder(R.drawable.default_profile_pic100x100).into(holder.circleImageView);
                }
            });
            holder.modifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeFollower(following.getCognitoId());
                }
            });

        }

        @Override
        public int getItemCount() {
            return this.followingList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
			TextView username, name;
			CircleImageView circleImageView;
			AppCompatButton modifyButton;

			public MyViewHolder(View view) {
				super(view);
				username = view.findViewById(R.id.UsernameTextView);
				name = view.findViewById(R.id.NameTextView);
				circleImageView = view.findViewById(R.id.profileImageView);
				modifyButton = view.findViewById(R.id.modifyFollowerButton);

			}
		}

	}
	
	
	
}
