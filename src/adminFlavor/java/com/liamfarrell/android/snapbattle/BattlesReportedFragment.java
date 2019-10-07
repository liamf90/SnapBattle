package com.liamfarrell.android.snapbattle;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.ui.FullBattleVideoPlayerActivity;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BanUserRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BanUserResponse;
import com.liamfarrell.android.snapbattle.model.ReportedBattle;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.DeleteBattleRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.IgnoreBattleRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.ReportedBattlesRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DeleteBattleResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.IgnoreBattleResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportedBattlesResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;



/**
 * Created by liamf on 21/03/2018.
 */

public class BattlesReportedFragment extends Fragment {
    public static final int BATTLES_PER_FETCH = 50;
    private int loadAmount = BATTLES_PER_FETCH;
    private View mProgressContainer;
    private boolean mAllBattlesFetched;
    private RecyclerView mRecyclerView;
    private ReportedBattlesAdapter mAdapter;
    private ArrayList<ReportedBattle> mReportedBattlesList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAllBattlesFetched = false;
        mReportedBattlesList = new ArrayList<ReportedBattle>();
        mAdapter = new ReportedBattlesAdapter(mReportedBattlesList);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_reportings, parent, false);

        mProgressContainer = v.findViewById(R.id.progressContainer);
        mRecyclerView = v.findViewById(R.id.recyclerList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        getBattles();

        return v;
    }

    private void getBattles() {
        mProgressContainer.setVisibility(View.VISIBLE);
        ReportedBattlesRequest request = new ReportedBattlesRequest();
        request.setFetchLimit(loadAmount);
        new GetBattlesTask(getActivity(), this).execute(request);
    }
    private static class GetBattlesTask extends AsyncTask<ReportedBattlesRequest, Void, AsyncTaskResult<ReportedBattlesResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<BattlesReportedFragment> fragmentReference;

        GetBattlesTask(Activity activity, BattlesReportedFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected  AsyncTaskResult<ReportedBattlesResponse> doInBackground(ReportedBattlesRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        final LambdaFunctionsInterface myInterface = factory.build(LambdaFunctionsInterface.class,  new CustomLambdaDataBinder() );

                try {
                    ReportedBattlesResponse response= myInterface.GetReportedBattles(params[0]);
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
            protected void onPostExecute( AsyncTaskResult<ReportedBattlesResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                BattlesReportedFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                ReportedBattlesResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;
                }

                fragment.mReportedBattlesList.clear();
                fragment.mReportedBattlesList.addAll(result.getSqlResult());
                fragment.mAdapter.notifyDataSetChanged();
                if (fragment.mReportedBattlesList.size() != BATTLES_PER_FETCH)
                {
                    fragment.mAllBattlesFetched = true;
                }
                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
            }
    }


    private void deleteBattle(final ReportedBattlesAdapter.MyViewHolder holder) {
        mProgressContainer.setVisibility(View.VISIBLE);
        DeleteBattleRequest request = new DeleteBattleRequest();
        request.setBattleID(mReportedBattlesList.get(holder.getAdapterPosition()).getBattleID());
        new DeleteBattlesTask(getActivity(), this, holder).execute(request);
    }

    private static class DeleteBattlesTask extends AsyncTask<DeleteBattleRequest, Void,  AsyncTaskResult<DeleteBattleResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<BattlesReportedFragment> fragmentReference;
        private ReportedBattlesAdapter.MyViewHolder holder;

        DeleteBattlesTask(Activity activity, BattlesReportedFragment fragment,  ReportedBattlesAdapter.MyViewHolder holder)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
            this.holder = holder;
        }

        @Override
        protected AsyncTaskResult<DeleteBattleResponse> doInBackground(DeleteBattleRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        final LambdaFunctionsInterface myInterface = factory.build(LambdaFunctionsInterface.class);

                try {
                    DeleteBattleResponse response = myInterface.DeleteBattleAdmin(params[0]);
                    return new AsyncTaskResult<>(response);
                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR", lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                } catch (AmazonServiceException ase) {
                    // invalid credentials, incorrect AWS signature, etc
                    Log.i("ERROR", ase.getErrorMessage());
                    return new AsyncTaskResult<>(ase);
                } catch (AmazonClientException ace) {
                    // Network issue
                    Log.i("ERROR", ace.toString());
                    return new AsyncTaskResult<>(ace);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<DeleteBattleResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                BattlesReportedFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;


                DeleteBattleResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {

                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;
                }


                    if (result.getAffectedRows() == 1)
                    {
                        //Comment Deleted
                        fragment.mReportedBattlesList.get(holder.getAdapterPosition()).setBattleDeleted(true);
                        fragment.mAdapter.notifyItemChanged(holder.getAdapterPosition());

                    }
                    else
                    {
                        Toast.makeText(activity, R.string.not_authorised_delete_battle, Toast.LENGTH_LONG).show();
                    }

                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
                }


    }


    private void ignoreBattle(final int position) {
        mProgressContainer.setVisibility(View.VISIBLE);
        IgnoreBattleRequest request = new IgnoreBattleRequest();
        request.setBattleID(mReportedBattlesList.get(position).getBattleID());
        new IgnoreBattlesTask(getActivity(), this, position).execute(request);

    }

    private static class IgnoreBattlesTask extends AsyncTask<IgnoreBattleRequest, Void,  AsyncTaskResult<IgnoreBattleResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<BattlesReportedFragment> fragmentReference;
        private int position;

        IgnoreBattlesTask(Activity activity, BattlesReportedFragment fragment, int position)
        {
            this.position = position;
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }
        @Override
        protected AsyncTaskResult<IgnoreBattleResponse> doInBackground(IgnoreBattleRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        final LambdaFunctionsInterface myInterface = factory.build(LambdaFunctionsInterface.class);


                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    IgnoreBattleResponse response = myInterface.IgnoreBattleAdmin(params[0]);
                    return new AsyncTaskResult<>(response);
                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR", lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                } catch (AmazonServiceException ase) {
                    // invalid credentials, incorrect AWS signature, etc
                    Log.i("ERROR", ase.getErrorMessage());
                    return new AsyncTaskResult<>(ase);
                } catch (AmazonClientException ace) {
                    // Network issue
                    Log.i("ERROR", ace.toString());
                    return new AsyncTaskResult<>(ace);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<IgnoreBattleResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                BattlesReportedFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                IgnoreBattleResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;

                }


                    if (result.getAffectedRows() == 1)
                    {
                        //Comment Ignored
                        fragment.mReportedBattlesList.get(position).setBattleIgnored(true);
                        fragment.mAdapter.notifyItemChanged(position);

                    }
                    else
                    {
                        Toast.makeText(activity, R.string.not_authorised_ignore_battle, Toast.LENGTH_LONG).show();
                    }




                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
            }

    }

    public enum ChallengerOrChallenged
    {
        CHALLENGED,
        CHALLENGER
    }

    private void banUser(final ChallengerOrChallenged challengerOrChallenged, final ReportedBattlesAdapter.MyViewHolder holder , int banLengthDays) {
        String cognitoIDBan;
        if (challengerOrChallenged == ChallengerOrChallenged.CHALLENGED)
        {
            cognitoIDBan= mReportedBattlesList.get(holder.getAdapterPosition()).getChallengedCognitoID();
        }
        else if (challengerOrChallenged == ChallengerOrChallenged.CHALLENGER)
        {
            cognitoIDBan = mReportedBattlesList.get(holder.getAdapterPosition()).getChallengerCognitoID();
        }
        else
        {
            throw new Error("Invalid Choice");

        }
        mProgressContainer.setVisibility(View.VISIBLE);

        BanUserRequest request = new BanUserRequest();
        request.setBattleIDReason(mReportedBattlesList.get(holder.getAdapterPosition()).getBattleID());
        request.setCognitoIDUser(cognitoIDBan);
        request.setBanLengthDays(banLengthDays);
        new BanUserTask(getActivity(), this, challengerOrChallenged, holder).execute(request);
    }

    private static class BanUserTask extends AsyncTask<BanUserRequest, Void, AsyncTaskResult<BanUserResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<BattlesReportedFragment> fragmentReference;
        private ChallengerOrChallenged challengerOrChallenged;
        private ReportedBattlesAdapter.MyViewHolder holder;

        BanUserTask(Activity activity, BattlesReportedFragment fragment, ChallengerOrChallenged challengerOrChallenged,ReportedBattlesAdapter.MyViewHolder holder)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
            this.holder = holder;
            this.challengerOrChallenged = challengerOrChallenged;
        }
        @Override
        protected AsyncTaskResult<BanUserResponse> doInBackground(BanUserRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));
        final LambdaFunctionsInterface myInterface = factory.build(LambdaFunctionsInterface.class);
                try {
                    BanUserResponse response = myInterface.BanUserAdmin(params[0]);
                    return new AsyncTaskResult<>(response);
                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR", lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                } catch (AmazonServiceException ase) {
                    // invalid credentials, incorrect AWS signature, etc
                    Log.i("ERROR", ase.getErrorMessage());
                    return new AsyncTaskResult<>(ase);
                } catch (AmazonClientException ace) {
                    // Network issue
                    Log.i("ERROR", ace.toString());
                    return new AsyncTaskResult<>(ace);
                }
            }

            @Override
            protected void onPostExecute( AsyncTaskResult<BanUserResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                BattlesReportedFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                BanUserResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;
                }



                    if (result.getAffectedRows() == 1)
                    {
                        //User Banned
                        if (challengerOrChallenged == ChallengerOrChallenged.CHALLENGER) {
                            fragment.mReportedBattlesList.get(holder.getAdapterPosition()).setChallengerBanned(true);
                        }
                        else if (challengerOrChallenged == ChallengerOrChallenged.CHALLENGED)
                        {
                            fragment.mReportedBattlesList.get(holder.getAdapterPosition()).setChallengedBanned(true);
                        }
                        fragment.mAdapter.notifyItemChanged(holder.getAdapterPosition());

                    }
                    else
                    {
                        Toast.makeText(activity, R.string.not_authorised_ignore_comment, Toast.LENGTH_LONG).show();
                    }




                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
            }

    }

    protected class ReportedBattlesAdapter extends RecyclerView.Adapter<ReportedBattlesAdapter.MyViewHolder> {

        private ArrayList<ReportedBattle>  battlesList;

        public class MyViewHolder extends RecyclerView.ViewHolder {


           ImageView thumbnailImageView;
           TextView battleNameTextView;
           TextView challengerUsernameTextView;
           TextView challengedUsernameTextView;
           TextView challengedNameTextView;
           TextView challengerNameTextView;
           Button deleteBattleButton;
           Button ignoreBattleReportButton;
           Button banChallengerButton;
           Button banChallengedButton;
           Button loadMoreBattlesButton;
           TextView challengerBannedTextView;
           TextView challengedBannedTextView;
           TextView battleDeletedTextView;
           TextView battleIgnoredTextView;
           EditText banDaysEditText;




            public MyViewHolder(View view) {
                super(view);
                battleNameTextView = view.findViewById(R.id.battleNameTextView);
                thumbnailImageView = view.findViewById(R.id.thumbnailImageView);
                challengerUsernameTextView = view.findViewById(R.id.challengerUsernameTextView);
                 challengedUsernameTextView = view.findViewById(R.id.challengedUsernameTextView);
                 challengedNameTextView = view.findViewById(R.id.challengedNameTextView);
                 challengerNameTextView = view.findViewById(R.id.challengerNameTextView);
                 deleteBattleButton = view.findViewById(R.id.deleteButton);
                 ignoreBattleReportButton = view.findViewById(R.id.ignoreButton);
                 banChallengerButton = view.findViewById(R.id.banChallengerButton);
                 banChallengedButton = view.findViewById(R.id.banChallengedButton);
                 loadMoreBattlesButton = view.findViewById(R.id.loadMoreButton);
                 challengerBannedTextView = view.findViewById(R.id.challengerBannedTextView);
                 challengedBannedTextView = view.findViewById(R.id.challengedBannedTextView);
                 battleDeletedTextView = view.findViewById(R.id.battleDeletedTextView);
                 battleIgnoredTextView = view.findViewById(R.id.battleIgnoredTextView);
                banDaysEditText = view.findViewById(R.id.banDaysEditText);

            }



        }




        public ReportedBattlesAdapter(ArrayList<ReportedBattle> battlesList) {
            this.battlesList = battlesList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_reported_battle, parent, false);


            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

                final ReportedBattle b = battlesList.get(position);
                holder.battleNameTextView.setText(b.getBattleName());
                holder.challengerUsernameTextView.setText(b.getChallengerUsername());
                holder.challengedUsernameTextView.setText(b.getChallengedUsername());
                holder.challengedNameTextView.setText(b.getChallengedName());
                holder.challengerNameTextView.setText(b.getChallengerName());
                if (battlesList.get(position).isChallengerBanned()){holder.challengerBannedTextView.setVisibility(View.VISIBLE);}
                if (battlesList.get(position).isChallengedBanned()){holder.challengedBannedTextView.setVisibility(View.VISIBLE);}
                if (battlesList.get(position).isBattleDeleted()){holder.battleDeletedTextView.setVisibility(View.VISIBLE);}
                if (battlesList.get(position).isBattleIgnored()){holder.battleIgnoredTextView.setVisibility(View.VISIBLE);}
                Picasso.get().load(b.getThumbnailServerUrl()).placeholder(R.drawable.placeholder1440x750).error(R.drawable.placeholder1440x750).into(holder.thumbnailImageView);

                holder.thumbnailImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String filepath = b.getServerFinalVideoUrl(b.getChallengerCognitoID());
                        mProgressContainer.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(getActivity(), FullBattleVideoPlayerActivity.class);
                        intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_BATTLEID, b.getBattleID());
                        intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_FILE_VIDEO_PATH, filepath);
                        intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGER_USERNAME, b.getChallengerUsername());
                        intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGED_USERNAME, b.getChallengedUsername());
                        startActivity(intent);
                    }
                });


                holder.ignoreBattleReportButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //ignore battle
                        ignoreBattle( position);

                    }
                });
                holder.banChallengerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.banDaysEditText.getText().toString().equals(""))
                        {
                            Toast.makeText(getActivity(), R.string.no_ban_days_entered_toast, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            int banDaysAmount = Integer.parseInt(holder.banDaysEditText.getText().toString());
                            if (banDaysAmount <= 0) {
                                Toast.makeText(getActivity(), R.string.ban_days_greater_0_toast, Toast.LENGTH_SHORT).show();
                            } else {
                                banUser(ChallengerOrChallenged.CHALLENGER, holder, banDaysAmount);
                            }
                        }
                        //c.user_reported_cognito_id
                    }
                });
                holder.banChallengedButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int banDaysAmount = Integer.parseInt(holder.banDaysEditText.getText().toString());
                        if (banDaysAmount <=0)
                        {
                            Toast.makeText(getActivity(), R.string.ban_days_greater_0_toast, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            banUser(ChallengerOrChallenged.CHALLENGED, holder, banDaysAmount);
                        }
                    }
                });
                holder.deleteBattleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteBattle(holder);
                    }
                });
                if ((position == loadAmount - 1) && !mAllBattlesFetched) {
                    holder.loadMoreBattlesButton.setVisibility(View.VISIBLE);
                    holder.loadMoreBattlesButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getBattles();
                        }
                    });
                }




        }
        @Override
        public int getItemCount() {
            return battlesList.size();
        }



    }
}
