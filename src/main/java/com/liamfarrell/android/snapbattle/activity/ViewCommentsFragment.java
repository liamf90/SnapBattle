package com.liamfarrell.android.snapbattle.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.liamfarrell.android.snapbattle.model.Comment;
import com.liamfarrell.android.snapbattle.model.Video;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.caches.OtherUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.AddCommentRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.DeleteCommentRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.ReportCommentRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.AddCommentResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DeleteCommentResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetCommentsResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportCommentResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.VerifyUserResponse;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.GetCommentsRequest;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.VerifyUserRequest;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Liam on 14/01/2018.
 */

public class ViewCommentsFragment extends Fragment {
    private static final String TAG = "ViewCommentsFragment";
    private ArrayList<Comment> mComments;
    private int mBattleID;
    private EditText mCommentEditText;
    private ImageButton mAddCommentButton;
    private ProgressBar mAddCommentProgressBar;
    private CommentAdapter mCommentAdapter;
    private boolean mIsEditTextChangedByOnTextChanged = false;
    private boolean mOnTagAdd = false;
    CallbackManager mCallbackManager;

    //use CopyOnWriteArrayList to avoid concurrentmodificationexception
    private CopyOnWriteArrayList<String> mUsernameTagsList;
    private ConcurrentHashMap<String, Integer> mUsernameTagsListWithCount;

    private CharSequence beforeTextChanged;

