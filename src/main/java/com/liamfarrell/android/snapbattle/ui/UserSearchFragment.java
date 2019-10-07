package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
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
import com.google.gson.JsonParser;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.SignedUrlsRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UsersSearchRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetSignedUrlsResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache;
import com.liamfarrell.android.snapbattle.caches.OtherUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.model.User;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetNewSignedUrlResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.liamfarrell.android.snapbattle.views.SimpleDividerItemDecoration;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.FollowingSort;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSearchFragment extends Fragment implements androidx.appcompat.widget.SearchView.OnQueryTextListener
{
    private static String TAG = "OpponentList";



    public enum State
    {
        LOADING,
        NO_RESULTS,
        SHOW_LIST;
    }


    private View mProgressContainer;
    private ArrayList<FollowingSort> mUserList;
    private FollowingUserCache mSFollowingUserCache;
    private UserSearchAdapter mAdapter;
    private androidx.appcompat.widget.SearchView searchView;


    public static final String EXTRA_FACEBOOK_ID = "com.liamfarrell.android.snapbattle.opponentfacebookid";
    public static final String EXTRA_FACEBOOK_NAME = "com.liamfarrell.android.snapbattle.opponentfacebookname";
    public static final String EXTRA_COGNITO_ID = "com.liamfarrell.android.snapbattle.opponentcognitoid";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //searchView = ((SearchUsersAndBattlesActivity)getCallbacks()).getSearchView();
        mUserList = new ArrayList<>();
        mAdapter = new UserSearchAdapter(mUserList);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)
    {
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        mProgressContainer = view.findViewById(R.id.progressContainer);
        loadFollowingList();

        RecyclerView mRecyclerView = view.findViewById(R.id.recyclerList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                getActivity().getApplicationContext()
        ));
        mRecyclerView.setAdapter(mAdapter);
        setRetainInstance(true);
        return view;
    }



    public void setOnQueryChangedListener(@NotNull SearchView searchView) {
        searchView.setOnQueryTextListener( this);
    }

    private void loadFollowingList()
    {
        FollowingUserCache.CacheLoadCallbacks callbacks = new FollowingUserCache.CacheLoadCallbacks() {
            @Override
            public void onLoadedFromSQL() {
                //ready
                mProgressContainer.setVisibility(View.GONE);

            }

            @Override
            public void onLoadedFromFile() {
                mProgressContainer.setVisibility(View.GONE);
            }

            @Override
            public void onCacheAlreadyLoaded() {
                mProgressContainer.setVisibility(View.GONE);
            }

            @Override
            public void onUpdated() {
                //re download the following list.

            }

            @Override
            public void onNoUpdates() {
                //re download the following list.

            }
        };
        mSFollowingUserCache = new FollowingUserCache(getActivity(), callbacks);
    }



    private void PerformUpdate(final String searchName) {
        UsersSearchRequest request = new UsersSearchRequest();
        request.setUserSearchQuery(searchName);
        new PerformUpdateTask(getActivity(), this, searchName).execute(request);
    }

    private static class PerformUpdateTask extends AsyncTask<UsersSearchRequest, Void, AsyncTaskResult<GetUsersResponse>>
    {
        private String searchName;
        private WeakReference<Activity> activityReference;
        private WeakReference<UserSearchFragment> fragmentReference;

        PerformUpdateTask(Activity activity, UserSearchFragment fragment, String searchName)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
            this.searchName = searchName;
        }
        @Override
        protected AsyncTaskResult<GetUsersResponse> doInBackground(UsersSearchRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class );



        // The Lambda function invocation results in a network call
        // Make sure it is not called from the main thread

                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    GetUsersResponse response =  lambdaFunctionsInterface.UserSearch(params[0]);
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
                UserSearchFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                GetUsersResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {

                    if (asyncResult.getError() instanceof AmazonServiceException)
                    {

                        Toast.makeText(activity, R.string.server_error_toast, Toast.LENGTH_LONG).show();
                        return;
                    }
                    else if (asyncResult.getError() instanceof LambdaFunctionException)
                    {
                        JsonParser parser = new JsonParser();
                        if (parser.parse(((LambdaFunctionException) asyncResult.getError()).getDetails()).getAsJsonObject().get("errorType") != null) {
                            String errorType = parser.parse(((LambdaFunctionException) asyncResult.getError()).getDetails()).getAsJsonObject().get("errorType").getAsString();
                            if (errorType.equals(LambdaFunctionsInterface.UPGRADE_REQUIRED_ERROR_MESSAGE))
                            {
                                Toast.makeText(activity, R.string.upgrade_required_toast_message, Toast.LENGTH_LONG).show();
                                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
                            }
                        }
                        else
                        {
                            Toast.makeText(activity, R.string.server_error_toast, Toast.LENGTH_LONG).show();
                            fragment.mProgressContainer.setVisibility(View.INVISIBLE);
                        }
                        return;
                    }
                    else if (asyncResult.getError() instanceof AmazonClientException)
                    {

                        Toast.makeText(activity, R.string.no_internet_connection_toast, Toast.LENGTH_LONG).show();
                        return;
                    }


                }

               //check that the search field is still the same as the returned search result
                if (searchName.equals(fragment.searchView.getQuery().toString())) {
                    if (result.getSqlResult().size() > 0) {
                        FollowingSort f;

                        //remove loading messageTextView
                        fragment.mUserList.remove(fragment.mUserList.size() -1);
                        fragment.mAdapter.notifyItemRemoved(fragment.mUserList.size());
                        int positionStartInsert = fragment.mUserList.size();
                        int count = 0;

                        for (User user : result.getSqlResult()) {
                            f = new FollowingSort(user.getFacebookName(), user.getCognitoId(), user.getUsername(), user.getFacebookUserId(), user.getProfilePicCount(), user.getProfilePicSignedUrl());
                            //Log.i("USerSearch", "Username 1: " + f.getUsername());
                            boolean userListContainsFollowing = false;



                            for (User fsearch : fragment.mUserList)
                            {
                                if (fsearch.getCognitoId().equals(f.getCognitoId()))
                                {
                                    userListContainsFollowing = true;
                                    Log.i("USerSearch", "Already in da fsearch");
                                }
                            }
                            if (!userListContainsFollowing) {
                                fragment.mUserList.add(f);
                                Log.i("USerSearch", "Username 2: " + f.getUsername());
                                count ++;
                            }


                        }
                        fragment.mAdapter.setState(State.SHOW_LIST);
                        fragment.mAdapter.notifyItemRangeInserted(positionStartInsert,count );
                    } else {

                        fragment.displayNoSearchResultsMessage();
                    }
                }
            }

    }

    private void GetNewSignedUrlsForCachedUsers(ArrayList<String> cognitoIDFollowingCacheSearchResultList) {
        SignedUrlsRequest request = new SignedUrlsRequest();
        request.setCognitoIdToGetSignedUrlList(cognitoIDFollowingCacheSearchResultList);
        new GetSignedUrlsTask(getActivity(), this).execute(request);
    }

    private static class GetSignedUrlsTask extends AsyncTask<SignedUrlsRequest, Void,  AsyncTaskResult<GetSignedUrlsResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<UserSearchFragment> fragmentReference;

        GetSignedUrlsTask(Activity activity, UserSearchFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }
        @Override
        protected  AsyncTaskResult<GetSignedUrlsResponse> doInBackground(SignedUrlsRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get().getApplicationContext()));

            final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);

                try {
                    GetSignedUrlsResponse response =  lambdaFunctionsInterface.GetProfilePicSignedUrls(params[0]);
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
                    return new AsyncTaskResult<>(ace);
                    //return null;
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<GetSignedUrlsResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                UserSearchFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;
                GetSignedUrlsResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {

                    new HandleLambdaError().handleError(asyncResult.getError(), activity, fragment.mProgressContainer);
                    return;
                }
                //get new signed urls for cached following users if there was an error on setting the profile pic or the profile pic has changed

                for (GetNewSignedUrlResponse newSignedUrl: result.getNewSignedUrls())
                {
                    for (int i = 0; i < fragment.mUserList.size(); i++)
                    {
                        FollowingSort fs = fragment.mUserList.get(i);
                        //check fs is not a dummy fs to show searching.. etc
                        if (!fs.getFacebookName().equals("00") && fs.getCognitoId().equals(newSignedUrl.getCognitoId()))
                        {
                            if (fs.getHasProfilePicDownloadError() ||
                                    fs.getProfilePicCount() != newSignedUrl.getProfilePicCount())
                            {
                                fragment.mSFollowingUserCache.updateSignedUrl(activity, newSignedUrl.getCognitoId(), newSignedUrl.getProfilePicCount(), newSignedUrl.getNewSignedUrl());
                                User fol = fragment.mSFollowingUserCache.getFollowing(newSignedUrl.getCognitoId());
                                fragment.mUserList.set(i,new FollowingSort(fol.getFacebookName(), fol.getCognitoId(),fol.getUsername(), fol.getFacebookUserId(), fol.getProfilePicCount(), fol.getProfilePicSignedUrl()));
                                fragment. mAdapter.notifyItemChanged(i);
                            }

                        }
                    }

                }
            }



    }

    private void displayLoadingMessage()
    {
        int positionInserted = mUserList.size();
        mAdapter.setState(State.LOADING);
        mUserList.add(new FollowingSort("00", "00"));
        mAdapter.notifyItemInserted(positionInserted);
    }

    private void displayNoSearchResultsMessage()
    {
        mAdapter.setState(State.NO_RESULTS);
        mUserList.clear();
        mUserList.add(new FollowingSort("00", "00"));
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        //do final search

        if (query.length() != 0)
        {
            displayLoadingMessage();
            PerformUpdate(query);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        final String textStart = searchView.getQuery().toString();


        if (textStart.length() > 0)
        {

            if (textStart.length() < 3)
            {
                mAdapter.setState(State.SHOW_LIST);
            }
            if (textStart.length() == 1)
            {
                //get signed urls for the results
                searchFollowingCacheAddToUserList(textStart, true);
            }
            else
            {
                searchFollowingCacheAddToUserList(textStart, false);
            }

        }
        else {
            mUserList.clear();
            mAdapter.setState(State.SHOW_LIST);
            mAdapter.notifyDataSetChanged();
        }


        if (textStart.length() >= 3)
        {

            //Display loading sign
            displayLoadingMessage();
            final Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(BattleNameSearchFragment.MIN_TIME_WAIT_UNTIL_SEARCH_TEXT_CHANGED_MILLISECONDS);
                        if (textStart.equals(searchView.getQuery().toString()))
                        {
                            PerformUpdate(searchView.getQuery().toString());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            };
            Thread t1 = new Thread(run);
            t1.start();


        }




        return false;

    }



    private void searchFollowingCacheAddToUserList(String searchQuery, boolean updateSignedUrlsServer)
    {
        mUserList.clear();
        ArrayList<String> cognitoIDGetSignedUrlServerList = new ArrayList<>();
        ArrayList<User> searchMatchesList = new ArrayList<>();
        Collection<User> followingList = mSFollowingUserCache.getFollowerList();
        Log.i("UserSearch: ", "Follow list size: " + followingList.size());
        for (User f : followingList)
        {
            if (isFollowingUserMatchSearch(f, searchQuery))
            {
                searchMatchesList.add(f);
            }
        }
        for (User f : searchMatchesList)
        {
            FollowingSort fs = new FollowingSort(f.getFacebookName(), f.getCognitoId(),f.getUsername(), f.getFacebookUserId(), f.getProfilePicCount(), f.getProfilePicSignedUrl());
            fs.setSortFactor(20);
            mUserList.add(fs);
            cognitoIDGetSignedUrlServerList.add(fs.getCognitoId());

        }

        if(updateSignedUrlsServer && mUserList.size() > 0)
        {


            GetNewSignedUrlsForCachedUsers(cognitoIDGetSignedUrlServerList);
        }

        mAdapter.notifyDataSetChanged();

    }
    private boolean isFollowingUserMatchSearch(User f, String searchQuery)
    {
        //TODO: match username as well
        if (f.getFacebookName().toLowerCase().startsWith(searchQuery)){
            return true;
        }
        String[] splitName = f.getFacebookName().toLowerCase().split(" ");
        for (String name : splitName)
        {
            if (name.startsWith(searchQuery.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }



    private class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.MyViewHolder>
    {
        private State mState;
        private ArrayList<FollowingSort> mFollowingSort;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            CircleImageView profileImageView;
            TextView facebookNameTextView;
            TextView messageDisplay;
            TextView userNameTextView;
            View v;


            public MyViewHolder(View view) {
                super(view);
                switch (mState) {
                    case SHOW_LIST: {

                        profileImageView = view.findViewById(R.id.profilePic);
                        facebookNameTextView = view.findViewById(R.id.nameTextView);
                        userNameTextView = view.findViewById(R.id.userNameTextView);

                    }
                    case NO_RESULTS: {
                        messageDisplay = view.findViewById(android.R.id.text1);
                    }
                    case LOADING: {
                        messageDisplay = view.findViewById(android.R.id.text1);
                    }

                }




            }
        }
        public UserSearchAdapter(ArrayList<FollowingSort> userSearchResultList) {

            this.mFollowingSort = userSearchResultList;
            mState = State.SHOW_LIST;
        }


        public void setState(State state)
        {
            mState = state;
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(viewType, parent, false);
            return new MyViewHolder(itemView);

        }

        @Override
        public int getItemViewType(final int position) {
            switch (mState) {
                case SHOW_LIST: {
                    return R.layout.list_item_search_user;
                }
                case NO_RESULTS: {
                    if (position == mFollowingSort.size() - 1) {
                        return android.R.layout.simple_list_item_1;
                    }
                    else
                    {
                        return R.layout.list_item_search_user;
                    }
                }
                case LOADING: {
                    if (position == mFollowingSort.size() - 1)
                    {
                        return R.layout.list_item_loading;
                    }
                    else
                    {
                        return R.layout.list_item_search_user;
                    }

                }
                default : return android.R.layout.simple_list_item_1;

            }
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {


            switch (mState)
            {
                case SHOW_LIST: {
                    final User f = mFollowingSort.get(position);
                    holder.facebookNameTextView.setText(f.getFacebookName());
                    Log.i(TAG, "Username: " + f.getUsername());
                    holder.userNameTextView.setText(f.getUsername());
                    Picasso.get().cancelRequest( holder.profileImageView);
                    holder.profileImageView.setImageResource(R.drawable.default_profile_pic100x100);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            User chosenUser = mFollowingSort.get(position);
                            Intent  i = new Intent(getActivity(), UsersBattlesActivity.class);
                            i.putExtra(UsersBattlesActivity.EXTRA_COGNITO_ID, chosenUser.getCognitoId());
                            startActivity(i);

                        }
                    });

                    Log.i(TAG, "Getting User " + f.getFacebookName());


                    OtherUsersProfilePicCacheManager cache = OtherUsersProfilePicCacheManager.getProfilePicCache(getActivity());
                    cache.getSignedUrlProfilePicOpponent(f.getCognitoId(), f.getProfilePicCount(), f.getProfilePicSignedUrl(), getActivity(), new OtherUsersProfilePicCacheManager.SignedUrlCallback() {
                        @Override
                        public void onSignedPicReceived(String signedUrl) {
                            Log.i(TAG, "Signed url received: " + signedUrl);
                            if (f.getProfilePicCount() > 0) {
                                Picasso.get().load(signedUrl).error(R.drawable.default_profile_pic100x100).placeholder(R.drawable.default_profile_pic100x100).into(holder.profileImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.i(TAG, "Success Loading ProfilePOJO Pic");
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.i(TAG, "ERROR Loading ProfilePOJO Pic");



                                            if (holder.getAdapterPosition() != -1 && mFollowingSort.get(holder.getAdapterPosition()) != null) {
                                                mFollowingSort.get(holder.getAdapterPosition()).setProfilePicDownloadError(true);
                                            }


                                    }
                                });
                                //if error. reload it

                            }
                        }
                    });

                    return;
                }
                case NO_RESULTS: {
                    holder.messageDisplay.setText(R.string.no_results);
                    return;
                }
                case LOADING: {
                }

            }
        }

        @Override
        public int getItemCount() {
            return mFollowingSort.size();
        }











    }





}
