package com.liamfarrell.android.snapbattle.ui.startup;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing;
import com.liamfarrell.android.snapbattle.ui.FollowFacebookFriendsFragment;
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.User;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FollowUserWithFacebookIDsRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.liamfarrell.android.snapbattle.views.SimpleDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Liam on 30/12/2017.
 */

public class AddFacebookFriendsAsFollowersStartupFragment extends Fragment {

        private ArrayList<User> mOpponenList;
        private ArrayList<User> mOpponentSelectedList;
        private View mProgressContainer;
        private int mCheckBoxCheckedCount = 0;
        private OpponentAdapterSelect mAdapter;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)
        {
            View view = inflater.inflate(R.layout.fragment_add_followers_select, container, false);
            setRetainInstance(true);
            mProgressContainer = view.findViewById(R.id.opponent_list_progressContainer);
            mProgressContainer.setVisibility(View.VISIBLE);
            mOpponenList = new ArrayList<User>();
            mOpponentSelectedList = new ArrayList<>();
            RecyclerView mRecyclerView = view.findViewById(R.id.recyclerList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                    getActivity().getApplicationContext()
            ));

            //fill opponent list with all facebook friends that use app.
            //so user can follow these users
            mAdapter = new OpponentAdapterSelect(mOpponenList);
            mRecyclerView.setAdapter(mAdapter);
            fillFriendsList();
            return view;
        }

        private void fillFriendsList()
        {
            //Graph request gets the users friends that use the app
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/friends",
                    null,
                    com.facebook.HttpMethod.GET,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response)
                        {
                            mProgressContainer.setVisibility(View.GONE);
                            mOpponenList.addAll(GraphResponseToOpponentList(response));
                            if (mOpponenList.size() == 0) {
                                //Go to next fragment
                                if (getActivity() != null) {
                                    ((StartupActivity) getActivity()).nextFragment();
                                }
                            } else {
                                mAdapter.notifyDataSetChanged();
                            }

                        }
                    }
            ).executeAsync();
        }

    private class OpponentAdapterSelect extends RecyclerView.Adapter<OpponentAdapterSelect.MyViewHolder>
    {
        private ArrayList<User> opponentList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            CircleImageView circleImageView;
            Button modifyFollowerButton;
            CheckBox followCheckBox;
            public MyViewHolder(View view) {
                super(view);
                tv = view.findViewById(R.id.facebookUsernameTextView);
                circleImageView = view.findViewById(R.id.profileImageView);
                modifyFollowerButton = view.findViewById(R.id.modifyFollowerButton);
                followCheckBox = view.findViewById(R.id.addFollowerCheckBox);
            }
        }
        public OpponentAdapterSelect(ArrayList<User> opponentFollowList)
        {
            this.opponentList = opponentFollowList;
        }


        @Override
        public OpponentAdapterSelect.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_facebook_friends_select, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final OpponentAdapterSelect.MyViewHolder holder, int position) {
            final User follower = this.opponentList.get(position);
            holder.circleImageView.setImageResource(R.drawable.default_profile_pic100x100);
            FollowFacebookFriendsFragment.getFriendProfilePicture(follower.getFacebookUserId(),  holder.circleImageView);
            holder.tv.setText(follower.getFacebookName());
            holder.followCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b)
                    {
                        mCheckBoxCheckedCount++;
                        mOpponentSelectedList.add(follower);
                    }
                    else
                    {
                        mCheckBoxCheckedCount--;
                        mOpponentSelectedList.remove(follower);
                    }

                    //TODO QUESTIONABLE CODE??
                    if (mCheckBoxCheckedCount == 0)
                    {
                        ((StartupActivity)getActivity()).setEnableNextButton(false);
                    }
                    else
                    {
                        ((StartupActivity)getActivity()).setEnableNextButton(true);
                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            return opponentList.size();
        }
    }

        public void addFollowers() {
            mProgressContainer.setVisibility(View.VISIBLE);
            ArrayList<String> facebookFriendsToAddList = new ArrayList<>();
            for (User userToAdd: mOpponentSelectedList)
            {
                facebookFriendsToAddList.add(userToAdd.getFacebookUserId());
            }
            FollowUserWithFacebookIDsRequest addFollowerRequest = new FollowUserWithFacebookIDsRequest();
            addFollowerRequest.setFacebookFriendIdList(facebookFriendsToAddList);
            new AddFollowersTask(getActivity(), this).execute(addFollowerRequest);
        }

        private static class AddFollowersTask extends AsyncTask<FollowUserWithFacebookIDsRequest, Void, AsyncTaskResult<ResponseFollowing>>
        {
            private WeakReference<Activity> activityReference;
            private WeakReference<AddFacebookFriendsAsFollowersStartupFragment> fragmentReference;

            AddFollowersTask(Activity activity, AddFacebookFriendsAsFollowersStartupFragment fragment)
            {
                fragmentReference = new WeakReference<>(fragment);
                activityReference = new WeakReference<>(activity);
            }


            @Override
            protected  AsyncTaskResult<ResponseFollowing> doInBackground(FollowUserWithFacebookIDsRequest... params) {

            // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
                // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
                LambdaInvokerFactory factory = new LambdaInvokerFactory(
                        activityReference.get().getApplicationContext(),
                        Regions.US_EAST_1,
                        FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

            final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
                    try {
                        ResponseFollowing response =   lambdaFunctionsInterface.AddFollower (params[0]);
                        return new AsyncTaskResult<>(response);
                    } catch (LambdaFunctionException lfe) {
                        Log.i("ERROR", lfe.getDetails());
                        Log.i("ERROR",lfe.getStackTrace().toString());
                        lfe.printStackTrace();

                        return new AsyncTaskResult<>(lfe);                    }
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
                protected void onPostExecute(AsyncTaskResult<ResponseFollowing> asyncResult)  {
                    // get a reference to the activity and fragment if it is still there
                    AddFacebookFriendsAsFollowersStartupFragment fragment = fragmentReference.get();
                    Activity activity = activityReference.get();
                    if (fragment == null || fragment.isRemoving()) return;
                    if (activity == null || activity.isFinishing()) return;

                    if (asyncResult.getError() != null)
                    {
                        new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                        return;
                    }
                    //Update following user cache
                    FollowingUserCache.get(activity.getApplicationContext(), null).updateCache(activity.getApplicationContext(),null);
                    fragment.mProgressContainer.setVisibility(View.INVISIBLE);

                    //Go to next fragment
                    ((StartupActivity)activity).nextFragment();
                }
        }



        private ArrayList<User> GraphResponseToOpponentList(GraphResponse response)
        {
            ArrayList<User> opponentListFromGraph = new ArrayList<User>();
            if (response.getError() != null)
            { Log.e("AddFacebookFriends", "ERROR: " + response.getError());}
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
                        opponentListFromGraph.add(u );
                    }
                }
                catch (JSONException e) {
                    //return null
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            return opponentListFromGraph;

        }

}
