package com.liamfarrell.android.snapbattle.ui.createbattle;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.SuggestionsResponse;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.BattleTypeSuggestionsSearchRequest;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.liamfarrell.android.snapbattle.util.TopAndRecentBattleTypeRetriever;
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liam on 3/05/2017.
 */

public class ChooseBattleTypeFragment extends Fragment {
    private static final String TAG = "ChooseBattleTypeFrag";
    private TextView topBattles1TextView, topBattles2TextView, topBattles3TextView, topBattles4TextView,
            topBattles5TextView, topBattles6TextView, topBattles7TextView, topBattles8TextView, recentBattles1TextView, recentBattles2TextView, recentBattles3TextView,
            recentBattles4TextView, recentBattles5TextView, recentBattles6TextView, recentBattles7TextView,
            recentBattles8TextView;


    private ArrayList<SuggestionsResponse> mBattleNameSuggestionsList;
    private BattleNameSuggestionsAdapter mAdapter;
    private Long timeOfLastSuggestionsCheck;

    private final long MIN_MILLISECONDS_BETWEEN_SERVER_CHECK = 1000; // 1 second

    private ArrayList<String> mTopBattlesList;
    private ArrayList<String> mRecentBattlesList;

    private AppCompatAutoCompleteTextView battleTypeEditText;

    private ProgressBar mRecentBattlesProgressSpinner, mTopBattlesProgressSpinner;
    private boolean mIsTopBattlesReceived;
    private boolean mIsRecentBattlesReceived;
    private Thread t1;
    //this boolean is used so if an top or recent battle name is selected, an suggestion update search on the server wont be done.
    private boolean mTopOrRecentBattleNameSelected = false;



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mIsTopBattlesReceived = false;
        mIsRecentBattlesReceived = false;
        setRetainInstance(true);

