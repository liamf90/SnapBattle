package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.ui.createbattle.ChooseVotingFragment;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.Video;
import com.liamfarrell.android.snapbattle.model.Voting;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CompletedBattlesRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CompletedBattlesResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BattleCompletedListFragment extends Fragment
{
	public static final int BATTLES_PER_FETCH = 10;

	protected boolean allBattlesFetched;
	protected ArrayList<Battle> mBattles;
	protected View mProgressContainer;
	protected CompletedBattlesAdapter mCurrentBattleAdapter;
	protected RecyclerView mRecyclerView;

	protected int getLayoutID()
	{
		return R.layout.fragment_completed_list;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBattles = new ArrayList<Battle>();
		mCurrentBattleAdapter= new CompletedBattlesAdapter(mBattles);
		allBattlesFetched = false;
		setRetainInstance(true);
	}



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FullBattleVideoPlayerFragment.VOTE_DONE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            int battleID = data.getIntExtra(FullBattleVideoPlayerFragment.INTENT_EXTRA_BATTLE_ID_VOTE_DONE_REQUEST, -1);
            if (battleID != -1) {
                int position = -1;
                for (int i=0; i < mBattles.size(); i++) {
                    if (mBattles.get(i).getBattleID() == battleID){
                        position = i;
                        break;
                    }
                }
                if (position != -1){
                    mBattles.get(position).setUserHasVoted(true);
                    mCurrentBattleAdapter.notifyItemChanged( mCurrentBattleAdapter.getBattleIdPosition(battleID));
                }

            }
        }
    }


	protected void getBattles() {

		mProgressContainer.setVisibility(View.VISIBLE);

        CompletedBattlesRequest request = new CompletedBattlesRequest();
        request.setFetchLimit(BATTLES_PER_FETCH);
        if (mBattles.size() > 0)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            request.setGetAfterDate(sdf.format(mBattles.get(mBattles.size() - 1).getLastVideoUploadTime()));
        }
        new GetBattlesTask(getActivity(), this).execute(request);


	}

	private static class GetBattlesTask extends AsyncTask<CompletedBattlesRequest, Void,AsyncTaskResult<CompletedBattlesResponse>>
	{

		private WeakReference<Activity> activityReference;
		private WeakReference<BattleCompletedListFragment> fragmentReference;

		GetBattlesTask(Activity activity, BattleCompletedListFragment fragment) {
			fragmentReference = new WeakReference<>(fragment);
			activityReference = new WeakReference<>(activity);
		}



		// The Lambda function invocation results in a network call
		// Make sure it is not called from the main thread

			@Override
			protected AsyncTaskResult<CompletedBattlesResponse> doInBackground(CompletedBattlesRequest... params) {
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
					CompletedBattlesResponse response = lambdaFunctionsInterface.getCompletedBattles(params[0]);
					return new AsyncTaskResult<CompletedBattlesResponse>(response);

				} catch (LambdaFunctionException lfe) {

					Log.i("ERROR", lfe.getDetails());
					Log.i("ERROR",lfe.getStackTrace().toString());
					lfe.printStackTrace();

					return new AsyncTaskResult<CompletedBattlesResponse>(lfe);
				}
				catch (AmazonServiceException ase) {
					// invalid credentials, incorrect AWS signature, etc
					Log.i("ERROR", ase.getErrorMessage());
					return new AsyncTaskResult<CompletedBattlesResponse>(ase);
				}
				catch (AmazonClientException ace) {
					// Network issue
					Log.i("ERROR", ace.toString());
					return new AsyncTaskResult<CompletedBattlesResponse>(ace);
				}
			}

			@Override
			protected void onPostExecute( AsyncTaskResult<CompletedBattlesResponse> asyncResult) {
				BattleCompletedListFragment fragment = fragmentReference.get();
				Activity activity = activityReference.get();
				if (fragment == null || fragment.isRemoving()) return;
				if (activity == null || activity.isFinishing()) return;

				CompletedBattlesResponse result = asyncResult.getResult();
				if (asyncResult.getError() != null)
				{
                    new HandleLambdaError().handleError(asyncResult.getError(), activity, fragment.mProgressContainer);
                    return;
				}

				if (result == null) {
					Toast.makeText(activity, R.string.no_completed_battles_toast, Toast.LENGTH_LONG).show();
					fragment.mProgressContainer.setVisibility(View.INVISIBLE);
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

		View v = inflater.inflate(getLayoutID(), parent, false);
		mProgressContainer = v.findViewById(R.id.completed_list_progressContainer);
		mRecyclerView = v.findViewById(R.id.recyclerList);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setAdapter(mCurrentBattleAdapter);
		mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
		getBattles();

		return v;
	}

    protected View.OnClickListener getBattleOnClickListener(final Battle b)
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent  i = new Intent(getActivity(), ViewBattleActivity.class);
                i.putExtra(ViewBattleFragment.BATTLE_ID_EXTRA, b.getBattleId().toString());
                startActivity(i);
            }
        };

    }

	protected class CompletedBattlesAdapter extends RecyclerView.Adapter<CompletedBattlesAdapter.MyViewHolder> {

		private ArrayList<Battle> battlesList;

		public class MyViewHolder extends RecyclerView.ViewHolder {

		    LinearLayout parentLinearLayout;
			ImageView thumbnailImageView;
			TextView battleNameTextView;
			TextView battleRoundsTextView ;
			TextView battleStatusTextView;
			TextView likeCountTextView;
			TextView dislikeCounTextView;
			TextView videoViewCountTextView;
			TextView challengerUsernameTextView, challengedUsernameTextView;
			TextView challengerResultTextView, challengedResultTextView, challengerVotesTextView, challengedVotesTextView, votingTypeTextView, canVoteTextView, timeUntilVoteEndsTextView;
			ConstraintLayout votingLayout;



			public MyViewHolder(View view) {
				super(view);
				battleNameTextView =view
						.findViewById(R.id.battle_list_item_battle_name_TextView);

				battleRoundsTextView = view.findViewById(R.id.battle_rounds_TextView);
				battleStatusTextView = view.findViewById(R.id.battle_status_TextView);
				thumbnailImageView = view.findViewById(R.id.thumbnailImageView);
                parentLinearLayout = view.findViewById(R.id.parentLinearLayout);
				dislikeCounTextView = view.findViewById(R.id.dislikeCountTextView);
				likeCountTextView = view.findViewById(R.id.likeCountTextView);
				challengerUsernameTextView = view.findViewById(R.id.challenger_name_TextView);
				challengedUsernameTextView = view.findViewById(R.id.challenged_name_TextView);
                videoViewCountTextView = view.findViewById(R.id.videoViewCountTextView);

				//Voting TextViews
				challengerResultTextView = view.findViewById(R.id.challenger_result_TextView);
				challengedResultTextView = view.findViewById(R.id.challenged_result_TextView);
				challengerVotesTextView = view.findViewById(R.id.challenger_votes_TextView);
				challengedVotesTextView = view.findViewById(R.id.challenged_votes_TextView);
				votingTypeTextView = view.findViewById(R.id.voting_type_TextView);
				canVoteTextView = view.findViewById(R.id.can_vote_TextView);
				timeUntilVoteEndsTextView = view.findViewById(R.id.time_until_vote_endsTextView);


                votingLayout = view.findViewById(R.id.votingLayout);
			}



		}



		public CompletedBattlesAdapter(ArrayList<Battle> battlesList) {
			this.battlesList = battlesList;
		}

		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View itemView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.list_item_battle_friends, parent, false);


			return new MyViewHolder(itemView);
		}

        int getBattleIdPosition(int battleID) {
            for (int i=0; i< battlesList.size(); i++) {
                if (battlesList.get(i).getBattleID() == battleID) {
                    return i;
                }
            }
            return -1;
        }

		@Override
		public void onBindViewHolder(final MyViewHolder holder, final int position) {

			final Battle b = battlesList.get(position);


			final Resources res = getResources();

			String battleName = b.getBattleName();
			//Cancel the previous set thumbnail request, if it exists
			Picasso.get().cancelRequest(holder.thumbnailImageView);
			holder.thumbnailImageView.setImageResource(R.drawable.placeholder1440x750);
			holder.battleNameTextView.setText(res.getString(R.string.battle_name, battleName));
			holder.battleStatusTextView.setText(b.getCompletedBattleStatus());
			holder.battleRoundsTextView.setText(res.getQuantityString(R.plurals.rounds, b.getRounds(), b.getRounds()));
			holder.likeCountTextView.setText(res.getQuantityString(R.plurals.likes, b.getLikeCount(), b.getLikeCount()));
			holder.dislikeCounTextView.setText(res.getQuantityString(R.plurals.dislikes, b.getDislikeCount(), b.getDislikeCount()));
			holder.challengerUsernameTextView.setText(b.getChallengerUsername());
			holder.challengedUsernameTextView.setText(b.getChallengedUsername());
			holder.videoViewCountTextView.setText(res.getString(R.string.video_views, b.getVideoViewCount()));
            holder.votingTypeTextView.setText(b.getVoting().getVotingChoice().getLongStyle());

            holder.challengerUsernameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), UsersBattlesActivity.class);
                    i.putExtra(UsersBattlesActivity.EXTRA_COGNITO_ID, b.getChallengerCognitoID());
                    startActivity(i);

                }
            });
            holder.challengedUsernameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), UsersBattlesActivity.class);
                    i.putExtra(UsersBattlesActivity.EXTRA_COGNITO_ID, b.getChallengedCognitoID());
                    startActivity(i);


                }
            });

			if (b.getVoting().getVotingChoice() == ChooseVotingFragment.VotingChoice.NONE)
			{
				holder.votingLayout.setVisibility(View.VISIBLE);
				holder.challengerResultTextView.setVisibility(View.GONE);
				holder.challengedResultTextView.setVisibility(View.GONE);
				holder.challengerVotesTextView.setVisibility(View.GONE);
				holder.challengedVotesTextView.setVisibility(View.GONE);
				holder.timeUntilVoteEndsTextView.setVisibility(View.GONE);
				holder.canVoteTextView.setVisibility(View.GONE);
			}
			else {
				holder.votingLayout.setVisibility(View.VISIBLE);

				if (b.getVoting().getVotingTimeEnd() == null)
				{
					//voting hasnt begun yet
					holder.challengerResultTextView.setVisibility(View.GONE);
					holder.challengedResultTextView.setVisibility(View.GONE);
					holder.challengerVotesTextView.setVisibility(View.GONE);
					holder.challengedVotesTextView.setVisibility(View.GONE);
					holder.timeUntilVoteEndsTextView.setVisibility(View.GONE);
					holder.canVoteTextView.setVisibility(View.GONE);
				}
				else if (b.getVoting().getVotingTimeEnd() != null && b.getVoting().getVotingTimeEnd().after(new Date(System.currentTimeMillis()))) {
					//voting is still going
					holder.challengerResultTextView.setVisibility(View.GONE);
					holder.challengedResultTextView.setVisibility(View.GONE);
					holder.challengerVotesTextView.setVisibility(View.GONE);
					holder.challengedVotesTextView.setVisibility(View.GONE);
					holder.canVoteTextView.setVisibility(View.VISIBLE);

					if (b.getUserHasVoted() != null && b.getUserHasVoted()){
						holder.canVoteTextView.setText(res.getText(R.string.have_voted));
					} else {
						b.getVoting().canUserVote(FacebookLoginFragment.getCredentialsProvider(getActivity()).getCachedIdentityId(), b.getChallengerCognitoID(), b.getChallengedCognitoID(), b.getChallengerFacebookUserId(), b.getChallengedFacebookUserId(), new Voting.MutualFriendCallbacks() {
							@Override
							public void onCanVote() {
								holder.canVoteTextView.setText(res.getText(R.string.can_vote));
							}

							@Override
							public void onCannotVote() {
								holder.canVoteTextView.setText(res.getText(R.string.can_not_vote));
							}
						});
					}


					holder.timeUntilVoteEndsTextView.setVisibility(View.VISIBLE);
					holder.timeUntilVoteEndsTextView.setText(res.getString(R.string.voting_time_left, Video.getTimeUntil(b.getVoting().getVotingTimeEnd())));
				} else if (b.getVoting().getVotingTimeEnd() != null && !b.getVoting().getVotingTimeEnd().after(new Date(System.currentTimeMillis())))
				{
					//voting has finished
					holder.canVoteTextView.setVisibility(View.GONE);
					holder.timeUntilVoteEndsTextView.setVisibility(View.GONE);

					holder.challengerResultTextView.setVisibility(View.VISIBLE);
					holder.challengedResultTextView.setVisibility(View.VISIBLE);
					holder.challengerVotesTextView.setVisibility(View.VISIBLE);
					holder.challengedVotesTextView.setVisibility(View.VISIBLE);
					holder.challengerResultTextView.setText(b.getVoting().getChallengerVotingResult());
					holder.challengedResultTextView.setText(b.getVoting().getChallengedVotingResult());
					holder.challengerVotesTextView.setText(res.getQuantityString(R.plurals.votes, b.getVoting().getVoteChallenger(), b.getVoting().getVoteChallenger()));
					holder.challengedVotesTextView.setText(res.getQuantityString(R.plurals.votes, b.getVoting().getVoteChallenged(), b.getVoting().getVoteChallenged()));
				}
			}

			Log.i("CompletedListFrag", "Signed url: " + b.getSignedThumbnailUrl());
			Picasso.get().load(b.getSignedThumbnailUrl()).placeholder(R.drawable.placeholder1440x750).error(R.drawable.placeholder1440x750).into(holder.thumbnailImageView);

            holder.itemView.setOnClickListener(getBattleOnClickListener(b));


			if (position == battlesList.size() - 1 && !allBattlesFetched)
			{
				getBattles();
			}

		}
		@Override
		public int getItemCount() {
			return battlesList.size();
		}



	}




}
