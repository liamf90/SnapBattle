package com.liamfarrell.android.snapbattle.ui.createbattle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.liamfarrell.android.snapbattle.model.User;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UsernameToFacebookIDRequest;
import com.liamfarrell.android.snapbattle.ui.FollowFacebookFriendsFragment;
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.ui.UsersBattlesActivity;
import com.liamfarrell.android.snapbattle.caches.OtherUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FollowingRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UsernameToFacebookIDResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.liamfarrell.android.snapbattle.views.LinearDividerItemDecoration;
import com.liamfarrell.android.snapbattle.R;
import com.squareup.picasso.Picasso;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChooseOpponentFragment extends Fragment
{
	private static final String TAG = "OpponentList";
    private ArrayList<User> mBackupOpponentList;
	protected ArrayList<User> mOpponentList;
	private Map<String, Integer> mOpponentSortFactorMap;
    private TabLayout mOpponentTabSelector;
    private OpponentAdapter mAdapter;
    private View mProgressContainer;

    public static final String EXTRA_FACEBOOK_ID = "com.liamfarrell.android.snapbattle.opponentfacebookid";
	public static final String EXTRA_FACEBOOK_NAME = "com.liamfarrell.android.snapbattle.opponentfacebookname";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOpponentList = new ArrayList<>();
        mBackupOpponentList = new ArrayList<>();
        mAdapter = new OpponentAdapter(mOpponentList);


    }


    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)
	{

		View view = inflater.inflate(R.layout.fragment_opponent_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider);
        RecyclerView.ItemDecoration dividerItemDecoration = new LinearDividerItemDecoration(dividerDrawable);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(mAdapter);
        SearchView searchView = view.findViewById(R.id.searchbox);


        //Set Search View on query text listener to perform list updates as the user types
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                mOpponentList.clear();
                mOpponentList.addAll(mBackupOpponentList);
                doMySearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mOpponentList.clear();
                mOpponentList.addAll(mBackupOpponentList);
                doMySearch(newText);
                return true;
            }
        });
		mOpponentTabSelector = view.findViewById(R.id.opponentSelectorTabLayout);
		//update the opponents list depending on which tab is selected
		mOpponentTabSelector.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
			    Log.i(TAG, "Items: " + mAdapter.getItemCount());
				if (tab.getPosition() == 0)
				{
					//recent
					mProgressContainer.setVisibility(View.VISIBLE);
					fillListRecents();
				}
				if (tab.getPosition() == 1)
				{
					//following
					loadFollowing();
				}
				if (tab.getPosition() == 2)
				{
					//facebook
					mProgressContainer.setVisibility(View.VISIBLE);
					fillFriendsList();
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

		mProgressContainer = view.findViewById(R.id.opponent_list_progressContainer);
        Button enterUsernameButton = view.findViewById(R.id.EnterUsernameManuallyButton);
		enterUsernameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View v2 = getActivity().getLayoutInflater()
			            .inflate(R.layout.username_dialog, null);
				final EditText usernameEditText =v2.findViewById(R.id.usernameTextView);
			
				 new AlertDialog.Builder(getActivity())
		            .setView(v2)
		            .setTitle(R.string.enter_username_dialog_title)
		            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) {
		                   Log.i(TAG, "Username: " + usernameEditText.getText().toString());
		                   dialog.cancel();
                            usernameToCognitoId(usernameEditText.getText().toString());
		                }
		            })
		            .create().show();
			}
		});
		
		//fill opponent list with all recent opponents as this is the initial tab selected
        fillListRecents();
		
		
		return view;
	}

	private Integer getSortFactor(User op)
    {
        return mOpponentSortFactorMap.get(op.getFacebookUserId());
    }

    private void setSortFactor(User op, Integer sortFactor)
    {
        mOpponentSortFactorMap.put(op.getFacebookUserId(), sortFactor);
    }

	private void doMySearch(String query)
    {
        //filter the opponents list with the text the user inputs. results are shown in order of how close the result is.
        //factors such as a match to the start of the first name will display higher than a match to the part of the last name

            ArrayList<User> newOpponentList = new ArrayList<>();
            query = query.toLowerCase();
            for (int i = 0; i < mOpponentList.size(); i++) {
                int resultComparison = 0;
                if (!query.contains(" "))
                {
                    String[] NameSplit = mOpponentList.get(i).getFacebookName().toLowerCase().split(" ");
                    for (int x = 0; x < NameSplit.length; x++) {
                        if (x == 0) {
                            if (NameSplit[x].equals(query)) {
                                //Top result
                                resultComparison = 1;
                                //maybe break for performance
                                break;
                            } else if (NameSplit[x].startsWith(query)) {
                                resultComparison = 2;
                                break;

                                //maybe break

                            } else if (NameSplit[x].contains(query)) {
                                //fifth best result
                                resultComparison = 5;
                            }
                        } else {
                            if (NameSplit[x].equals(query)) {
                                //third best result
                                resultComparison = 3;
                                break;
                                //maybe break for performance

                            } else if (NameSplit[x].startsWith(query)) {
                                //fourth best result
                                if (resultComparison == 0 || resultComparison > 4) {
                                    resultComparison = 4;
                                }

                            } else if (NameSplit[x].contains(query)) {
                                //sixth best result
                                if (resultComparison == 0) {
                                    resultComparison = 6;
                                }
                            }
                        }

                    }
                } else
                {
                  if (mOpponentList.get(i).getFacebookName().toLowerCase().startsWith(query))
                  {
                      resultComparison = 1;
                  }
                  else if (mOpponentList.get(i).getFacebookName().toLowerCase().contains(query))
                  {
                      resultComparison = 2;
                  }
                }

                if (resultComparison != 0) {
                    newOpponentList.add(mOpponentList.get(i));
                    setSortFactor(newOpponentList.get(newOpponentList.size() - 1), resultComparison);
                    //newOpponentList.get(newOpponentList.size() - 1).setSortFactor(resultComparison);
                }

            }

            mOpponentList.clear();
            mOpponentList.addAll(newOpponentList);
            Collections.sort(mOpponentList, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return getSortFactor(o1) - getSortFactor(o2);
                //o1.getSortFactor() - o2.getSortFactor();
            }
            });


            mAdapter.notifyDataSetChanged();
    }


    //If a user  manually enters the username of the opponent, get the cognito id and user info of the opponent
	private void usernameToCognitoId(String username) {
        UsernameToFacebookIDRequest request = new UsernameToFacebookIDRequest();
        request.setUsername(username);
        new UsernameToCognitoTask(getActivity(), this).execute(request);

    }
    private static class UsernameToCognitoTask extends  AsyncTask<UsernameToFacebookIDRequest, Void, AsyncTaskResult<UsernameToFacebookIDResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ChooseOpponentFragment> fragmentReference;

        UsernameToCognitoTask(Activity activity, ChooseOpponentFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<UsernameToFacebookIDResponse> doInBackground(UsernameToFacebookIDRequest... params) {
		// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
		LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
				Regions.US_EAST_1,
                IdentityManager.getDefaultIdentityManager().getCredentialsProvider());

		final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
				try {
					UsernameToFacebookIDResponse response =  lambdaFunctionsInterface.UsernameToFacebookID(params[0]);
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
			protected void onPostExecute(AsyncTaskResult<UsernameToFacebookIDResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                ChooseOpponentFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                UsernameToFacebookIDResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;
                }

				if (result.getSqlResult().size() > 0 )
				{
				    User user = result.getSqlResult().get(0);
                    ((CreateBattleActivity)activity).setOpponent(user);
				}
				else
				{
					Toast.makeText(activity, R.string.username_not_found_toast_message, Toast.LENGTH_SHORT).show();
				}
			}
	}




	//This method loads all the users that the user follows, used when the following tab is selected
	private void loadFollowing() {
        mProgressContainer.setVisibility(View.VISIBLE);
        FollowingRequest request = new FollowingRequest();
        request.setShouldGetProfilePic(true);
        new LoadFollowingTask(getActivity(), this).execute(request);
    }

    private static class LoadFollowingTask extends AsyncTask<FollowingRequest, Void, AsyncTaskResult<ResponseFollowing>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ChooseOpponentFragment> fragmentReference;

        LoadFollowingTask(Activity activity, ChooseOpponentFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<ResponseFollowing> doInBackground(FollowingRequest... params) {
		// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
		LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
				Regions.US_EAST_1,
                IdentityManager.getDefaultIdentityManager().getCredentialsProvider());

		// Create the Lambda proxy object with default Json data binder.
		// You can provide your own data binder by implementing
		// LambdaDataBinder
		final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
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
                // get a reference to the callbacks and fragment if it is still there
                ChooseOpponentFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

				ResponseFollowing result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;

                }
                fragment.mOpponentList.clear();

                fragment.mOpponentList.addAll(result.getSqlResult());
                fragment.mBackupOpponentList.clear();
                fragment.mBackupOpponentList.addAll(fragment.mOpponentList);
                fragment.mProgressContainer.setVisibility(View.GONE);
                fragment.mAdapter.notifyDataSetChanged();
			}
	}



    protected GetUsersResponse getRecentFunction(LambdaFunctionsInterface lambdaFunctionsInterface)
	{

		return lambdaFunctionsInterface.GetRecentBattleUsers();
	}

	protected int getListItemResId()
	{
		return R.layout.list_item_opponent_select;
	}



    //Gets the users that this user has recently battled in order
	private void fillListRecents() {
        new FillListRecentsTask(getActivity(), this).execute();
    }

    private static class FillListRecentsTask extends AsyncTask<Void, Void, AsyncTaskResult<GetUsersResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ChooseOpponentFragment> fragmentReference;

        FillListRecentsTask(Activity activity, ChooseOpponentFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<GetUsersResponse> doInBackground(Void... params) {
		// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
		LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
				Regions.US_EAST_1,
                IdentityManager.getDefaultIdentityManager().getCredentialsProvider());

		        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);


				try {
					GetUsersResponse response = fragmentReference.get().getRecentFunction(lambdaFunctionsInterface);
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
			protected void onPostExecute(AsyncTaskResult<GetUsersResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                ChooseOpponentFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

				GetUsersResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;

                }
                fragment. mOpponentList.clear();
                fragment.mOpponentList.addAll(result.getSqlResult());
                fragment.mBackupOpponentList.clear();
                fragment.mBackupOpponentList.addAll(fragment.mOpponentList);
                fragment.mAdapter.notifyDataSetChanged();
                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
			}
	}


    private void downloadProfilePicture(final User op, final OpponentAdapter.MyViewHolder holder)
    {
        final OtherUsersProfilePicCacheManager profilePicCache = OtherUsersProfilePicCacheManager.getProfilePicCache(getActivity());
        profilePicCache.getSignedUrlProfilePicOpponent(op.getCognitoId(), op.getProfilePicCount(), op.getProfilePicSignedUrl(), getActivity(), new OtherUsersProfilePicCacheManager.SignedUrlCallback() {
            @Override
            public void onSignedPicReceived(String signedUrl) {

                Picasso.get().load(signedUrl).placeholder(R.drawable.default_profile_pic100x100).into(holder.circleImageView);
            }
        });

    }

    private class OpponentAdapter extends RecyclerView.Adapter<OpponentAdapter.MyViewHolder>
    {
        private List<User> opponentList;


        private OpponentAdapter(List<User> opponentList)
        {
            this.opponentList  = opponentList;
        }

        @Override
        public OpponentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(getListItemResId(), parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(OpponentAdapter.MyViewHolder holder, final int position) {
            final User op = this.opponentList.get(position);
            holder.username.setText(op.getUsername());
            holder.name.setText(op.getFacebookName());
            holder.circleImageView.setImageResource(R.drawable.default_profile_pic100x100);
            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CreateBattleActivity)getActivity()).setOpponent(op);
                }
            });

            holder.circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent  i = new Intent(getActivity(), UsersBattlesActivity.class);
                    i.putExtra(UsersBattlesActivity.EXTRA_COGNITO_ID, op.getCognitoId());
                    startActivity(i);
                }
            });

            //if facebook friend tab is selected, get the profile urls from the facebook graph api
            //this is a static method in another fragment

            //else get profile picture if the profile pic count != 0
            if (mOpponentTabSelector.getSelectedTabPosition() == 2) {
                FollowFacebookFriendsFragment.getFriendProfilePicture(op.getFacebookUserId(), holder.circleImageView);
            }
            else {

                if (op.getProfilePicCount() != 0) {
                    downloadProfilePicture(op, holder);
                }

            }
        }

        @Override
        public int getItemCount() {
            return this.opponentList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView username, name;
            CircleImageView circleImageView;
            View mainView;


            public MyViewHolder(View view) {
                super(view);
                username = view.findViewById(R.id.UsernameTextView);
                name = view.findViewById(R.id.NameTextView);
                circleImageView = view.findViewById(R.id.profileImageView);
                mainView= view;
            }
        }

    }


	
	private void fillFriendsList()
	{
	    //Ths method gets the facebook friends of the user that also use the app.
        //Uses facebook graph request returning facebook name

		Log.i(TAG, "Making the API call to get mutual facebook friends");
		/* make the API call */
		new GraphRequest(
		    AccessToken.getCurrentAccessToken(),
		    "/me/friends",
		    null,
		    com.facebook.HttpMethod.GET,
		    new GraphRequest.Callback() {
				@Override
				public void onCompleted(GraphResponse response) 
				{
					
					Log.i(TAG, "Received graph request callback");

                    mOpponentList.clear();
					mOpponentList.addAll(GraphResponseToOpponentList(response));
                    mBackupOpponentList.clear();
                    mBackupOpponentList.addAll(mOpponentList);
					mAdapter.notifyDataSetChanged();
					mProgressContainer.setVisibility(View.INVISIBLE);
				}
		    }
		).executeAsync();
	}
	
	public static ArrayList<User> GraphResponseToOpponentList(GraphResponse response)
	{
	    //This method converts the facbebook graph response to use the data in an opponent list

		ArrayList<User> opponentListFromGraph = new ArrayList<>();
		if (response.getError() != null)
		{ Log.e(TAG, "ERROR: " + response.getError());}
		else
		{
			try {
                JSONArray friendsList = response.getJSONObject().getJSONArray("data");
                JSONObject friend;
                String friendName;
                String friendId;

                for (int i=0; i < friendsList.length(); i++)
                {
                    friend = friendsList.getJSONObject(i);
                    friendName = friend.getString("name");
                    friendId = friend.getString("id");
                    User u = new User(friendName, friendId);
                    //Opponent op = new Opponent(null, friendName, "", friendId);
                    Log.i(TAG, "Friend name = " + friendName + ", Friend ID: " + friendId);
                    //Add friend to array list
                    opponentListFromGraph.add(u);
                }
			}
			catch (JSONException e) {
			    //return empty list
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return opponentListFromGraph;
	}


}
