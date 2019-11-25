package com.liamfarrell.android.snapbattle;

import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BanUserRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.DeleteCommentRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.ReportedCommentsRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BanUserResponse;
import com.liamfarrell.android.snapbattle.model.ReportedComment;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.IgnoreCommentRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DeleteCommentResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.IgnoreCommentResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportedCommentsResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by liamf on 21/03/2018.
 */

public class CommentsReportedFragment extends Fragment {
    public static final int COMMENTS_PER_FETCH = 50;
    private int loadAmount = COMMENTS_PER_FETCH;
    private View mProgressContainer;
    private boolean mAllCommentsFetched;
    private RecyclerView mRecyclerView;
    private CommentsReportedAdapter mAdapter;
    private ArrayList<ReportedComment> mCommentsList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAllCommentsFetched = false;
        mCommentsList = new ArrayList<ReportedComment>();
        mAdapter = new CommentsReportedAdapter(mCommentsList);
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

        getComments();

        return v;
    }

    private void getComments() {
        mProgressContainer.setVisibility(View.VISIBLE);

        ReportedCommentsRequest request = new ReportedCommentsRequest();
        request.setFetchLimit(loadAmount);
        new GetCommentsTask(getActivity(), this).execute(request);

    }

    private static class GetCommentsTask extends AsyncTask<ReportedCommentsRequest, Void, AsyncTaskResult<ReportedCommentsResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<CommentsReportedFragment> fragmentReference;

        GetCommentsTask(Activity activity, CommentsReportedFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected  AsyncTaskResult<ReportedCommentsResponse> doInBackground(ReportedCommentsRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        final LambdaFunctionsInterface myInterface = factory.build(LambdaFunctionsInterface.class,   new CustomLambdaDataBinder());

                try {
                    ReportedCommentsResponse response = myInterface.GetReportedComments(params[0]);
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
            protected void onPostExecute( AsyncTaskResult<ReportedCommentsResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                CommentsReportedFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                ReportedCommentsResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;
                }
                fragment.mCommentsList.clear();
                fragment.mCommentsList.addAll(result.getSqlResult());
                fragment.mAdapter.notifyDataSetChanged();
                if (fragment.mCommentsList.size() != COMMENTS_PER_FETCH)
                {
                    fragment.mAllCommentsFetched = true;
                }
                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
            }
    }




    private void deleteComment(final CommentsReportedAdapter.MyViewHolder holder) {
        mProgressContainer.setVisibility(View.VISIBLE);
       DeleteCommentRequest commentRequest = new DeleteCommentRequest();
        commentRequest.setCommentID(mCommentsList.get(holder.getAdapterPosition()).getCommentId());
        new DeleteCommentTask(getActivity(), this, holder).execute(commentRequest);
    }

    private static class DeleteCommentTask extends AsyncTask<DeleteCommentRequest, Void, AsyncTaskResult<DeleteCommentResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<CommentsReportedFragment> fragmentReference;
        private CommentsReportedAdapter.MyViewHolder holder;

        DeleteCommentTask(Activity activity, CommentsReportedFragment fragment, CommentsReportedAdapter.MyViewHolder holder)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
            this.holder = holder;
        }
        @Override
        protected AsyncTaskResult<DeleteCommentResponse> doInBackground(DeleteCommentRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        final LambdaFunctionsInterface myInterface = factory.build(LambdaFunctionsInterface.class);



                try {
                    DeleteCommentResponse response = myInterface.DeleteCommentAdmin(params[0]);
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
            protected void onPostExecute(AsyncTaskResult<DeleteCommentResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                CommentsReportedFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;
                DeleteCommentResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;
                }

                    if (result.getAffectedRows() == 1)
                    {
                        //Comment Deleted
                        fragment.mCommentsList.get(holder.getAdapterPosition()).setCommentDeleted(true);
                        fragment.mAdapter.notifyItemChanged(holder.getAdapterPosition());

                    }
                    else
                    {
                        Toast.makeText(activity, R.string.not_authorised_delete_comment, Toast.LENGTH_LONG).show();
                    }




                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
            }


    }


    private void ignoreComment(final CommentsReportedAdapter.MyViewHolder holder) {
        mProgressContainer.setVisibility(View.VISIBLE);
        IgnoreCommentRequest commentRequest = new IgnoreCommentRequest();
        commentRequest.setCommentID(mCommentsList.get(holder.getAdapterPosition()).getCommentId());
        new IgnoreCommentTask(getActivity(),this, holder).execute(commentRequest);
    }

    private static class IgnoreCommentTask extends AsyncTask<IgnoreCommentRequest, Void,  AsyncTaskResult<IgnoreCommentResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<CommentsReportedFragment> fragmentReference;
        private  CommentsReportedAdapter.MyViewHolder holder;

        IgnoreCommentTask(Activity activity, CommentsReportedFragment fragment, CommentsReportedAdapter.MyViewHolder holder)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
            this.holder = holder;
        }
        @Override
        protected AsyncTaskResult<IgnoreCommentResponse> doInBackground(IgnoreCommentRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        final LambdaFunctionsInterface myInterface = factory.build(LambdaFunctionsInterface.class);

                try {
                    IgnoreCommentResponse response = myInterface.IgnoreCommentAdmin(params[0]);
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
            protected void onPostExecute(AsyncTaskResult<IgnoreCommentResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                CommentsReportedFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;
                IgnoreCommentResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;
                }



                    if (result.getAffectedRows() == 1)
                    {
                        //Comment Ignored
                        fragment.mCommentsList.get(holder.getAdapterPosition()).setCommentIgnored(true);


                        fragment.mAdapter.notifyItemChanged(holder.getAdapterPosition());

                    }
                    else
                    {
                        Toast.makeText(activity, R.string.not_authorised_ignore_comment, Toast.LENGTH_LONG).show();
                    }




                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
            }

    }

    private void banUser(final CommentsReportedAdapter.MyViewHolder holder, int banLengthDays) {
        mProgressContainer.setVisibility(View.VISIBLE);
        BanUserRequest request = new BanUserRequest();
        request.setCognitoIDUser(mCommentsList.get(holder.getAdapterPosition()).getCognitoIdCommenter());
        request.setCommentIDReason(mCommentsList.get(holder.getAdapterPosition()).getCommentId());
        request.setBanLengthDays(banLengthDays);
        new BanUserTask(getActivity(), this, holder).execute(request);

    }

    private static class BanUserTask extends   AsyncTask<BanUserRequest, Void, AsyncTaskResult<BanUserResponse>>
    {

        private WeakReference<Activity> activityReference;
        private WeakReference<CommentsReportedFragment> fragmentReference;
        private  CommentsReportedAdapter.MyViewHolder holder;

        BanUserTask(Activity activity, CommentsReportedFragment fragment,  CommentsReportedAdapter.MyViewHolder holder)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
            this.holder = holder;
        }
        @Override
        protected AsyncTaskResult<BanUserResponse> doInBackground(BanUserRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        final LambdaFunctionsInterface myInterface = factory.build(LambdaFunctionsInterface.class);



                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
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
                CommentsReportedFragment fragment = fragmentReference.get();
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
                        fragment.mCommentsList.get(holder.getAdapterPosition()).setUserIsBanned(true);

                        fragment.mAdapter.notifyItemChanged(holder.getAdapterPosition());

                    }
                    else
                    {
                        Toast.makeText(activity, R.string.not_authorised_ignore_comment, Toast.LENGTH_LONG).show();
                    }

                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
                }



    }

    protected class CommentsReportedAdapter extends RecyclerView.Adapter<CommentsReportedAdapter.MyViewHolder> {

        private ArrayList<ReportedComment>  commentsList;

        public class MyViewHolder extends RecyclerView.ViewHolder {


            TextView commentTextView;
            TextView nameOfUserTextView;
            TextView usernameTextView;
            Button deleteCommentButton;
            Button banUserButton;
            Button loadMoreButton;
            Button ignoreButton;
            TextView userBannedTextView;
            TextView commentDeletedTextView;
            TextView commentIgnoredTextView;
            EditText banDaysAmountEditText;



            public MyViewHolder(View view) {
                super(view);
                commentIgnoredTextView = view.findViewById(R.id.commentIgnoredTextView);
                userBannedTextView = view.findViewById(R.id.userBannedTextView);
                commentDeletedTextView = view.findViewById(R.id.commentDeletedTextView);
                commentTextView =view.findViewById(R.id.commentTextView);
                nameOfUserTextView=view.findViewById(R.id.nameTextView);
               usernameTextView=view.findViewById(R.id.usernameTextView);
                deleteCommentButton=view.findViewById(R.id.deleteButton);
                banUserButton=view.findViewById(R.id.banButton);
                loadMoreButton = view.findViewById(R.id.loadMoreButton);
                ignoreButton = view.findViewById(R.id.ignoreButton);
                banDaysAmountEditText= view.findViewById(R.id.banDaysEditText);

            }



        }




        public CommentsReportedAdapter(ArrayList<ReportedComment> commentsList) {
            this.commentsList = commentsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_reported_comment, parent, false);


            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            final ReportedComment c = commentsList.get(position);
            holder.commentTextView.setText(c.getComment());
            holder.nameOfUserTextView.setText(c.getUserReportedName());
            holder.usernameTextView.setText(c.getUsername());
           if (commentsList.get(position).isUserIsBanned()){holder.userBannedTextView.setVisibility(View.VISIBLE);}
            if (commentsList.get(position).isCommentDeleted()){holder.commentDeletedTextView.setVisibility(View.VISIBLE);}
            if (commentsList.get(position).isCommentIgnored()){holder.commentIgnoredTextView.setVisibility(View.VISIBLE);}


            holder.ignoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ignore comment
                    ignoreComment( holder);

                }
            });
            holder.banUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.banDaysAmountEditText.getText().toString().equals(""))
                    {
                        Toast.makeText(getActivity(), R.string.no_ban_days_entered_toast, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        int banDaysAmount = Integer.parseInt(holder.banDaysAmountEditText.getText().toString());
                        if (banDaysAmount <= 0) {
                            Toast.makeText(getActivity(), R.string.ban_days_greater_0_toast, Toast.LENGTH_SHORT).show();
                        } else {
                            banUser(holder, banDaysAmount);
                        }
                    }

                    //c.user_reported_cognito_id
                }
            });
            holder.deleteCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteComment(holder);
                }
            });
            if ((position == loadAmount - 1) && !mAllCommentsFetched) {
                        holder.loadMoreButton.setVisibility(View.VISIBLE);
                        holder.loadMoreButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                        getComments();
                    }
                });
            }




        }
        @Override
        public int getItemCount() {
            return commentsList.size();
        }



        }
}
