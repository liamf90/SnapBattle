package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.liamfarrell.android.snapbattle.model.User;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FollowUserWithFacebookIDsRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FollowingRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.RemoveFacebookFriendAsFollowerRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.liamfarrell.android.snapbattle.views.LinearDividerItemDecoration;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


public class FollowFacebookFriendsFragment extends Fragment
{
    private View mProgressContainer;
    private static String TAG = "OpponentList";
    private ArrayList<User> facebookFriendsList;
    private ArrayList<User> fullFollowingList;
    private OpponentAdapter adapter;
    private CallbackManager mCallbackManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)
    {
        View view = inflater.inflate(R.layout.fragment_add_followers, container, false);

        setRetainInstance(true);

        mProgressContainer = view.findViewById(R.id.opponent_list_progressContainer);
        facebookFriendsList = new ArrayList<>();
        fullFollowingList = new ArrayList<>();
        RecyclerView mRecyclerView = view.findViewById(R.id.recyclerList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setLayoutManager(mLayoutManager);
        Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider);
        RecyclerView.ItemDecoration dividerItemDecoration = new LinearDividerItemDecoration(dividerDrawable);
        mRecyclerView.addItemDecoration(dividerItemDecoration);


        //fill opponent list with all facebook friends that use app.

        adapter = new OpponentAdapter(facebookFriendsList);
        mRecyclerView.setAdapter(adapter);
        fillFriendsList();


        return view;
    }






    private class OpponentAdapter extends RecyclerView.Adapter<OpponentAdapter.MyViewHolder>
    {
        private ArrayList<User> opponentList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            CircleImageView circleImageView;
            AppCompatButton modifyFollowerButton;

            public MyViewHolder(View view) {
                super(view);
                tv = view.findViewById(R.id.facebookUsernameTextView);
                circleImageView = view.findViewById(R.id.profileImageView);
                modifyFollowerButton = view.findViewById(R.id.modifyFollowerButton);
            }
        }
        private OpponentAdapter(ArrayList<User> opponentFollowList)
        {
            this.opponentList = opponentFollowList;
        }




        @Override
        public OpponentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_facebook_friends, parent, false);


            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final OpponentAdapter.MyViewHolder holder, int position) {
            final User follower = opponentList.get(position);
            holder.circleImageView.setImageResource(R.drawable.default_profile_pic100x100);
            getFriendProfilePicture(follower.getFacebookUserId(), holder.circleImageView);
            holder.tv.setText(follower.getFacebookName());
            Log.i(TAG, "Name: " + follower.getFacebookName()+ ",  Following: " + follower.getIsFollowing());
            if (!follower.getIsFollowing())
            {
                holder.modifyFollowerButton.setText(R.string.follow);
            }
            if (follower.getIsFollowing())
            {
                holder.modifyFollowerButton.setText(R.string.unfollow);
            }

            holder.modifyFollowerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    holder.modifyFollowerButton.setEnabled(false);
                    if (follower.getIsFollowing())
                    {

                        removeFollower(follower.getFacebookUserId(), holder);
                    }
                    else if (!follower.getIsFollowing())
                    {

                        followUser(follower.getFacebookUserId(), holder);
                    }
                }
            });


        }

        @Override
        public int getItemCount() {
            return opponentList.size();
        }
    }



    private void fillFriendsList()
    {

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

                        if (response.getError() != null && response.getError().getErrorCode() == FacebookRequestError.INVALID_ERROR_CODE)
                        {
                            Toast.makeText(getActivity(), R.string.server_error_toast, Toast.LENGTH_SHORT).show();
                            mProgressContainer.setVisibility(View.GONE);
                            return;
                        }

                        facebookFriendsList.addAll(GraphResponseToOpponentList(response));

                        //if the user_friends permission has not been approved by the user, the returned reponse will have 0 friends.
                        //so if the response has 0 friends check if the user_friends permission has not been approved and ask the user if they would like to accept it.
                        if (facebookFriendsList.size()==0 && !doesUserHaveUserFriendsPermission()){
                            Toast.makeText(getActivity(), R.string.need_accept_permission_user_friends, Toast.LENGTH_SHORT).show();
                            requestUserFriendsPermission();
                            mProgressContainer.setVisibility(View.GONE);
                            return;
                        }


                        getFollowing();


                    }
                }
        ).executeAsync();
    }

    private void getFollowing() {
        fullFollowingList.clear();
        FollowingRequest request = new FollowingRequest();
        request.setShouldGetProfilePic(true);
        new GetFollowingTask(getActivity(), this).execute(request);
    }

    private static class GetFollowingTask extends AsyncTask<FollowingRequest, Void, AsyncTaskResult<ResponseFollowing>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<FollowFacebookFriendsFragment> fragmentReference;

        GetFollowingTask(Activity activity, FollowFacebookFriendsFragment fragment) {
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

            FollowFacebookFriendsFragment fragment = fragmentReference.get();
            Activity activity = activityReference.get();
            if (fragment == null || fragment.isRemoving()) return;
            if (activity == null || activity.isFinishing()) return;

            ResponseFollowing result = asyncResult.getResult();
            if (asyncResult.getError() != null)
            {
                new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                return;
            }

            fragment.fullFollowingList.clear();
            fragment.fullFollowingList.addAll(result.getSqlResult());

            //update follow/unfollow button for each user
            fragment.checkIfFollowingFacebookFriends();
        }
    }

    private void checkIfFollowingFacebookFriends()
    {
        for (int i = 0; i < facebookFriendsList.size(); i++)
        {
            for (User u : fullFollowingList)
            {
                if (u.getFacebookUserId().equals(facebookFriendsList.get(i).getFacebookUserId())){
                    facebookFriendsList.get(i).setIsFollowing(true);
                }
            }
        }
        mProgressContainer.setVisibility(View.INVISIBLE);
        adapter.notifyDataSetChanged();
    }




    public static void getFriendProfilePicture(String facebookUserId, final CircleImageView circleImageView)
    {


        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + facebookUserId  + "?fields=picture.width(40).height(40)" ,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        //Log.i("AddFacebookFriends", response.getJSONObject().toString());
                        String url = null;
                        try {
                            JSONObject data = response.getJSONObject();
                            url = data.getJSONObject("picture").getJSONObject("data").getString("url");
                            boolean isSoulette = data.getJSONObject("picture").getJSONObject("data").getBoolean("is_silhouette");
                            Log.i("AddFacebookFriends", "url: " + url);
                            if (!isSoulette)
                            {
                                Picasso.get().load(url).placeholder(R.drawable.default_profile_pic100x100).into(circleImageView);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch(NullPointerException e)
                        {
                            e.printStackTrace();
                        }

                    }
                });
        //include redirect = false paramaters to get url of picture

        request.executeAsync();


    }

    private boolean doesUserHaveUserFriendsPermission(){
        Set<String> declinedPermissions = AccessToken.getCurrentAccessToken().getDeclinedPermissions();
        return !declinedPermissions.contains("user_friends");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void requestUserFriendsPermission(){
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        fillFriendsList();
                    }

                    @Override
                    public void onCancel() {
                        //User Friends Not accepted.. Do nothing

                    }


                    @Override
                    public void onError(FacebookException e) {
                        Toast.makeText(getActivity(), R.string.server_error_toast, Toast.LENGTH_SHORT).show();
                    }
                });


        LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList("user_friends"));

    }

    public static ArrayList<User> GraphResponseToOpponentList(GraphResponse response)
    {
        ArrayList<User> opponentListFromGraph = new ArrayList<User>();


        try {
            //Log.i(TAG, "Response: " +  response.getJSONObject().getJSONArray("data").toString());
            JSONArray friendsList = response.getJSONObject().getJSONArray("data");
            JSONObject friend;
            String friendName;
            String friendId;


            for (int i=0; i < friendsList.length(); i++)
            {

                friend = friendsList.getJSONObject(i);
                friendName = friend.getString("name");
                friendId = friend.getString("id");
                User op = new User(friendName, friendId);
                Log.i(TAG, "Friend name = " + friendName + ", Friend ID: " + friendId);
                //Add friend to array list
                opponentListFromGraph.add(op);
            }

        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        return opponentListFromGraph;

    }

    private void followUser(String facebookId, OpponentAdapter.MyViewHolder holder)
    {
        new FollowUserTask(getActivity(), this, facebookId, holder).execute();
    }

    private static class FollowUserTask extends AsyncTask<Void, Void,AsyncTaskResult<ResponseFollowing>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<FollowFacebookFriendsFragment> fragmentReference;
        private String facebookIDFollow;
        private OpponentAdapter.MyViewHolder holder;

        // only retain a weak reference to the callbacks
        FollowUserTask(Activity activity, FollowFacebookFriendsFragment fragment, String facebookIDFollow, final OpponentAdapter.MyViewHolder holder) {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
            this.facebookIDFollow = facebookIDFollow;
            this.holder = holder;

        }

        protected AsyncTaskResult<ResponseFollowing> doInBackground(Void... params) {

            // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
            LambdaInvokerFactory factory = new LambdaInvokerFactory(
                    activityReference.get(),
                    Regions.US_EAST_1,
                    FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

            final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
            FollowUserWithFacebookIDsRequest addFollowerRequest = new FollowUserWithFacebookIDsRequest();
            ArrayList<String> facebookUserIdarrayList = new ArrayList<>(1);
            facebookUserIdarrayList.add(facebookIDFollow);
            addFollowerRequest.setFacebookFriendIdList(facebookUserIdarrayList);

            try {
                ResponseFollowing response = lambdaFunctionsInterface.AddFollower(addFollowerRequest);
                return new AsyncTaskResult<ResponseFollowing>(response);
            } catch (LambdaFunctionException lfe) {
                Log.i("ERROR", lfe.getDetails());
                Log.i("ERROR",lfe.getStackTrace().toString());
                lfe.printStackTrace();

                return new AsyncTaskResult<ResponseFollowing>(lfe);
            }
            catch (AmazonServiceException ase) {
                // invalid credentials, incorrect AWS signature, etc
                return new AsyncTaskResult<ResponseFollowing>(ase);
            }
            catch (AmazonClientException ace) {
                // Network issue
                Log.i("ERROR", ace.toString());
                return new AsyncTaskResult<ResponseFollowing>(ace);
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<ResponseFollowing> asyncResult) {

            // get a reference to the callbacks if it is still there
            Log.i(TAG, "On post execute");
            FollowFacebookFriendsFragment fragment = fragmentReference.get();
            Activity activity = activityReference.get();
            if (fragment == null || fragment.isRemoving()) return;
            if (activity == null || activity.isFinishing()) return;

            Log.i(TAG, "Fragment and callbacks != null");
            if (asyncResult.getError() != null)
            {
                new HandleLambdaError().handleError(asyncResult.getError(), activity, fragment.mProgressContainer);
                return;
            }

            //Update following user cache
            FollowingUserCache.get(activity.getApplicationContext(), null).updateCache(activity.getApplicationContext(),null);;

            fragment.facebookFriendsList.get(holder.getAdapterPosition()).setIsFollowing(true);
            holder.modifyFollowerButton.setEnabled(true);
            holder.modifyFollowerButton.setText(R.string.unfollow);

        }
    }


    private void removeFollower(String facebookIDUnfollow, final OpponentAdapter.MyViewHolder holder) {
        RemoveFacebookFriendAsFollowerRequest removeFollowerRequest = new RemoveFacebookFriendAsFollowerRequest();
        removeFollowerRequest.setFacebookIDUnFollow(facebookIDUnfollow);
        new RemoveFollowerTask(getActivity(), this, holder).execute(removeFollowerRequest);
    }

    private static class RemoveFollowerTask extends AsyncTask<RemoveFacebookFriendAsFollowerRequest, Void,  AsyncTaskResult<DefaultResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<FollowFacebookFriendsFragment> fragmentReference;
        private  OpponentAdapter.MyViewHolder holder;

        RemoveFollowerTask(Activity activity, FollowFacebookFriendsFragment fragment, OpponentAdapter.MyViewHolder holder) {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
            this.holder = holder;
        }


        @Override
        protected  AsyncTaskResult<DefaultResponse> doInBackground(RemoveFacebookFriendAsFollowerRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);


                try {
                    DefaultResponse response = lambdaFunctionsInterface.RemoveFacebookFriendAsFollower(params[0]);
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
            protected void onPostExecute(AsyncTaskResult<DefaultResponse> asyncResult) {
                FollowFacebookFriendsFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity, fragment.mProgressContainer);
                    return;
                }

                //Update following user cache
                FollowingUserCache.get(activity.getApplicationContext(), null).updateCache(activity.getApplicationContext(),null);;

                fragment.facebookFriendsList.get(holder.getAdapterPosition()).setIsFollowing(false);
                holder.modifyFollowerButton.setEnabled(true);
                holder.modifyFollowerButton.setText(R.string.follow);
            }
    }






}