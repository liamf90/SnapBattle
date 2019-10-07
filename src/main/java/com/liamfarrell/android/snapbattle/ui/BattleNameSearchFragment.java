package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BattleTypeSuggestionsSearchRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.SuggestionsResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.liamfarrell.android.snapbattle.views.SimpleDividerItemDecoration;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class BattleNameSearchFragment extends Fragment implements androidx.appcompat.widget.SearchView.OnQueryTextListener
{
    public static final int MIN_TIME_WAIT_UNTIL_SEARCH_TEXT_CHANGED_MILLISECONDS = 400;

    private View mProgressContainer;
    private static String TAG = "BattleNameSearchFragment";
    private ArrayList<SuggestionsResponse> mBattleList;
    private  BattleNameAdapter mAdapter;
    private androidx.appcompat.widget.SearchView mSearchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);


        super.onCreate(savedInstanceState);
        mSearchView = ((SearchUsersAndBattlesActivity)getActivity()).getSearchView();
        mBattleList = new ArrayList<>();
        mAdapter = new BattleNameAdapter(mBattleList);


    }

    public void setQueryChangeListener(androidx.appcompat.widget.SearchView searchView)
    {
        searchView.setOnQueryTextListener( this);
    }




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)
    {
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        mProgressContainer = view.findViewById(R.id.progressContainer);
        mProgressContainer.setVisibility(View.GONE);
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






    private void PerformUpdate(final String searchName) {
        BattleTypeSuggestionsSearchRequest request = new BattleTypeSuggestionsSearchRequest();
        request.setSearchName(searchName);
        new PerformUpdateTask(getActivity(), this, searchName).execute(request);
    }

    private static class PerformUpdateTask extends AsyncTask<BattleTypeSuggestionsSearchRequest, Void, AsyncTaskResult<BattleTypeSuggestionsSearchResponse>>{

        private WeakReference<Activity> activityReference;
        private WeakReference<BattleNameSearchFragment> fragmentReference;
        private String searchName;

        PerformUpdateTask(Activity activity, BattleNameSearchFragment fragment, String searchName)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
            this.searchName = searchName;
        }

        @Override
        protected  AsyncTaskResult<BattleTypeSuggestionsSearchResponse> doInBackground(BattleTypeSuggestionsSearchRequest... params) {
        Log.i(TAG, "Performing Update");
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get().getApplicationContext()));

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);


        // The Lambda function invocation results in a network call
        // Make sure it is not called from the main thread


        // invoke "echo" method. In case it fails, it will throw a
        // LambdaFunctionException.
        try {
            BattleTypeSuggestionsSearchResponse response = lambdaFunctionsInterface.BattleTypeSuggestionsSearch(params[0]);
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
    protected void onPostExecute( AsyncTaskResult<BattleTypeSuggestionsSearchResponse> asyncResult) {
        // get a reference to the callbacks if it is still there
        BattleNameSearchFragment fragment = fragmentReference.get();
        Activity activity = activityReference.get();
        if (fragment == null || fragment.isRemoving()) return;
        if (activity == null || activity.isFinishing()) return;

        BattleTypeSuggestionsSearchResponse result = asyncResult.getResult();
        if (asyncResult.getError() != null)
        {
            new HandleLambdaError().handleError(asyncResult.getError(), activity, fragment.mProgressContainer);
            return;
        }

        if ( searchName.equals( fragment.mSearchView.getQuery().toString())) {
            fragment.mBattleList.clear();
            if (result.getSqlResult().size() > 0) {
                fragment.mBattleList.addAll(result.getSqlResult());
                fragment.mAdapter.setState(UserSearchFragment.State.SHOW_LIST);
                fragment.mAdapter.notifyDataSetChanged();
            }
            else
            {
                fragment.displayNoSearchResultsMessage();
            }
        }
    }



    }





    @Override
    public boolean onQueryTextSubmit(String query) {
        //do final search
        displayLoadingMessage();
        if (query.length() != 0)
        {
            PerformUpdate(query);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {

        final String textStart = mSearchView.getQuery().toString();


        if (textStart.length() >= 3)
        {
            //Display loading sign
            displayLoadingMessage();
            final Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("Search", "point 1");
                        Thread.sleep(MIN_TIME_WAIT_UNTIL_SEARCH_TEXT_CHANGED_MILLISECONDS);
                        if (textStart.equals(mSearchView.getQuery().toString()))
                        {
                            Log.i("Search", "point 2");
                            PerformUpdate(mSearchView.getQuery().toString());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            };
            Thread t1 = new Thread(run);
            t1.start();


        }
        else
        {
            mBattleList.clear();
            mAdapter.notifyDataSetChanged();
        }

        return false;
    }

    private void displayLoadingMessage()
    {
        mAdapter.setState(UserSearchFragment.State.LOADING);
        SuggestionsResponse result = new SuggestionsResponse();
        result.setBattleName("");
        result.setCount(1);
        mBattleList.clear();
        mBattleList.add(result);
        mAdapter.notifyDataSetChanged();
    }

    private void displayNoSearchResultsMessage()
    {
        mAdapter.setState(UserSearchFragment.State.NO_RESULTS);
        SuggestionsResponse result = new SuggestionsResponse();
        result.setBattleName("");
        result.setCount(1);
        mBattleList.clear();
        mBattleList.add(result);
        mAdapter.notifyDataSetChanged();
    }



    private class BattleNameAdapter extends RecyclerView.Adapter<BattleNameAdapter.MyViewHolder>
    {
        private UserSearchFragment.State mState;
        private ArrayList<SuggestionsResponse> battleSearchResultList;

        public class MyViewHolder extends RecyclerView.ViewHolder {


            TextView battleNameTextView;
            TextView messageDisplay;
            TextView battleCountTextView;
            View v;


            public MyViewHolder(View view) {
                super(view);
                battleNameTextView = view.findViewById(R.id.battleNameTextView);
                 battleCountTextView = view.findViewById(R.id.battleNameCountTextView);

                messageDisplay = view.findViewById(android.R.id.text1);


            }
        }
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView;
            switch (viewType)
            {
                case R.layout.list_item_search_battle: {
                    itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_search_battle, parent, false);
                    return new MyViewHolder(itemView);
                }
                case android.R.layout.simple_list_item_1: {
                    itemView = LayoutInflater.from(parent.getContext())
                            .inflate(android.R.layout.simple_list_item_1, parent, false);
                    return new MyViewHolder(itemView);


                }
                case R.layout.list_item_loading: {
                    itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_loading, parent, false);
                    return new MyViewHolder(itemView);

                }
                default:
                {
                    itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_search_battle, parent, false);
                    return new MyViewHolder(itemView);

                }


            }

        }

        @Override
        public int getItemViewType(final int position) {
            switch (mState) {
                case SHOW_LIST: {
                    return R.layout.list_item_search_battle;
                }
                case NO_RESULTS: {

                    return android.R.layout.simple_list_item_1;
                }
                case LOADING: {
                    return R.layout.list_item_loading;
                }
                default : return android.R.layout.simple_list_item_1;

            }
        }



        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {


            switch (mState)
            {
                case SHOW_LIST: {
                    final SuggestionsResponse b = battleSearchResultList.get(position);
                    holder.battleNameTextView.setText(b.getBattleName());
                    holder.battleCountTextView.setText(getResources().getQuantityString(R.plurals.battle_count, b.getCount(), b.getCount()));
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SuggestionsResponse chosenBattleName = (SuggestionsResponse) mBattleList.get(position);
                            Intent i = new Intent(getActivity(), ViewBattlesFromNameActivity.class);
                            i.putExtra(ViewBattlesFromNameActivity.EXTRA_BATTLE_NAME, chosenBattleName.getBattleName());
                            startActivity(i);

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
            return battleSearchResultList.size();
        }



        private BattleNameAdapter(ArrayList<SuggestionsResponse> battleSearchResultList) {

            this.battleSearchResultList = battleSearchResultList;
            mState = UserSearchFragment.State.SHOW_LIST;
        }


        public void setState(UserSearchFragment.State state)
        {
            mState = state;
        }









    }

}






