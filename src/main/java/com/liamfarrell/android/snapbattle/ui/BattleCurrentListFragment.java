package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.caches.OtherUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CurrentBattlesRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CurrentBattleResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.liamfarrell.android.snapbattle.views.LinearDividerItemDecoration;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BattleCurrentListFragment extends Fragment
{
	private static final String TAG = "BattleCurrentFragment";
	public static final int BATTLES_PER_FETCH = 10;

	private boolean allBattlesFetched;
	private ArrayList<Battle> mBattles;
	private View mProgressContainer;
	private CurrentBattlesAdapter mCurrentBattleAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBattles = new ArrayList<>();
		mCurrentBattleAdapter= new CurrentBattlesAdapter(mBattles);
		allBattlesFetched = false;
		setRetainInstance(true);


	}


    /**
     * Gets the current battles
     */
	private void getCurrentBattles() {
        mProgressContainer.setVisibility(View.VISIBLE);
        CurrentBattlesRequest request = new CurrentBattlesRequest();
        request.fetchLimit = BATTLES_PER_FETCH;

        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //sdf.format(mBattles.get(mBattles.size() - 1).getLastVideoUploadedTime());
        request.setOffset(mBattles.size());

        new GetCurrentBattlesTask(getActivity(), this).execute(request);
    }

    private static class GetCurrentBattlesTask extends AsyncTask<CurrentBattlesRequest, Void, AsyncTaskResult<CurrentBattleResponse>> {
        private WeakReference<Activity> activityReference;
        private WeakReference<BattleCurrentListFragment> fragmentReference;

        GetCurrentBattlesTask(Activity activity, BattleCurrentListFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }


		// The Lambda function invocation results in a network call
		// Make sure it is not called from the main thread
			@Override
			protected AsyncTaskResult<CurrentBattleResponse> doInBackground(CurrentBattlesRequest... params)  {
                // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
                LambdaInvokerFactory factory = new LambdaInvokerFactory(
                        activityReference.get().getApplicationContext(),
                        Regions.US_EAST_1,
                        FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

                // Create the Lambda proxy object with default Json data binder.
                // You can provide your own data binder by implementing
                // LambdaDataBinder
                final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class,  new CustomLambdaDataBinder());
				try
				{
					CurrentBattleResponse response =  lambdaFunctionsInterface.getCurrentBattle(params[0]);
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
                    //return null;

                }
			}

			@Override
			protected void onPostExecute( AsyncTaskResult<CurrentBattleResponse> asyncResult) {
                // get a reference to the activity and fragment if it is still there
                BattleCurrentListFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                CurrentBattleResponse result = asyncResult.getResult();
				if (asyncResult.getError() != null)
				{
					new HandleLambdaError().handleError(asyncResult.getError(), activity, fragment.mProgressContainer);
					return;
				}

				if (result == null) {
                    fragment. mProgressContainer.setVisibility(View.INVISIBLE);
					Toast.makeText(activity, R.string.no_current_battles_toast, Toast.LENGTH_LONG).show();
					return;

				}
				int oldLastIndex = fragment.mBattles.size();
				fragment.mBattles.addAll(result.getSqlResult());
                fragment.mCurrentBattleAdapter.notifyItemRangeInserted(oldLastIndex, fragment.mBattles.size());
                if (fragment.mBattles.size() != BATTLES_PER_FETCH)
				{
                    fragment.allBattlesFetched = true;
				}
                fragment.mProgressContainer.setVisibility(View.INVISIBLE);



		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_current_list, parent, false);
		mProgressContainer = v.findViewById(R.id.current_list_progressContainer);
		RecyclerView mRecyclerView = v.findViewById(R.id.recyclerList);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setLayoutManager(mLayoutManager);
		Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider);
		RecyclerView.ItemDecoration dividerItemDecoration = new LinearDividerItemDecoration(dividerDrawable);
		mRecyclerView.addItemDecoration(dividerItemDecoration);
		mRecyclerView.setAdapter(mCurrentBattleAdapter);

		getCurrentBattles();

		return v;
	}
	
	

	private class CurrentBattlesAdapter extends RecyclerView.Adapter<CurrentBattlesAdapter.MyViewHolder> {

		private ArrayList<Battle> battlesList;

		public class MyViewHolder extends RecyclerView.ViewHolder {


			TextView battleNameTextView;
			TextView battleOpponentTextView;
			TextView timeAgoTextView;

			TextView battleRoundsTextView ;
			TextView battleStatusTextView;
			LinearLayout whoTurnFrameLayout;
			CircleImageView profilePicOpponent;


			public MyViewHolder(View view) {
				super(view);
				battleNameTextView =view
						.findViewById(R.id.battle_list_item_battle_name_TextView);
				battleOpponentTextView = view
						.findViewById(R.id.battle_list_item_opponent_name_TextView);
				battleRoundsTextView = view
						.findViewById(R.id.battle_rounds_TextView);
				battleStatusTextView = view.findViewById(R.id.battle_status_TextView);
				whoTurnFrameLayout = view.findViewById(R.id.whoTurnLinearLayout);
				profilePicOpponent = view.findViewById(R.id.profilePic);
				timeAgoTextView = view.findViewById(R.id.time_ago_TextView);
			}
		}

		public CurrentBattlesAdapter(ArrayList<Battle> battlesList) {
			this.battlesList = battlesList;
		}

		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View itemView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.list_item_battle_current, parent, false);


			return new MyViewHolder(itemView);
		}

		@Override
		public void onBindViewHolder(final MyViewHolder holder, final int position) {

			final Battle b = battlesList.get(position);
			Resources res = getResources();

			holder.battleOpponentTextView.setText(res.getString(R.string.vsOpponent, b.getOpponentName(FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId())));
			Log.i(TAG, "Opponent Name: " + b.getOpponentName(FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId()));
			String currentBattleStatus = b.getCurrentBattleStatus();
			currentBattleStatus = currentBattleStatus.replace(" ", "\n");
			currentBattleStatus = currentBattleStatus.toUpperCase();
			holder.battleStatusTextView.setText(currentBattleStatus);
			holder.timeAgoTextView.setText(b.getTimeSinceLastVideosUploaded());
			//Cancel the previous set profile pic request, if it exists
			Picasso.get().cancelRequest(holder.profilePicOpponent);
            holder.profilePicOpponent.setImageResource(R.drawable.default_profile_pic100x100);

			String battleName = b.getBattleName();
			holder.battleNameTextView.setText(res.getString(R.string.battle_name, battleName));
			holder.battleRoundsTextView.setText(res.getQuantityString(R.plurals.rounds, b.getRounds(), b.getRounds()));

			if (b.getWhoTurn() == Battle.Who_turn.OPPONENT_TURN) {
				holder.itemView.setBackgroundColor(getResources().getColor(R.color.opponent_turn_background));
			} else if (b.getWhoTurn() == Battle.Who_turn.YOUR_TURN) {
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.your_turn_background));

			}
			holder.profilePicOpponent.setOnClickListener(new CircleImageView.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent  i = new Intent(getActivity(), UsersBattlesActivity.class);
					i.putExtra(UsersBattlesActivity.EXTRA_COGNITO_ID, b.getOpponentCognitoID(FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId()));
					startActivity(i);
				}
			});



			String currentCognitoId = FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId();
			String opponentCognitoId = b.getOpponentCognitoID(currentCognitoId);
			int opponentProfilePicCount = b.getOpponentProfilePicCount(currentCognitoId);

			if (opponentProfilePicCount == 0)
			{
				Picasso.get().load(R.drawable.default_profile_pic100x100).into(holder.profilePicOpponent);
			}
			else
			{

				OtherUsersProfilePicCacheManager profilePicCache = OtherUsersProfilePicCacheManager.getProfilePicCache(getActivity());
				profilePicCache.getSignedUrlProfilePicOpponent(opponentCognitoId, opponentProfilePicCount, b.getProfilePicSmallSignedUrl(), getActivity(), new OtherUsersProfilePicCacheManager.SignedUrlCallback() {
					@Override
					public void onSignedPicReceived(String signedUrl) {
						Picasso.get().load(signedUrl).placeholder(R.drawable.default_profile_pic100x100).error(R.drawable.default_profile_pic100x100).into(holder.profilePicOpponent);
					}
				});
			}
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					Intent  i = new Intent(getActivity(), ViewBattleActivity.class);
					i.putExtra(ViewBattleFragment.BATTLE_ID_EXTRA, b.getBattleId().toString());
					startActivity(i);
				}
			});

			if (position == battlesList.size() - 1 && !allBattlesFetched)
			{
				getCurrentBattles();
			}

		}
		@Override
		public int getItemCount() {
			return battlesList.size();
		}



	}

	
	

}
