package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.LinearLayout;
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
import com.liamfarrell.android.snapbattle.ui.createbattle.ChooseVotingFragment;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.caches.OtherUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateBattleAcceptedRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetChallengesResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.liamfarrell.android.snapbattle.views.SimpleDividerItemDecoration;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class BattleChallengesListFragment extends Fragment
{
	private ArrayList<Battle> mBattles;
	private View mProgressContainer;
	private BattleChallengesAdapter mChallengesAdapter;
	private CallbackManager mCallbackManager;


    @Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		mBattles = new ArrayList<Battle>();

        mChallengesAdapter = new BattleChallengesAdapter(mBattles);
		setRetainInstance(true);

		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_challenges_list, parent, false);
        mProgressContainer = v.findViewById(R.id.challenges_list_progressContainer);
        RecyclerView recyclerView = v.findViewById(R.id.recyclerList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity().getApplicationContext()));
        recyclerView.setAdapter(mChallengesAdapter);
        getBattleChallenges();

        return v;

	}

	private void getBattleChallenges() {
    	new GetBattleChallengesTask(getActivity(), this).execute();

    }
    private static class GetBattleChallengesTask extends AsyncTask<Void, Void, AsyncTaskResult<GetChallengesResponse>> {
        private WeakReference<Activity> activityReference;
        private WeakReference<BattleChallengesListFragment> fragmentReference;

        GetBattleChallengesTask(Activity activity, BattleChallengesListFragment fragment) {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

    @Override
    protected AsyncTaskResult<GetChallengesResponse> doInBackground(Void... params) {
		// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
		LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
				Regions.US_EAST_1,
				FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

		final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class, new CustomLambdaDataBinder());

				try {
					GetChallengesResponse response =  lambdaFunctionsInterface.getBattleChallenges();
					return new AsyncTaskResult<GetChallengesResponse>(response);

				} catch (LambdaFunctionException lfe) {
					Log.i("ERROR", lfe.getDetails());
					Log.i("ERROR",lfe.getStackTrace().toString());
					lfe.printStackTrace();

					return new AsyncTaskResult<GetChallengesResponse>(lfe);
				}
				catch (AmazonServiceException ase) {
					// invalid credentials, incorrect AWS signature, etc
					Log.i("ERROR", ase.getErrorMessage());
					return new AsyncTaskResult<GetChallengesResponse>(ase);
				}
				catch (AmazonClientException ace) {
					// Network issue
					Log.i("ERROR", ace.toString());
					return new AsyncTaskResult<GetChallengesResponse>(ace);
				}
			}

			@Override
			protected void onPostExecute( AsyncTaskResult<GetChallengesResponse> asyncResult) {
                BattleChallengesListFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

				GetChallengesResponse result = asyncResult.getResult();
				if (asyncResult.getError() != null)
				{
					new HandleLambdaError().handleError(asyncResult.getError(), activity, fragment.mProgressContainer);
					return;
				}

				if (result == null) {
					Toast.makeText(activity, R.string.no_challenges_battles_toast, Toast.LENGTH_LONG).show();
                    fragment.mProgressContainer.setVisibility(View.INVISIBLE);
					return;

				}

                fragment.mBattles.addAll(result.getSql_result());
                fragment.mChallengesAdapter.notifyDataSetChanged();
                fragment.mProgressContainer.setVisibility(View.INVISIBLE);

			}


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


	private void requestUserFriendsPermission(final BattleChallengesAdapter.MyViewHolder holder){
		mCallbackManager = CallbackManager.Factory.create();
		LoginManager.getInstance().registerCallback(mCallbackManager,
				new FacebookCallback<LoginResult>() {
					@Override
					public void onSuccess(LoginResult loginResult) {
						updateBattleAccepted(true, holder);
					}

					@Override
					public void onCancel() {
						//User Friends Not accepted.. Do nothing
						holder.battleAccept.setEnabled(true);
						holder.battleDecline.setEnabled(true);
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


	private void updateBattleAccepted(final boolean accepted, final BattleChallengesAdapter.MyViewHolder holder) {
        Battle b = mBattles.get(holder.getAdapterPosition());


        UpdateBattleAcceptedRequest updateBattleAccceptedRequest = new UpdateBattleAcceptedRequest();
        updateBattleAccceptedRequest.setBattleAccepted(accepted);
        updateBattleAccceptedRequest.setBattleID(b.getBattleId());
        new UpdateBattleAcceptedTask(getActivity(), this, accepted, holder, b).execute(updateBattleAccceptedRequest);
    }




    private static class UpdateBattleAcceptedTask extends AsyncTask<UpdateBattleAcceptedRequest, Void,AsyncTaskResult<DefaultResponse>>
    {

        private WeakReference<Activity> activityReference;
        private WeakReference<BattleChallengesListFragment> fragmentReference;
        private boolean accepted;
        private BattleChallengesAdapter.MyViewHolder holder;
        private Battle b;

        UpdateBattleAcceptedTask(Activity activity, BattleChallengesListFragment fragment,  boolean accepted, BattleChallengesAdapter.MyViewHolder holder, Battle b)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
            this.accepted = accepted;
            this.holder = holder;
            this.b = b;

        }
        @Override
        protected AsyncTaskResult<DefaultResponse> doInBackground(UpdateBattleAcceptedRequest... params) {
		LambdaInvokerFactory factory = new LambdaInvokerFactory(
				activityReference.get().getApplicationContext(),
				Regions.US_EAST_1,
				FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

		final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
				try {
                    DefaultResponse response = lambdaFunctionsInterface.UpdateBattleAccepted(params[0]);
                    return new AsyncTaskResult<DefaultResponse>(response);
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
			protected void onPostExecute(AsyncTaskResult<DefaultResponse> asyncResult) {
                // get a reference to the activity if it is still there
                BattleChallengesListFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

				if (asyncResult.getError() != null)
				{
					new HandleLambdaError().handleError(asyncResult.getError(), activity, fragment.mProgressContainer);
					//notifydatasetchanged to re enable buttons
					fragment.mChallengesAdapter.notifyDataSetChanged();
					return;

				}

                if (accepted)
                {
					fragment.mBattles.remove(b);
					fragment.mChallengesAdapter.notifyItemRemoved(holder.getAdapterPosition());
                    Intent i2 = new Intent(activity, ViewBattleActivity.class);
                    i2.putExtra(ViewBattleFragment.BATTLE_ID_EXTRA, b.getBattleId().toString());
                    activity.startActivity(i2);
                }
                else
                {
                    fragment.mBattles.remove(b);
                    fragment.mChallengesAdapter.notifyItemRemoved(holder.getAdapterPosition());
                }
			}

	}

	private class BattleChallengesAdapter extends RecyclerView.Adapter<BattleChallengesAdapter.MyViewHolder>
	{
		private ArrayList<Battle> battlesList;

		public class MyViewHolder extends RecyclerView.ViewHolder {


			TextView battleNameTextView;
			TextView battleOpponentTextView;

			TextView battleRoundsTextView ;
			TextView battleStatusTextView;
			TextView chosenVotingTypeTextView;
			TextView votingLengthTitleTextView;
			TextView chosenVotingLengthTextView;

			CircleImageView profilePicOpponent;
			Button battleAccept;
			Button battleDecline;

			LinearLayout votingLengthLayout;


			public MyViewHolder(View view) {
				super(view);
				battleNameTextView =view
						.findViewById(R.id.battle_list_item_battle_name_TextView);
				battleOpponentTextView = view
						.findViewById(R.id.battle_list_item_opponent_name_TextView);
				battleRoundsTextView = view.findViewById(R.id.battle_rounds_TextView);
                votingLengthLayout = view.findViewById(R.id.votingLengthLayout);
				chosenVotingTypeTextView = view.findViewById(R.id.chosenVotingTextView);
				chosenVotingLengthTextView = view.findViewById(R.id.votingLengthTextView);
				votingLengthTitleTextView = view.findViewById(R.id.votingLengthTitleTextView);
				battleStatusTextView = view.findViewById(R.id.battle_status_TextView);
				profilePicOpponent = view.findViewById(R.id.profilePic);
				battleAccept = view.findViewById(R.id.battle_list_item_accept_button);
				battleDecline = view.findViewById(R.id.battle_list_item_decline_button);

			}



		}
		BattleChallengesAdapter(ArrayList<Battle> battles) {
			battlesList = battles;
		}
		
		



		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View itemView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.list_item_battle_challenge, parent, false);


			return new MyViewHolder(itemView);
		}

		@Override
		public void onBindViewHolder(final MyViewHolder holder, final int position) {

			final Battle b = battlesList.get(position);
			Resources res = getResources();
			//holder.battleOpponentsTextView.setText(res.getString(R.string.opponentsMultiLine, b.getChallengerFacebookName(), b.getChallengedFacebookName()));

			holder.battleOpponentTextView.setText(res.getString(R.string.vsOpponent, b.getChallengerUsername()));

			String currentBattleStatus = b.getChallengedTimeSinceStatus();
			holder.battleStatusTextView.setText(currentBattleStatus);

			String battleName = b.getBattleName();
			holder.battleNameTextView.setText(res.getString(R.string.battle_name, battleName));
			holder.battleRoundsTextView.setText(res.getQuantityString(R.plurals.rounds, b.getRounds(), b.getRounds()));
			holder.chosenVotingTypeTextView.setText(b.getVoting().getVotingChoice().getLongStyle());
			//Cancel the previous set profile pic request, if it exists
			Picasso.get().cancelRequest(holder.profilePicOpponent);
			holder.profilePicOpponent.setImageResource(R.drawable.default_profile_pic100x100);

			if (b.getVoting().getVotingChoice() != ChooseVotingFragment.VotingChoice.NONE)
			{
				holder.chosenVotingLengthTextView.setVisibility(View.VISIBLE);
                holder.votingLengthLayout.setVisibility(View.VISIBLE);
                holder.votingLengthTitleTextView.setVisibility(View.VISIBLE);
				holder.chosenVotingLengthTextView.setText(b.getVoting().getVotingLength().toString(getActivity()));;
			}

			holder.battleAccept.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
				    holder.battleAccept.setEnabled(false);
				    holder.battleDecline.setEnabled(false);

					if (b.getVoting().getVotingChoice() == ChooseVotingFragment.VotingChoice.MUTUAL_FACEBOOK){
						if (doesUserHaveUserFriendsPermission()){
							updateBattleAccepted(true, holder);
						}else {
							Toast.makeText(getActivity(), R.string.need_accept_permission_user_friends, Toast.LENGTH_SHORT).show();
							requestUserFriendsPermission(holder);}
					} else{
						updateBattleAccepted(true, holder);
					}

				}
			});

			holder.battleDecline.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
                    holder.battleAccept.setEnabled(false);
                    holder.battleDecline.setEnabled(false);
					updateBattleAccepted(false, holder);

				}
			});
            holder.profilePicOpponent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Go to opponents battle list fragment
                    Intent  i = new Intent(getActivity(), UsersBattlesActivity.class);
                    i.putExtra(UsersBattlesActivity.EXTRA_COGNITO_ID, b.getOpponentCognitoID(FacebookLoginFragment.getCredentialsProvider(getActivity()).getCachedIdentityId()));
                    startActivity(i);
                }
            });

			String currentCognitoId = FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId();
			String opponentCognitoId = b.getChallengerCognitoId();
			int opponentProfilePicCount = b.getChallengerProfilePicCount();

			if (opponentProfilePicCount != 0)
			{

				OtherUsersProfilePicCacheManager profilePicCache = OtherUsersProfilePicCacheManager.getProfilePicCache(getActivity());
				profilePicCache.getSignedUrlProfilePicOpponent(opponentCognitoId, opponentProfilePicCount, b.getProfilePicSmallSignedUrl(), getActivity(), new OtherUsersProfilePicCacheManager.SignedUrlCallback() {
					@Override
					public void onSignedPicReceived(String signedUrl) {
                        Log.i("ChallengesList", "Signed url: " + signedUrl);
						Picasso.get().load(signedUrl).placeholder(R.drawable.default_profile_pic100x100).error(R.drawable.default_profile_pic100x100).into(holder.profilePicOpponent);

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
