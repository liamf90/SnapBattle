package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.SignedUrlsRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetSignedUrlsResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.caches.OtherUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetNewSignedUrlResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.liamfarrell.android.snapbattle.views.LinearDividerItemDecoration;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.notifications.Notification;
import com.liamfarrell.android.snapbattle.caches.NotificationCache;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationListFragment extends Fragment
{
    private static final String TAG = "NotificationFragment";
    private LinkedList<Notification> mNotifications;
    private ArrayList<String> mCognitoIDsProfilePicUrlUpdatedList;
    private boolean mIsNoMoreNotifications;
    private NotificationCache mSNotificationCache;
    private NotificationAdapterRecycler mAdapter;
    private OtherUsersProfilePicCacheManager sProfilePicCache;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mIsNoMoreNotifications = false;
        mNotifications = new LinkedList<>();
        mCognitoIDsProfilePicUrlUpdatedList = new ArrayList<>();
        mAdapter = new NotificationAdapterRecycler(mNotifications);
        setRetainInstance(true);
        mSNotificationCache = NotificationCache.getNotificationCache();
        sProfilePicCache = OtherUsersProfilePicCacheManager.getProfilePicCache(getActivity());

        mSNotificationCache.setGCMUpdateCallback(new NotificationCache.GCMUpdatesCallback() {
            @Override
            public void onUpdates(boolean hasAllNotificationsBeenSeen) {
                mNotifications.clear();
                mNotifications.addAll(mSNotificationCache.getNotificationList());
            }


        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_notification_list, parent, false);

        RecyclerView recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());;
        recyclerView.setLayoutManager(mLayoutManager);
        Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider_thin);
        RecyclerView.ItemDecoration dividerItemDecoration = new LinearDividerItemDecoration(dividerDrawable);
        recyclerView.addItemDecoration(dividerItemDecoration);

        //load the notifications
        loadNotifications();
        return v;
    }

    private void loadNotifications()
    {
        //get notifications from notification cache and check for updates from server
        mSNotificationCache.LoadListFromFile(getActivity(),
                new NotificationCache.LoadNotificationsCallback() {
                    @Override
                    public void onNoUpdates(boolean hasAllNotificationsBeenSeen) {
                        if (NotificationListFragment.this.isVisible())
                        {
                            mSNotificationCache.updateDynamoSeenAllNotifications(getActivity());

                        }
                    }

                    @Override
                    public void onLoaded(final LinkedList<Notification> notificationList, boolean hasAllNotificationsBeenSeen) {

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mNotifications.addAll(notificationList);
                                    mAdapter.notifyDataSetChanged();

                                    if (NotificationListFragment.this.isVisible()) {
                                        mSNotificationCache.updateDynamoSeenAllNotifications(getActivity());

                                    }
                                    if (notificationList.size() > 0) {
                                        getSignedUrlsNotificationProfilePics(notificationList);
                                    }


                                }
                            });
                        }
                    }

                    @Override
                    public void onUpdates(LinkedList<Notification> notificationUpdatesForTop, boolean hasAllNotificationsBeenSeen) {
                        mNotifications.addAll(0, notificationUpdatesForTop);
                        mAdapter.notifyItemRangeInserted(0, notificationUpdatesForTop.size());
                        if (NotificationListFragment.this.isVisible())
                        {
                            mSNotificationCache.updateDynamoSeenAllNotifications(getActivity());

                        }
                        if (notificationUpdatesForTop.size() > 0) {
                            getSignedUrlsNotificationProfilePics(notificationUpdatesForTop);
                        }

                    }
                });

    }




    private void getSignedUrlsNotificationProfilePics(final List<Notification> notificationList) {
        //this method gets signed urls for the profile pics

        //get an unique list of the cognito ids by adding the list to a set
        Set<String> cognitoIDOfUsersSet = new HashSet<String>();
        for (Notification n : notificationList)
        {
            cognitoIDOfUsersSet.add(n.getOpponentCognitoId());
        }
        cognitoIDOfUsersSet.toArray();

        //only update profile pic signed urls of cognito ids that havnt been updated yet by this fragment.
        ArrayList<String> cognitoIDList = new ArrayList<String>(cognitoIDOfUsersSet);
        cognitoIDList.removeAll(mCognitoIDsProfilePicUrlUpdatedList);

        if (cognitoIDList.size() > 0) {
            Log.i(TAG, "Getting urls for cognito: " + cognitoIDList.toString());
            SignedUrlsRequest request = new SignedUrlsRequest();
            request.setCognitoIdToGetSignedUrlList(new ArrayList<String>(cognitoIDList));
            new GetSignedUrlsNotificationProfilePicsTask(getActivity(), this).execute(request);
        }
    }

    private static class GetSignedUrlsNotificationProfilePicsTask extends AsyncTask<SignedUrlsRequest, Void, AsyncTaskResult<GetSignedUrlsResponse>> {
        private WeakReference<Activity> activityReference;
        private WeakReference<NotificationListFragment> fragmentReference;

        GetSignedUrlsNotificationProfilePicsTask(Activity activity, NotificationListFragment fragment)
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
                IdentityManager.getDefaultIdentityManager().getCredentialsProvider());

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);



        // The Lambda function invocation results in a network call
        // Make sure it is not called from the main thread


                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    GetSignedUrlsResponse response = lambdaFunctionsInterface.GetProfilePicSignedUrls(params[0]);
                    return new AsyncTaskResult<>(response);

                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR", lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return null;
                } catch (AmazonServiceException ase) {
                    // invalid credentials, incorrect AWS signature, etc
                    Log.i("ERROR", ase.getErrorMessage());
                    return null;
                } catch (AmazonClientException ace) {
                    // Network issue
                    Log.i("ERROR", ace.toString());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<GetSignedUrlsResponse> asyncResult) {
                //get new signed urls for cached following users if there was an error on setting the profile pic or the profile pic has changed
                // get a reference to the callbacks and fragment if it is still there
                NotificationListFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;


                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
                    return;
                }

                //update the notifications with new signed urls for the profile pics of the respective opponents.
                if (asyncResult.getResult() != null)
                {
                    GetSignedUrlsResponse result = asyncResult.getResult();

                    for (GetNewSignedUrlResponse newSignedUrl : result.getNewSignedUrls()) {
                        if (!fragment.mCognitoIDsProfilePicUrlUpdatedList.contains(newSignedUrl.getNewSignedUrl()))
                        {
                            fragment.mCognitoIDsProfilePicUrlUpdatedList.add(newSignedUrl.getCognitoId());
                        }

                        for (int i = 0; i < fragment.mNotifications.size(); i++) {
                            Notification n = fragment.mNotifications.get(i);
                            Log.i("Notification", n.getMessage(activityReference.get()).toString() + ", signed url: " + newSignedUrl.getNewSignedUrl());
                            if (    n.getOpponentCognitoId().equals(newSignedUrl.getCognitoId()) &&
                                    (n.getSignedUrlProfilePicOpponent() == null || newSignedUrl.getProfilePicCount() > n.getOpponentProfilePicCount()))
                                    {
                                        if (newSignedUrl.getProfilePicCount() > 0) {

                                            fragment.mNotifications.get(i).setSignedUrlProfilePicOpponent(newSignedUrl.getNewSignedUrl());
                                            fragment.mNotifications.get(i).setOpponentProfilePicCount(newSignedUrl.getProfilePicCount());
                                            fragment.sProfilePicCache.updateSignedUrlProfilePicOpponent(newSignedUrl.getCognitoId(), newSignedUrl.getProfilePicCount(),
                                                    newSignedUrl.getNewSignedUrl(), activity.getApplicationContext());
                                            fragment.mAdapter.notifyItemChanged(i);
                                        }
                                        else
                                        {
                                            fragment.mNotifications.get(i).setOpponentProfilePicCount(newSignedUrl.getProfilePicCount());
                                        }
                            }
                        }
                    }

                }
            }
    }



    private class NotificationAdapterRecycler extends RecyclerView.Adapter<NotificationAdapterRecycler.MyViewHolder> {

        private List<Notification> notificationsList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            View listItemView;
            TextView messageTextView;
            CircleImageView profiePic;

            public MyViewHolder(View view) {
                super(view);
                listItemView = view;
                messageTextView = view.findViewById(R.id.notificationMessageTextView);
                profiePic = view.findViewById(R.id.profileImageView);

            }
        }


        public NotificationAdapterRecycler(List<Notification> notificationsList) {
            this.notificationsList = notificationsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate( R.layout.list_item_notification, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final Notification n = notificationsList.get(position);
            holder.listItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //startActivity(n.getIntent(getActivity()));
                }
            });
            holder.messageTextView.setText(n.getMessage(getContext()));
            //Cancel the previous set profile pic request, if it exists
            Picasso.get().cancelRequest(holder.profiePic);
            holder.profiePic.setImageResource(R.drawable.default_profile_pic100x100);

            //get signed url for users profile pic.



            if (n.getOpponentCognitoId() != null) {
                String signedUrlProfilePicOpponent = sProfilePicCache.getSignedUrlProfilePicOpponent(n.getOpponentCognitoId());
                Integer profilePicCount = sProfilePicCache.getProfilePicCount(n.getOpponentCognitoId());
                if (profilePicCount != null)
                {
                    n.setOpponentProfilePicCount(profilePicCount);
                }

                n.setSignedUrlProfilePicOpponent(signedUrlProfilePicOpponent);
                //if signedUrlProfilePicOpponent is null then it is not in the cache.

                if (signedUrlProfilePicOpponent != null && n.getOpponentProfilePicCount() != 0)
                {
                    //load cached signed url into profile pic imageview. if error -> set signed url to null.
                    Picasso.get().load(signedUrlProfilePicOpponent).placeholder(R.drawable.default_profile_pic100x100).error(R.drawable.default_profile_pic100x100).into(holder.profiePic, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            n.setSignedUrlProfilePicOpponent(null);
                            notificationsList.set(holder.getAdapterPosition(), n);
                        }
                    });
                }
                notificationsList.set(position, n);

            }


            //if it is the last notification and we havnt checked before, check for more notifications on server
            if (position == (mNotifications.size() - 1) && !mIsNoMoreNotifications) {
                //check for more
                checkForMoreNotifcations();


            }
        }

        private void checkForMoreNotifcations()
        {

                mSNotificationCache.LoadMoreNotifications(getActivity(), mNotifications.size() ,new NotificationCache.LoadMoreNotificationsCallback() {


                    @Override
                    public void onNotificationsLoaded(List<Notification> notifications) {

                        int start = mNotifications.size();
                        mNotifications.addAll(notifications);
                        if (notifications.size() != NotificationCache.LOAD_MORE_NOTIFICATIONS_AMOUNT)
                        {
                            mIsNoMoreNotifications = true;
                        }
                        mAdapter.notifyItemRangeInserted(start, notifications.size());
                        if (notifications.size() > 1) {
                            getSignedUrlsNotificationProfilePics(notifications);
                        }
                    }

                    @Override
                    public void onNoMoreNotificationsAvailable() {
                        mIsNoMoreNotifications = true;


                    }
                });



        }

        @Override
        public int getItemCount() {
            return notificationsList.size();
        }
    }
}