        final TopAndRecentBattleTypeRetriever topBattleReceiver = new TopAndRecentBattleTypeRetriever(getActivity());
        TopAndRecentBattleTypeRetriever.BattleTypeCallbacks callbacks = new TopAndRecentBattleTypeRetriever.BattleTypeCallbacks() {
            @Override
            public void onTopBattlesReceived(ArrayList<String> arrayList)
            {
                mIsTopBattlesReceived = true;
                mTopBattlesList = new ArrayList<>();
                mTopBattlesList.addAll(arrayList);

                if (mIsRecentBattlesReceived)
                {
                    mTopBattlesProgressSpinner.setVisibility(View.GONE);
                    mRecentBattlesProgressSpinner.setVisibility(View.GONE);
                    SetTopBattlesToTextViews();
                    SetRecentBattlesToTextViews();
                }

            }

            @Override
            public void onRecentBattlesReceived(ArrayList<String> arrayList)
            {
                mIsRecentBattlesReceived = true;
                mRecentBattlesList = new ArrayList<>();
                mRecentBattlesList.addAll(arrayList);

                if (mIsTopBattlesReceived)
                {
                    mTopBattlesProgressSpinner.setVisibility(View.GONE);
                    mRecentBattlesProgressSpinner.setVisibility(View.GONE);
                    SetTopBattlesToTextViews();
                    SetRecentBattlesToTextViews();
                }

            }
        };
        topBattleReceiver.getTopAndRecentBattles(callbacks);


    }

    private void setTopAndRecentBattlesOnClickListeners(final TextView recentOrTopButton)
    {
        recentOrTopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTopOrRecentBattleNameSelected = true;
                battleTypeEditText.setText( recentOrTopButton.getText());
                battleTypeEditText.setSelection(battleTypeEditText.getText().length());
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)
    {
        View v = inflater.inflate(R.layout.fragment_choose_battle_type, container, false);
        topBattles1TextView = (TextView)v.findViewById(R.id.topBattleTextView1);
        topBattles2TextView = (TextView)v.findViewById(R.id.topBattleTextView2);
        topBattles3TextView = (TextView)v.findViewById(R.id.topBattleTextView3);
        topBattles4TextView = (TextView)v.findViewById(R.id.topBattleTextView4);
        topBattles5TextView = (TextView)v.findViewById(R.id.topBattleTextView5);
        topBattles6TextView = (TextView)v.findViewById(R.id.topBattleTextView6);
        topBattles7TextView = (TextView)v.findViewById(R.id.topBattleTextView7);
        topBattles8TextView = (TextView)v.findViewById(R.id.topBattleTextView8);

        recentBattles1TextView = (TextView)v.findViewById(R.id.recentBattleTextView1);
        recentBattles2TextView = (TextView)v.findViewById(R.id.recentBattleTextView2);
        recentBattles3TextView = (TextView)v.findViewById(R.id.recentBattleTextView3);
        recentBattles4TextView = (TextView)v.findViewById(R.id.recentBattleTextView4);
        recentBattles5TextView = (TextView)v.findViewById(R.id.recentBattleTextView5);
        recentBattles6TextView = (TextView)v.findViewById(R.id.recentBattleTextView6);
        recentBattles7TextView = (TextView)v.findViewById(R.id.recentBattleTextView7);
        recentBattles8TextView = (TextView)v.findViewById(R.id.recentBattleTextView8);

        setTopAndRecentBattlesOnClickListeners(topBattles1TextView);
        setTopAndRecentBattlesOnClickListeners(topBattles2TextView);
        setTopAndRecentBattlesOnClickListeners(topBattles3TextView);
        setTopAndRecentBattlesOnClickListeners(topBattles4TextView);
        setTopAndRecentBattlesOnClickListeners(topBattles5TextView);
        setTopAndRecentBattlesOnClickListeners(topBattles6TextView);
        setTopAndRecentBattlesOnClickListeners(topBattles7TextView);
        setTopAndRecentBattlesOnClickListeners(topBattles8TextView);
        setTopAndRecentBattlesOnClickListeners(recentBattles1TextView);
        setTopAndRecentBattlesOnClickListeners(recentBattles2TextView);
        setTopAndRecentBattlesOnClickListeners(recentBattles3TextView);
        setTopAndRecentBattlesOnClickListeners(recentBattles4TextView);
        setTopAndRecentBattlesOnClickListeners(recentBattles5TextView);
        setTopAndRecentBattlesOnClickListeners(recentBattles6TextView);
        setTopAndRecentBattlesOnClickListeners(recentBattles7TextView);
        setTopAndRecentBattlesOnClickListeners(recentBattles8TextView);

        mRecentBattlesProgressSpinner = v.findViewById(R.id.recentBattlesLoadingProgressBar);
        mTopBattlesProgressSpinner  =v.findViewById(R.id.topBattlesLoadingProgressBar);

        battleTypeEditText = v.findViewById(R.id.battleTypeEditText);
        battleTypeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                //Only allow user to go to next fragment if the battle name text is not empty
                if (!s.toString().equals("")) {
                    ((CreateBattleActivity)getActivity()).setEnableNextButton(true);
                }
                else {
                    ((CreateBattleActivity)getActivity()).setEnableNextButton(false);
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //only proceed if the user has manually typed a key input into the edittext
                if (mTopOrRecentBattleNameSelected) {
                    //reset the boolean
                    mTopOrRecentBattleNameSelected = false;
                    return;
                }


                //if enough time has elapsed between key strokes and the battle name is atleast 2 in length
                //-- perform suggestions update search to show battle name suggestions to the user

                final String textBeforeWaitChecks = s.toString();
                if (s.length() > 1)
                {
                    if (timeOfLastSuggestionsCheck !=null)
                    {
                        //wait until the timebetween == min time between server check and update
                        //only if the edit text has not changed
                        Runnable r  = new Runnable() {
                            @Override
                            public void run() {

                                Long timeNow = System.currentTimeMillis();
                                Long timeBetween = timeNow - timeOfLastSuggestionsCheck;
                                if (timeBetween < MIN_MILLISECONDS_BETWEEN_SERVER_CHECK)
                                {
                                    timeNow = System.currentTimeMillis();
                                    timeBetween = timeNow - timeOfLastSuggestionsCheck;
                                    Long timeToWait = MIN_MILLISECONDS_BETWEEN_SERVER_CHECK - timeBetween;
                                    try {
                                        Thread.sleep(timeToWait);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (textBeforeWaitChecks.equals(battleTypeEditText.getText().toString()))
                                {
                                    performUpdate(textBeforeWaitChecks);
                                }
                            }
                        };
                        if (t1 != null && t1.isAlive())
                        {
                            t1.interrupt();
                        }
                        t1 = new Thread(r);
                        t1.start();
                    }
                    else
                    {
                        performUpdate(s.toString());
                    }
                }
            }
        });

        mBattleNameSuggestionsList = new ArrayList<>();

        mAdapter = new BattleNameSuggestionsAdapter(mBattleNameSuggestionsList);
        battleTypeEditText.setAdapter(mAdapter);

        //min 2 characters before suggestions box is shown
        battleTypeEditText.setThreshold(2);


        return v;
    }

    private void SetTopBattlesToTextViews()
    {
        if (mTopBattlesList.size() >= 1)
        {
            topBattles1TextView.setText(mTopBattlesList.get(0));
            topBattles1TextView.setVisibility(View.VISIBLE);
        }
        if (mTopBattlesList.size() >= 2)
        {
            topBattles2TextView.setText(mTopBattlesList.get(1));
            topBattles2TextView.setVisibility(View.VISIBLE);
        }
        if (mTopBattlesList.size() >= 3)
        {
            topBattles3TextView.setText(mTopBattlesList.get(2));
            topBattles3TextView.setVisibility(View.VISIBLE);
        }
        if (mTopBattlesList.size() >= 4)
        {
            topBattles4TextView.setText(mTopBattlesList.get(3));
            topBattles4TextView.setVisibility(View.VISIBLE);
        }
        if (mTopBattlesList.size() >= 5)
        {
            topBattles5TextView.setText(mTopBattlesList.get(4));
            topBattles5TextView.setVisibility(View.VISIBLE);
        }
        if (mTopBattlesList.size() >= 6)
        {
            topBattles6TextView.setText(mTopBattlesList.get(5));
            topBattles6TextView.setVisibility(View.VISIBLE);
        }
        if (mTopBattlesList.size() >= 7)
        {
            topBattles7TextView.setText(mTopBattlesList.get(6));
            topBattles7TextView.setVisibility(View.VISIBLE);
        }
        if (mTopBattlesList.size() >= 8)
        {
            topBattles8TextView.setText(mTopBattlesList.get(7));
            topBattles8TextView.setVisibility(View.VISIBLE);
        }
    }

    private void SetRecentBattlesToTextViews()
    {
        if (mRecentBattlesList.size() >= 1)
        {
            recentBattles1TextView.setText(mRecentBattlesList.get(0));
            recentBattles1TextView.setVisibility(View.VISIBLE);
        }
        if (mRecentBattlesList.size() >= 2)
        {
            recentBattles2TextView.setText(mRecentBattlesList.get(1));
            recentBattles2TextView.setVisibility(View.VISIBLE);
        }
        if (mRecentBattlesList.size() >= 3)
        {
            recentBattles3TextView.setText(mRecentBattlesList.get(2));
            recentBattles3TextView.setVisibility(View.VISIBLE);
        }
        if (mRecentBattlesList.size() >= 4)
        {
            recentBattles4TextView.setText(mRecentBattlesList.get(3));
            recentBattles4TextView.setVisibility(View.VISIBLE);
        }
        if (mRecentBattlesList.size() >= 5)
        {
            recentBattles5TextView.setText(mRecentBattlesList.get(4));
            recentBattles5TextView.setVisibility(View.VISIBLE);
        }
        if (mRecentBattlesList.size() >= 6)
        {
            recentBattles6TextView.setText(mRecentBattlesList.get(5));
            recentBattles6TextView.setVisibility(View.VISIBLE);
        }
        if (mRecentBattlesList.size() >= 7)
        {
            recentBattles7TextView.setText(mRecentBattlesList.get(6));
            recentBattles7TextView.setVisibility(View.VISIBLE);
        }
        if (mRecentBattlesList.size() >= 8)
        {
            recentBattles8TextView.setText(mRecentBattlesList.get(7));
            recentBattles8TextView.setVisibility(View.VISIBLE);
        }
    }
    public String getBattleName()
    {
        return this.battleTypeEditText.getText().toString();
    }


    private void performUpdate(String searchName) {
        timeOfLastSuggestionsCheck = System.currentTimeMillis();
        BattleTypeSuggestionsSearchRequest request = new BattleTypeSuggestionsSearchRequest();
        request.setSearchName(searchName);
        new PerformUpdateTask(getActivity(), this).execute(request);
    }


    private static class PerformUpdateTask extends AsyncTask<BattleTypeSuggestionsSearchRequest, Void, AsyncTaskResult<BattleTypeSuggestionsSearchResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ChooseBattleTypeFragment> fragmentReference;

        PerformUpdateTask(Activity activity, ChooseBattleTypeFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }
        @Override
        protected AsyncTaskResult<BattleTypeSuggestionsSearchResponse> doInBackground(BattleTypeSuggestionsSearchRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
           if (activityReference.get() == null) {return null;}

        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get().getApplicationContext()));

        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
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
                // get a reference to the activity and fragment if it is still there
                Log.i(TAG, "On post execute");
                ChooseBattleTypeFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                BattleTypeSuggestionsSearchResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,null);
                    return;
                }

                fragment.mBattleNameSuggestionsList.clear();
                if (result != null && result.getSqlResult().size() > 0 )
                {

                    for (SuggestionsResponse sug : result.getSqlResult())
                    {
                        //String s = sug.getBattleName();
                        fragment.mBattleNameSuggestionsList.add(sug);
                    }
                    //fragment.mAdapter.clear();
                    //fragment.mAdapter.addAll(fragment.mBattleNameSuggestionsList);
                    fragment.mAdapter.notifyDataSetChanged();
                }
                else
                {
                    //fragment.mAdapter.clear();
                    fragment.mBattleNameSuggestionsList.clear();
                    fragment.mAdapter.notifyDataSetChanged();
                }


            }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_next) {

            ((CreateBattleActivity)getActivity()).setBattleName(battleTypeEditText.getText().toString());
            ((CreateBattleActivity)getActivity()).nextFragment();


            return true;

        }
        return false;

    }

    //Battlename suggestions adapter
    private class BattleNameSuggestionsAdapter extends ArrayAdapter<SuggestionsResponse>
    {


        private Filter mFilter = new Filter() {
            @Override
            public String convertResultToString(Object resultValue) {
                return ((SuggestionsResponse)resultValue).getBattleName();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint != null) {
                    ArrayList<SuggestionsResponse> suggestions = new ArrayList<SuggestionsResponse>();
                    for (SuggestionsResponse suggestion : mBattleNameSuggestionsList) {
                        // Note: change the "contains" to "startsWith" if you only want starting matches
                        if (suggestion.getBattleName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(suggestion);
                        }
                    }

                    results.values = suggestions;
                    results.count = suggestions.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                if (results != null && results.count > 0) {
                    // we have filtered results
                    addAll((ArrayList<SuggestionsResponse>) results.values);
                } else {
                    // no filter, add entire original list back in
                    addAll(mBattleNameSuggestionsList);
                }
                notifyDataSetChanged();
            }
        };

        public BattleNameSuggestionsAdapter(List<SuggestionsResponse> battleNameList) {
            super(getActivity(), 0, battleNameList);
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(
                        //android.R.layout.simple_dropdown_item_1line, null);
                        R.layout.list_item_search_battle, parent, false);
            }
            TextView battleNameTextView = convertView.findViewById(R.id.battleNameTextView);
            TextView battleNameCountTextView = convertView.findViewById(R.id.battleNameCountTextView);

            SuggestionsResponse battleSuggestion = getItem(position);

            //TextView battleNameTextView = (TextView) super.getView(position, convertView, parent);
            battleNameTextView.setText(battleSuggestion.getBattleName());
            battleNameCountTextView.setText((getResources().getQuantityString(R.plurals.battle_count, battleSuggestion.getCount(), battleSuggestion.getCount())));

            return convertView;
        }


        @Override
        public Filter getFilter() {
            return mFilter;
        }


    }
}