    //FUTURE USE;
    //public final int COMMENTS_PER_FETCH = 15;
    private View mProgressContainer;
    public static final String EXTRA_BATTLEID = "com.liamfarrell.android.snapbattle.viewcommentsfragment.battleIDextra";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBattleID = getActivity().getIntent().getIntExtra(FullBattleVideoPlayerActivity.EXTRA_BATTLEID, 0);
        if (mBattleID == 0)
        {
            mBattleID = getActivity().getIntent().getIntExtra(EXTRA_BATTLEID, 0);
        }
        mComments = new ArrayList<>();
        mCommentAdapter = new CommentAdapter(mComments);
        mUsernameTagsList = new CopyOnWriteArrayList<>();
        mUsernameTagsListWithCount = new ConcurrentHashMap<>();
        setRetainInstance(true);
        getComments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_view_comments, container, false);
        mProgressContainer = v.findViewById(R.id.progressContainer);
        mProgressContainer.setVisibility(View.VISIBLE);
        mCommentEditText =  v.findViewById(R.id.commentEditText);
        mCommentEditText.setText("");
        mCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!mIsEditTextChangedByOnTextChanged) {

                    //Check old tags have not been interrupted

                    SpannableStringBuilder strBuilderAfter = new SpannableStringBuilder(s);
                    Object[] spansAfter = strBuilderAfter.getSpans(0, s.length() - 1, Object.class);

                    int currentSelection = mCommentEditText.getSelectionStart();

                    //check the username tags all still match
                    boolean allUsernamesMatched = true;
                    for (String username : mUsernameTagsList) {
                        boolean usernameMatched = false;
                        for (Object aSpansAfter : spansAfter) {
                            //check new tag still matches old tag
                            int start2 = strBuilderAfter.getSpanStart(aSpansAfter);
                            int end2 = strBuilderAfter.getSpanEnd(aSpansAfter);
                            String tag2 = strBuilderAfter.toString().substring(start2, end2);
                            Log.i(TAG, "Start: " + start2 + ", end2: " + end2 + ", tag: " + tag2);
                            String usernameSpan = tag2.substring(1, tag2.length());
                            Log.i(TAG, "Username: " + username);
                            Log.i(TAG, "Username span: " + usernameSpan);
                            if (username.equals(usernameSpan)) {
                                usernameMatched = true;
                                break;
                            }
                        }

                        Log.i(TAG, "Username " + username + "matched? : " + usernameMatched);

                        if (!usernameMatched) {
                            //remove tag from the username tag list and remove span
                            allUsernamesMatched = false;
                            mUsernameTagsList.remove(username);
                        }
                    }

                    if (!allUsernamesMatched) {
                        //update spans

                        SpannableStringBuilder newStringBuilder = new SpannableStringBuilder(s);
                        newStringBuilder.clearSpans();
                        for (Object aSpansAfter : spansAfter) {
                            int start3 = strBuilderAfter.getSpanStart(aSpansAfter);
                            int end3 = strBuilderAfter.getSpanEnd(aSpansAfter);
                            String tag3 = strBuilderAfter.toString().substring(start3, end3);
                            String username3 = tag3.substring(1, tag3.length());

                            Log.i(TAG, "If username tags list contains: " + username3 );
                            if (mUsernameTagsList.contains(username3)) {
                                Log.i(TAG, "true");
                                newStringBuilder.setSpan(new ForegroundColorSpan(0xFFCC5500), start3, end3, strBuilderAfter.getSpanFlags(aSpansAfter));
                            }
                        }
                        mIsEditTextChangedByOnTextChanged = true;
                        mCommentEditText.setText(newStringBuilder, TextView.BufferType.SPANNABLE);
                        mCommentEditText.setSelection(currentSelection);
                        mIsEditTextChangedByOnTextChanged = false;
                    }

                }


            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mAddCommentProgressBar = v.findViewById(R.id.addCommentProgressBar);
        RecyclerView recyclerView = v.findViewById(R.id.commentsList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mCommentAdapter);
        mAddCommentButton =  v.findViewById(R.id.addACommentButton);
        mAddCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment();
            }
        });
        return v;

    }
    private boolean doesUserHaveUserFriendsPermission(){
        Set<String> declinedPermissions = AccessToken.getCurrentAccessToken().getDeclinedPermissions();
        return !declinedPermissions.contains("user_friends");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        Log.d("CreateBattleActivity", "Activity result");
    }


    private void requestUserFriendsPermission(){
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        verifyUser();
                    }

                    @Override
                    public void onCancel() {

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


    private void addComment() {

        mAddCommentButton.setVisibility(View.GONE);
        mAddCommentProgressBar.setVisibility(View.VISIBLE);
        AddCommentRequest comment = new AddCommentRequest();
        comment.setBattleID(mBattleID);
        comment.setComment(mCommentEditText.getText().toString());
        comment.setUsernamesToTag(mUsernameTagsList);
        new AddCommentTask(getActivity(),this).execute(comment);
    }

    private static class AddCommentTask extends AsyncTask<AddCommentRequest, Void, AsyncTaskResult<AddCommentResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ViewCommentsFragment> fragmentReference;

        AddCommentTask(Activity activity, ViewCommentsFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<AddCommentResponse> doInBackground(AddCommentRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class, new CustomLambdaDataBinder());
                try {
                    AddCommentResponse response =  lambdaFunctionsInterface.AddComment(params[0]);
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
            protected void onPostExecute(AsyncTaskResult<AddCommentResponse> asyncResult) {
                // get a reference to the activity and fragment if it is still there
                ViewCommentsFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                fragment.mAddCommentButton.setVisibility(View.VISIBLE);
                fragment.mAddCommentProgressBar.setVisibility(View.GONE);

                AddCommentResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;
                }



                 if (result.getError() != null && result.getError().equals(AddCommentResponse.getUserNotMinimumFriendsError()))
                {
                    //User not verified with enough facebook friends to post comments.. Verify User
                    //first check if user has user_friends permission allowed
                    if (fragment.doesUserHaveUserFriendsPermission()){
                        fragment.verifyUser();
                    }else {
                        Toast.makeText(activity, R.string.need_accept_permission_user_friends, Toast.LENGTH_SHORT).show();
                        fragment.requestUserFriendsPermission();}

                    Log.i(TAG, "User Not Verified");


                }
                else if (result.getError() != null && result.getError().equals(AddCommentResponse.getUserBannedError()))
                {
                    //User is banned.
                    Date timeBanEnds = new Date();
                    //Suppress Lint asking to get local date format because we want UTC date format
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    if (result.getTimeBanEnds() != null)
                    {
                        try {
                            timeBanEnds = sdf.parse(result.getTimeBanEnds());
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(activity, activity.getResources().getString(R.string.banned_toast,timeBanEnds.toString()), Toast.LENGTH_SHORT).show();
                }
                else {
                    fragment.mComments.add(result.getSqlResult().get(0));
                     fragment.mCommentEditText.setText("");
                     fragment.mCommentAdapter.notifyDataSetChanged();
                     fragment.hideKeyboard();
                }
                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
            }
    }


        private void getComments()
        {
            GetCommentsRequest commentRequest = new GetCommentsRequest();
            int lastCommentId = -1;
            if (mComments.size() > 0) {
                lastCommentId = mComments.get(mComments.size() - 1).getCommentId();
            }
            commentRequest.setBattleID(mBattleID);
            commentRequest.setLastCommentID(lastCommentId);
            new GetCommentsTask(getActivity(), this).execute(commentRequest);

        }

        private static class GetCommentsTask extends AsyncTask<GetCommentsRequest, Void,  AsyncTaskResult<GetCommentsResponse>>
        {
            private WeakReference<Activity> activityReference;
            private WeakReference<ViewCommentsFragment> fragmentReference;

            GetCommentsTask(Activity activity, ViewCommentsFragment fragment)
            {
                fragmentReference = new WeakReference<>(fragment);
                activityReference = new WeakReference<>(activity);
            }

            @Override
            protected AsyncTaskResult<GetCommentsResponse> doInBackground(GetCommentsRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class, new CustomLambdaDataBinder());


                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    GetCommentsResponse response = lambdaFunctionsInterface.GetComments(params[0]);
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
            protected void onPostExecute(AsyncTaskResult<GetCommentsResponse> asyncResult) {
                // get a reference to the activity and fragment if it is still there
                ViewCommentsFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                GetCommentsResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;
                }


                if (result == null) {
                    Toast.makeText(activity, R.string.no_comments, Toast.LENGTH_LONG).show();
                    fragment.mProgressContainer.setVisibility(View.INVISIBLE);
                    return;

                }

                /*
                if (result.getSql_result().size() != fragment.COMMENTS_PER_FETCH) {
                    //TODO: load comments in batches once app gets more popular
                    //allCommentsLoaded = true;
                }
                */
                Log.i(TAG, result.toString());

                if (result.getBattle_deleted() == 1)
                {
                    Toast.makeText(activity, R.string.battle_removed_error_toast, Toast.LENGTH_LONG).show();
                    fragment.mProgressContainer.setVisibility(View.INVISIBLE);
                    activity.finish();
                }

                int startInsert = fragment.mComments.size() - 1;
                fragment.mComments.addAll(result.getSql_result());
                fragment.mCommentAdapter.notifyItemRangeChanged(startInsert, result.getSql_result().size());



                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
            }
    }


    private void reportComment(int commentID) {
        ReportCommentRequest reportCommentRequest = new ReportCommentRequest();
        reportCommentRequest.setCommentID(commentID);
        new ReportCommentTask(getActivity(), this).execute(reportCommentRequest);
    }

    private static class ReportCommentTask extends  AsyncTask<ReportCommentRequest, Void, AsyncTaskResult<ReportCommentResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ViewCommentsFragment> fragmentReference;

        ReportCommentTask(Activity activity, ViewCommentsFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);

        }

        @Override
        protected AsyncTaskResult<ReportCommentResponse> doInBackground(ReportCommentRequest... params)
        {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));


        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
                try {
                    ReportCommentResponse response = lambdaFunctionsInterface.ReportComment(params[0]);
                    return new AsyncTaskResult<>(response);
                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR", Arrays.toString(lfe.getStackTrace()));
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
            protected void onPostExecute(AsyncTaskResult<ReportCommentResponse> asyncResult) {
                // get a reference to the activity and fragment if it is still there
                ViewCommentsFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                ReportCommentResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;

                }
                    if (result.getAffectedRows() == 1)
                    {
                        Toast.makeText(activity, R.string.comment_reported_toast, Toast.LENGTH_LONG).show();

                    }
                    else
                    {
                        Toast.makeText(activity, R.string.comment_already_reported_toast, Toast.LENGTH_LONG).show();
                    }
                fragment.mProgressContainer.setVisibility(View.GONE);
            }
    }



    private void deleteComment(int commentID, final int position) {
        DeleteCommentRequest commentRequest = new DeleteCommentRequest();
        commentRequest.setCommentID(commentID);
        new DeleteCommentTask(getActivity(), this, position).execute(commentRequest);
    }

    private static class DeleteCommentTask extends  AsyncTask<DeleteCommentRequest, Void, AsyncTaskResult<DeleteCommentResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ViewCommentsFragment> fragmentReference;
        private int position;

        DeleteCommentTask(Activity activity, ViewCommentsFragment fragment, int position)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
            this.position = position;
        }

        @Override
        protected AsyncTaskResult<DeleteCommentResponse> doInBackground(DeleteCommentRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));


            final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
                try {
                    DeleteCommentResponse response =  lambdaFunctionsInterface.DeleteComment(params[0]);
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
                // get a reference to the activity and fragment if it is still there
                ViewCommentsFragment fragment = fragmentReference.get();
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
                       fragment.mComments.remove(position);
                       fragment.mCommentAdapter.notifyItemRemoved(position);
                   }
                   else
                   {
                       Toast.makeText(activity, R.string.not_authorised_delete_comment_toast, Toast.LENGTH_LONG).show();
                   }

                fragment.mProgressContainer.setVisibility(View.GONE);
            }

    }



    private void verifyUser() {
        VerifyUserRequest request = new VerifyUserRequest();
        request.setAccessToken(AccessToken.getCurrentAccessToken().getToken());
        new VerifyUserTask(getActivity(), this).execute(request);
    }

    private static class VerifyUserTask extends AsyncTask<VerifyUserRequest, Void, AsyncTaskResult<VerifyUserResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ViewCommentsFragment> fragmentReference;

        VerifyUserTask(Activity activity, ViewCommentsFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<VerifyUserResponse> doInBackground(VerifyUserRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);

                try {
                    VerifyUserResponse response =  lambdaFunctionsInterface.VerifyUser(params[0]);
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
            protected void onPostExecute(AsyncTaskResult<VerifyUserResponse> asyncResult) {
                // get a reference to the activity and fragment if it is still there
                ViewCommentsFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                VerifyUserResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;
                }

                    if (result.getResult().equals(VerifyUserResponse.USER_VERIFIED_RESULT)) {
                        Log.i(TAG, "User is verified");
                        fragment.addComment();
                    }
                    else if (result.getResult().equals(VerifyUserResponse.USER_NOT_VERIFIED_RESULT))
                    {
                        Toast.makeText(activity, R.string.not_enough_facebook_friends_toast, Toast.LENGTH_LONG).show();
                    }
            }


    }


    private void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }



    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {

        private List<Comment> commentsList;


        public class MyViewHolder extends RecyclerView.ViewHolder {

            public TextView commentTextView;
            public TextView timeSinceTextView;
            public CircleImageView profilePic;
            public View viewHolder;

            public MyViewHolder(View view) {
                super(view);
                viewHolder = view;
                commentTextView = view.findViewById(R.id.commentTextView);
                timeSinceTextView = view.findViewById(R.id.timeSinceTextView);
                profilePic = view.findViewById(R.id.profilePicSmall);
            }

        }

        public CommentAdapter(List<Comment> commentsList) {
            this.commentsList = commentsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_comment, parent, false);


            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            Picasso.get().cancelRequest(holder.profilePic);
            holder.profilePic.setImageResource(R.drawable.default_profile_pic100x100);
            holder.viewHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add username tag to edittext
                    if (!mUsernameTagsList.contains((commentsList.get(position).getUsername())))
                    {
                        mUsernameTagsList.add(commentsList.get(position).getUsername());


                        SpannableStringBuilder oldStringBuilder = new SpannableStringBuilder(mCommentEditText.getText());
                        String tag = "@" + commentsList.get(position).getUsername();

                        if (!(mCommentEditText.getText().toString().length() == 0 || mCommentEditText.getText().toString().charAt(mCommentEditText.getText().length() - 1) == ' ')) {
                            oldStringBuilder.append(" ");
                        }

                        int start = oldStringBuilder.length();
                        oldStringBuilder.append(tag);
                        oldStringBuilder.setSpan(new ForegroundColorSpan(0xFFCC5500), start, start  + tag.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


                        mIsEditTextChangedByOnTextChanged = true;
                        mCommentEditText.setText(oldStringBuilder, TextView.BufferType.SPANNABLE);
                        mCommentEditText.setSelection(mCommentEditText.getText().length());
                        mIsEditTextChangedByOnTextChanged = false;
                    }
                }
            });


            if (commentsList.get(position).getCognitoIdCommenter().equals(FacebookLoginFragment.getCredentialsProvider(getActivity()).getCachedIdentityId()))
            {

                holder.viewHolder.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        //Creating the instance of PopupMenu
                        PopupMenu popup = new PopupMenu(getActivity(), view);
                        //Inflating the Popup using xml file
                        popup.getMenuInflater().inflate(R.menu.comment_delete_popup_menu, popup.getMenu());

                        //registering popup with OnMenuItemClickListener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                deleteComment(commentsList.get(position).getCommentId(), position);
                                return true;
                            }
                        });

                        popup.show();//showing popup menu


                        return true;
                    }});//closing the setOnClickListener method
            }
            else
            {
                holder.viewHolder.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        //Creating the instance of PopupMenu
                        PopupMenu popup = new PopupMenu(getActivity(), view);
                        //Inflating the Popup using xml file
                        popup.getMenuInflater().inflate(R.menu.comment_report_popup_menu, popup.getMenu());

                        //registering popup with OnMenuItemClickListener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                reportComment(commentsList.get(position).getCommentId());
                                return true;
                            }
                        });

                        popup.show();//showing popup menu

                        return true;
                    }});//closing the setOnClickListener method
            }

            String username = commentsList.get(position).getUsername();
            SpannableStringBuilder usernameAndComment = new SpannableStringBuilder();
            usernameAndComment.append(username);
            int start = 0;
            usernameAndComment.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, usernameAndComment.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            usernameAndComment.append(" ");


            if (commentsList.get(position).isDeleted())
            {
                usernameAndComment.append(" ");
                int start2 = usernameAndComment.length();
                usernameAndComment.append(getResources().getString(R.string.comment_deleted));
                usernameAndComment.setSpan(new StyleSpan(Typeface.ITALIC), start2, usernameAndComment.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else
            {
                usernameAndComment.append(" ");
                usernameAndComment.append(commentsList.get(position).getComment());
            }
            holder.commentTextView.setText(usernameAndComment);
            holder.timeSinceTextView.setText(Video.getTimeSinceShorthand(commentsList.get(position).getTime()));


            if ( commentsList.get(position).getCommenterProfilePicCount()!= 0) {
                Picasso.get().load(R.drawable.default_profile_pic100x100).into(holder.profilePic);

                OtherUsersProfilePicCacheManager profilePicCache = OtherUsersProfilePicCacheManager.getProfilePicCache(getActivity());
                profilePicCache.getSignedUrlProfilePicOpponent(commentsList.get(position).getCognitoIdCommenter(), commentsList.get(position).getCommenterProfilePicCount(), commentsList.get(position).getCommenterProfilePicSmallSignedUrl(), getActivity()
                        , new OtherUsersProfilePicCacheManager.SignedUrlCallback() {
                            @Override
                            public void onSignedPicReceived(String signedUrl) {
                                Picasso.get().load(signedUrl).placeholder(R.drawable.default_profile_pic100x100).error(R.drawable.default_profile_pic100x100).into(holder.profilePic);
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