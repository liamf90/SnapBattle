
package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.liamfarrell.android.snapbattle.ActivityMainNavigationDrawer;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.BattleIDSave;
import com.liamfarrell.android.snapbattle.model.Voting;
import com.liamfarrell.android.snapbattle.util.UserFollowingChecker;
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseVotingFragment;
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache;
import com.liamfarrell.android.snapbattle.caches.FollowingBattleCache;
import com.liamfarrell.android.snapbattle.caches.FollowingBattleCache.LoadMoreBattlesCallback;
import com.liamfarrell.android.snapbattle.caches.FollowingBattleCacheFile;
import com.liamfarrell.android.snapbattle.caches.ThumbnailCacheHelper;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.Video;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
public class FollowingBattleListFragment extends Fragment
{
	private static final String TAG = "FollowingBattleList";
    private static final int LOAD_MORE_BEFORE_LAST_BATTLE_AMOUNT = 5;

	private static final int HOW_OFTEN_CHECK_FOR_UPDATES_MILLISECONDS = 60000; // EVERY 60 SECONDS
	private RecyclerView recyclerView;
	private TextView noBattlesTextView;
	private FollowingListAdapter mAdapter;
	private LinkedList<BattleIDSave> mBattleIDList;

	private SwipeRefreshLayout mSwipeToRefreshLayout;
    private LinearLayoutManager mLayoutManager;
	private boolean isFollowerCacheFullLoaded = false;
	private Handler updateHandler;
	private View mProgressContainer;
	private static boolean endOfList;



    static { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); }
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
        Log.i("FollowingBattleList", "On Create");
		super.onCreate(savedInstanceState);
		mBattleIDList = new LinkedList<>();

		endOfList = false;
		FollowingBattleCache.get(getActivity());
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_friends_battle_list, parent, false);
		mProgressContainer = v.findViewById(R.id.progressContainer);
		mProgressContainer.setVisibility(View.INVISIBLE);

		mSwipeToRefreshLayout= v.findViewById(R.id.swipe_container);
		mSwipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mSwipeToRefreshLayout.setRefreshing(true);
				updateFollowingBattleList();
			}
		});
		recyclerView = v.findViewById(R.id.recycler_view);


		mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
		mLayoutManager.setSmoothScrollbarEnabled(true);
		mLayoutManager.setItemPrefetchEnabled(true);
		mLayoutManager.setInitialPrefetchItemCount(10);

		recyclerView.setLayoutManager(mLayoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
		
       // recyclerView.setItemViewCacheSize(20);
        //recyclerView.setDrawingCacheEnabled(true);
        //recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);


		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				if (dy > 0)
				{
					((ActivityMainNavigationDrawer)getActivity()).hideFab();
				}
				else
				{
					((ActivityMainNavigationDrawer)getActivity()).showFab();
				}

			}
		});

		noBattlesTextView = v.findViewById(R.id.NoBattlesTextView);


		//Set up the follower cache, this cache knows who the user follows so any topBattles that are
		//in the news feed list that belong to an old follower will not be viewed.
        FollowingUserCache.CacheLoadCallbacks followerCacheCallbacks = new FollowingUserCache.CacheLoadCallbacks() {

            @Override
            public void onUpdated() { isFollowerCacheFullLoaded = true; }

            @Override
            public void onNoUpdates() { isFollowerCacheFullLoaded = true; }

            @Override
            public void onLoadedFromSQL() { isFollowerCacheFullLoaded = true; }

            @Override
            public void onLoadedFromFile() { }

            @Override
            public void onCacheAlreadyLoaded() { isFollowerCacheFullLoaded = true; }
        };
        FollowingUserCache.get(getActivity(), followerCacheCallbacks).updateCache(getActivity(),null);;
        mAdapter = new FollowingListAdapter(mBattleIDList);
        recyclerView.setAdapter(mAdapter);
        //updateFollowingList();
        loadFollowingBattleList();

        //turn on the background handler that checks for updates to topBattles every so often, whilst the fragment is being viewed
        turnOnCheckForUpdatesRepeater();

		return v;
	}


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FullBattleVideoPlayerFragment.VOTE_DONE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            int battleID = data.getIntExtra(FullBattleVideoPlayerFragment.INTENT_EXTRA_BATTLE_ID_VOTE_DONE_REQUEST, -1);
            if (battleID != -1) {
                mAdapter.notifyItemChanged(mAdapter.getBattleIdPosition(battleID));
            }
        }
    }

    public void updateFollowingList()
	{
	    Log.i("HomeFeed", "--FOLLOWINGBATTLES--- UPDATING FOLLOWING LIST");
		if (!FollowingBattleCache.get(getActivity()).isLoaded())
		{
			loadFollowingBattleList();
		}
		else
		{
			updateFollowingBattleList();
		}
	}


	private void updateFollowingBattleList() {
		final FollowingBattleCache.UpdateCallbacks callback = new FollowingBattleCache.UpdateCallbacks() {

			@Override
            public void onUpdated(List<Integer> updatedBattleIDList) {
				//Mysql data is updated. Update the visible topBattles if they have been updated
                for (int i= mLayoutManager.findFirstVisibleItemPosition(); i <= mLayoutManager.findLastVisibleItemPosition() && i>=0; i++)
                {
                    if (updatedBattleIDList.contains(FollowingBattleCache.get(getActivity()).getBattleIDFromPosition(i))) {
                        Log.i(TAG, "Updating visible item at position: " + i);
                        mAdapter.notifyItemChanged(i);
                    }
                }
				mSwipeToRefreshLayout.setRefreshing(false);
			}
			
			@Override
            public void onNewBattles(List<BattleIDSave> battleIDList) {
			    if (noBattlesTextView.getVisibility() == View.VISIBLE){
                noBattlesTextView.setVisibility(View.GONE);}
				// add topBattles to start of battle id
				for (int i = battleIDList.size() - 1; i >= 0; i--) {
					mBattleIDList.addFirst(battleIDList
							.get(i));
				}

				mAdapter.notifyItemRangeInserted(0, battleIDList.size());

                //scroll to top of list
                recyclerView.smoothScrollToPosition(0);
			}


            @Override
			public void onFullFeedUpdate(List<BattleIDSave> battleIDList) {
                if (noBattlesTextView.getVisibility() == View.VISIBLE){
                    noBattlesTextView.setVisibility(View.GONE);}
                mBattleIDList.clear();
				mBattleIDList.addAll(battleIDList);
				mAdapter.notifyDataSetChanged();

			}




        };
		FollowingBattleCache.get(getActivity()).updateList(getActivity(), callback);
	}


	//This method checks for updates on the server to the following topBattles every so often
	private void turnOnCheckForUpdatesRepeater(){
        updateHandler = new Handler();
        final int delay = HOW_OFTEN_CHECK_FOR_UPDATES_MILLISECONDS;

        updateHandler.postDelayed(new Runnable(){
            public void run(){
                if (FollowingBattleListFragment.this.isResumed()) {
                    updateFollowingBattleList();
                }
                updateHandler.postDelayed(this, delay);
            }
        }, delay);

    }


	
	private void loadFollowingBattleList()
	{
        //load the topBattles that the user follows from the following battle cache.

        //on updated from server after load callbacks
		FollowingBattleCache.UpdateCallbacks updateCallbacks = new FollowingBattleCache.UpdateCallbacks() {
			
			@Override
            public void onUpdated(List<Integer> updatedBattleIDList) {
				Log.i(TAG, "Update Following Battles - On Updated");
				//reload the visible topBattles
                for (int i= mLayoutManager.findFirstVisibleItemPosition(); i <= mLayoutManager.findLastVisibleItemPosition()&& i>=0; i++)
                {
                    if (updatedBattleIDList.contains(FollowingBattleCache.get(getActivity()).getBattleIDFromPosition(i))) {
                        Log.i(TAG, "Updating visible item at position: " + i);
                        mAdapter.notifyItemChanged(i);
                    }
                }
			}
			
			@Override
            public void onNewBattles(List<BattleIDSave> battleIDList) {
				// add topBattles to start of battle id
				for (int i = battleIDList.size() - 1; i >= 0; i--) {
					mBattleIDList.addFirst(battleIDList
							.get(i));
				}

				Log.i(TAG, "Update Folloqing Battles - On new topBattles");
				noBattlesTextView.setVisibility(View.GONE);
				mAdapter.notifyItemRangeInserted(0, battleIDList.size());
				//scroll to top of list
                recyclerView.smoothScrollToPosition(0);
			}


            @Override
			public void onFullFeedUpdate(List<BattleIDSave> battleIDList) {
				mBattleIDList.clear();
				mBattleIDList.addAll(battleIDList);
				mAdapter.notifyDataSetChanged();
			}



        };


		//on cache loaded
		FollowingBattleCache.CacheLoadCallbacks loadCallbacks = new FollowingBattleCache.CacheLoadCallbacks() {
			@Override
			public void OnNoBattlesInFeed() {
				Log.i(TAG, "LoadFriendsBattles - NoBattlesInFeed");
						mProgressContainer.setVisibility(View.GONE);
						noBattlesTextView.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void OnNoFileForUser() {
				Log.i(TAG, "LoadFriendsBattles - NoFileForUser");
						mProgressContainer.setVisibility(View.VISIBLE);
			}


			@Override
			public void OnCacheLoaded(List<BattleIDSave> battleIDList) {
				Log.i(TAG, "LoadFriendsBattles - OnCacheLoaded. Size: " + battleIDList.size());
				//When cache is loaded checking that more can still be loaded from server.
                //(if the max capacity of the cache file is greater then the size of the list than no more exists)
				if (battleIDList.size() < FollowingBattleCacheFile.FILE_MAX_CAPACITY)
				{
                    Log.i(TAG, "LoadFriendsBattles - OnCacheLoaded - End of List = true");
					endOfList = true;
				}
				else
				{
                    Log.i(TAG, "LoadFriendsBattles - OnCacheLoaded - End of List = false");
					endOfList = false;
				}
				if (battleIDList.size() == 0)
				{
                    Log.i(TAG, "LoadFriendsBattles - OnCacheLoaded - No Battles");
					noBattlesTextView.setVisibility(View.VISIBLE);
				}
				mBattleIDList.addAll(battleIDList);
                recyclerView.setAdapter(mAdapter);
				
			}

			@Override
			public void OnNoFileLoad(List<BattleIDSave> battleIDList) {
			            //loaded from server instead
						Log.i(TAG, "No file Load");
						mProgressContainer.setVisibility(View.INVISIBLE);
				endOfList = battleIDList.size() < FollowingBattleCacheFile.FILE_MAX_CAPACITY;
				mBattleIDList.addAll(battleIDList);

                if (battleIDList.size() == 0) {
                    Log.i(TAG, "LoadFriendsBattles - OnCacheLoaded - No Battles");
                    noBattlesTextView.setVisibility(View.VISIBLE);
                }
                 recyclerView.setAdapter(mAdapter);
			}
			
		};
		FollowingBattleCache.get(getActivity()).loadList(getActivity(), loadCallbacks, updateCallbacks);
	}


	private class FollowingListAdapter extends RecyclerView.Adapter<FollowingListAdapter.MyViewHolder> {

		private List<BattleIDSave> followingBattlesIDList;

		public class MyViewHolder extends RecyclerView.ViewHolder {

			private TextView battleNameTextView, battleStatusTextView, battleRoundsTextView, likeCountTextView, dislikeCountTextView;

			//Voting TextViews
			private TextView challengerResultTextView, challengedResultTextView, challengerVotesTextView, challengedVotesTextView, votingTypeTextView, canVoteTextView, timeUntilVoteEndsTextView, challengerUsernameTextView, challengedUsernameTextView;
			private TextView videoViewCountTextView;
			private ConstraintLayout votingLayout;

			public ImageView thumbnail;
			private View mView;

			private FrameLayout noMoreBattlesView;
			private TextView noMoreBattlesTextView;
			private View noMoreBattlesProgressContainer;

			public MyViewHolder(View view) {
				super(view);

				mView = view;

				battleNameTextView = view
						.findViewById(R.id.battle_list_item_battle_name_TextView);

				battleRoundsTextView = view.findViewById(R.id.battle_rounds_TextView);
				battleStatusTextView = view.findViewById(R.id.battle_status_TextView);
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

				//Voting Holder
				votingLayout = view.findViewById(R.id.votingLayout);

				dislikeCountTextView = view.findViewById(R.id.dislikeCountTextView);
				likeCountTextView = view.findViewById(R.id.likeCountTextView);
				thumbnail = (ImageView)view.findViewById(R.id.thumbnailImageView);
                thumbnail.setImageResource(R.drawable.placeholder1440x750);


				//No more topBattles View
               // noMoreBattlesView = view.findViewById(R.id.loadMoreBattlesLayout);
                //noMoreBattlesTextView  = view.findViewById(R.id.loadMoreBattlesTextView);
                noMoreBattlesProgressContainer = view.findViewById(R.id.loadMorebattlesProgressContainer);
			}
		}

        int getBattleIdPosition(int battleID) {
            for (int i=0; i< followingBattlesIDList.size(); i++) {
                if (followingBattlesIDList.get(i).getBattleID() == battleID) {
                    return i;
                }
            }
            return -1;
        }

		private FollowingListAdapter(List<BattleIDSave> followingBattlesList) {
			this.followingBattlesIDList = followingBattlesList;
		}

		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View itemView = LayoutInflater.from(parent.getContext())
					.inflate( 	R.layout.list_item_battle_friends, parent, false);


			return new MyViewHolder(itemView);
		}




		@Override
		public void onBindViewHolder(final MyViewHolder holder, final int position) {
            //set values of the battle
            //holder.thumbnail.setImageResource(R.drawable.placeholder1440x750);

			final int battleID = followingBattlesIDList.get(position).getBattleID();
			final Battle b = FollowingBattleCache.get(getActivity()).getFollowingBattle(battleID);

			holder.thumbnail.setOnClickListener(new ImageView.OnClickListener() {
				@Override
				public void onClick(View view) {
				    UserFollowingChecker followingChecker = new UserFollowingChecker();
                    UserFollowingChecker.FollowingCognitoCallback callback= new UserFollowingChecker.FollowingCognitoCallback() {

						@Override
						public void onFollowingCognitoReceived(String CognitoID)
						{


							Log.i(TAG, "Following Cognito ID received");
							String filepath = b.getServerFinalVideoUrl(CognitoID);
							mProgressContainer.setVisibility(View.INVISIBLE);
							Intent intent = new Intent(getActivity(), FullBattleVideoPlayerActivity.class);
							intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_BATTLEID, battleID);
							intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_FILE_VIDEO_PATH, filepath);
							intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGER_USERNAME, b.getChallengerUsername());
                            intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGED_USERNAME, b.getChallengedUsername());
							//Log.i(TAG, "Orientation Lock: " + b.getOrientationLock());
                            //start callbacks for result, result is if the user has voted so we can update the list view item of the battle according on resume
                            startActivityForResult(intent, FullBattleVideoPlayerFragment.VOTE_DONE_REQUEST_CODE);
						}
					};
                    followingChecker.getFollowingCognitoID(b.getChallengerCognitoID(), b.getChallengedCognitoID(), getActivity(), callback);
				}
			});

			if  (b == null)
			{
				Log.i(TAG, "ERROR b is null. Position: " + position + ", BattleID: " + battleID);
			}


            //check to see if we are following either of challenger or challenged still, if not do not show the battle OR BATTLE is deleted

			Date battleAddedToCacheDateTime = followingBattlesIDList.get(position).getTimeAddedToCache();
			Date followingUserCacheLastUpdated = FollowingUserCache.get(getActivity(), null).getFollowingUserCacheLastUpdated();

			FollowingUserCache followingUserCache = FollowingUserCache.get(getActivity(), null);
			if (b!= null && !b.isDeleted() && (!isFollowerCacheFullLoaded  ||
                    (battleAddedToCacheDateTime.after(followingUserCacheLastUpdated) || (followingUserCache.isCognitoIDFollower(b.getChallengedCognitoID())
                            || followingUserCache.isCognitoIDFollower(b.getChallengerCognitoID())) )))
			{
				holder.mView.setVisibility(View.VISIBLE);
				holder.mView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

				setThumbnail(holder, position, battleID, b);

				final Resources res = getResources();


                String battleName = b.getBattleName();
                holder.battleNameTextView.setText(res.getString(R.string.battle_name, battleName));
				holder.challengerUsernameTextView.setText(b.getChallengerUsername());
				holder.challengedUsernameTextView.setText(b.getChallengedUsername());
				holder.battleStatusTextView.setText(b.getCompletedBattleStatus(getContext()));
				holder.battleRoundsTextView.setText(res.getQuantityString(R.plurals.rounds, b.getRounds(), b.getRounds()));
				holder.likeCountTextView.setText(res.getQuantityString(R.plurals.likes, b.getLikeCount(), b.getLikeCount()));
				holder.dislikeCountTextView.setText(res.getQuantityString(R.plurals.dislikes, b.getDislikeCount(), b.getDislikeCount()));
                holder.videoViewCountTextView.setText(res.getString(R.string.video_views, b.getVideoViewCount()));
                holder.votingTypeTextView.setText(b.getVoting().getVotingChoice().toLongStyleString(getContext()));
                holder.noMoreBattlesView.setVisibility(View.GONE);
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
                        holder.canVoteTextView.setText("");

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
                        holder.timeUntilVoteEndsTextView.setText(res.getString(R.string.voting_time_left, Video.getTimeUntil(getContext(), b.getVoting().getVotingTimeEnd())));
                    } else if (b.getVoting().getVotingTimeEnd() != null && !b.getVoting().getVotingTimeEnd().after(new Date(System.currentTimeMillis())))
                    {
                        //voting has finished
                        holder.canVoteTextView.setVisibility(View.GONE);
                        holder.timeUntilVoteEndsTextView.setVisibility(View.GONE);

                        holder.challengerResultTextView.setVisibility(View.VISIBLE);
                        holder.challengedResultTextView.setVisibility(View.VISIBLE);
                        holder.challengerVotesTextView.setVisibility(View.VISIBLE);
                        holder.challengedVotesTextView.setVisibility(View.VISIBLE);
                        holder.challengerResultTextView.setText(b.getVoting().getChallengerVotingResult(getContext()));
                        holder.challengedResultTextView.setText(b.getVoting().getChallengedVotingResult(getContext()));
                        holder.challengerVotesTextView.setText(res.getQuantityString(R.plurals.votes, b.getVoting().getVoteChallenger(), b.getVoting().getVoteChallenger()));
                        holder.challengedVotesTextView.setText(res.getQuantityString(R.plurals.votes, b.getVoting().getVoteChallenged(), b.getVoting().getVoteChallenged()));
                    }
                }


                if (position == followingBattlesIDList.size()- 1)
				{

					if (!endOfList)
					{
					    holder.noMoreBattlesView.setVisibility(View.VISIBLE);
					    holder.noMoreBattlesProgressContainer.setVisibility(View.VISIBLE);

                        Log.i(TAG, "Not End of List. Load More Battles");
						//load more
						LoadMoreBattlesCallback loadCallback = new FollowingBattleCache.LoadMoreBattlesCallback() {

							@Override
							public void onMoreBattlesLoaded(List<BattleIDSave> moreBattles) {
                                Log.i(TAG, "More Battles Loaded. Item Count: " + moreBattles.size());
                                holder.noMoreBattlesProgressContainer.setVisibility(View.GONE);
                                holder.noMoreBattlesView.setVisibility(View.GONE);
                                if (moreBattles.size() > 0) {
                                    int currentSize = mBattleIDList.size();
                                    mBattleIDList.addAll(moreBattles);
                                    endOfList = mBattleIDList.size() < FollowingBattleCacheFile.FILE_MAX_CAPACITY;

                                    Log.i(TAG, "Item range inserted: " + position + " + 1 to itemcount of  + " +  moreBattles.size());
                                    mAdapter.notifyItemRangeInserted(position + 1, moreBattles.size());
                                }
                            }

                            @Override
							public void ThereIsNoMoreBattles() {
                                holder.noMoreBattlesTextView.setVisibility(View.VISIBLE);
                                holder.noMoreBattlesProgressContainer.setVisibility(View.GONE);
								endOfList = true;
							}
						};
						FollowingBattleCache.get(getActivity()).loadMoreBattles(getActivity(), loadCallback);
					}
					else
					{
                        holder.noMoreBattlesView.setVisibility(View.VISIBLE);
                        holder.noMoreBattlesTextView.setVisibility(View.VISIBLE);
                        Log.i("FriendBattleList", "End of List. No More Battles");
					}
				}
			}
			else
			{
				holder.mView.setVisibility(View.GONE);
                holder.mView.setLayoutParams(new RecyclerView.LayoutParams(0,0));

			}



		}

		private void setThumbnail(final MyViewHolder holder, final int position, final int battleID, final Battle b) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					//Cancel the previous set thumbnail request, if it exists
					Picasso.get().cancelRequest(holder.thumbnail);
					//Get thumbnail url from cache, if it is not in the cache, get the signed url from server.
					final ThumbnailCacheHelper thumbnailCacheHelper = ThumbnailCacheHelper.get(getActivity());
					String thumbnailSignedUrl = thumbnailCacheHelper.getThumbnailPicOldUrl(b.getBattleId());

					//Log.i(TAG, "Signed Url: " + thumbnailSignedUrl);
					if (thumbnailSignedUrl == null) {
						//thumbnail not in cache. get signed url from server
						Battle.getSignedUrlFromServer(b.getThumbnailServerUrl(), getActivity().getApplicationContext(), new Battle.SignedUrlCallback() {
							@Override
							public void onReceivedSignedUrl(String signedUrl) {
							   // Log.i("FollowingBattle", "old thumbnail signed url not in cache");
								if (getActivity() == null) { return;}
								thumbnailCacheHelper.putSignedUrl(getActivity(), battleID, signedUrl);
								Picasso.get().load(signedUrl).fit().centerCrop().noPlaceholder().error(R.drawable.placeholder1440x750).into(holder.thumbnail, new Callback() {
									@Override
									public void onSuccess() {
										//Log.i("FollowingBattle", "Success picasso");
									}
									@Override
									public void onError(Exception e) {
										//Log.i("FollowingBattle", "On error picasso");
									}
								});

							}
						});
					} else {
						//thumbnail in cache, load from cache
						Picasso.get().load(thumbnailSignedUrl).fit().centerCrop().noPlaceholder().error(R.drawable.placeholder1440x750).networkPolicy(NetworkPolicy.OFFLINE).into(holder.thumbnail, new Callback() {
							@Override
							public void onSuccess() {
								//Log.i("FollowingBattle", "thumbnail received from cache");
							}

							@Override
							public void onError(Exception e) {
								//Log.i("FollowingBattle", "No image in cache");
								//Image not in cache anymore.
								//get new signed Url and load into it
								if (getActivity() != null) {
									Battle.getSignedUrlFromServer(b.getThumbnailServerUrl(), getActivity(), new Battle.SignedUrlCallback() {
										@Override
										public void onReceivedSignedUrl(String signedUrl) {
											if (getActivity() != null) {
												thumbnailCacheHelper.putSignedUrl(getActivity(), battleID, signedUrl);
												Picasso.get().load(signedUrl).fit().centerCrop().noPlaceholder().error(R.drawable.placeholder1440x750).into(holder.thumbnail);
											}
										}
									});
								}
							}
						});

					}

					//Preload next few thumbnails
					int preloadAmount = 10;
					if (position == 0)
					{
						for (int i = 1; i <= preloadAmount && i < followingBattlesIDList.size(); i++)
						{
							Battle bPreload = FollowingBattleCache.get(getActivity()).getFollowingBattle(followingBattlesIDList.get(i).getBattleID());
							if (bPreload != null) {
								preloadThumbnail(thumbnailCacheHelper, bPreload);
							}
						}
					}
					else
					{
						if (position + preloadAmount < followingBattlesIDList.size()) {
							Battle bPreload = FollowingBattleCache.get(getActivity()).getFollowingBattle(followingBattlesIDList.get(position + preloadAmount).getBattleID());
							if (bPreload != null) {
								preloadThumbnail(thumbnailCacheHelper, bPreload);
							}                                }
					}

				}};
			Thread t1 = new Thread(r);
			t1.run();
		}

		@Override
		public int getItemCount() {
			return followingBattlesIDList.size();
		}
	}



	private void preloadThumbnail(final ThumbnailCacheHelper thumbnailCacheHelper, final Battle b)
    {
         String thumbnailSignedUrl = thumbnailCacheHelper.getThumbnailPicOldUrl(b.getBattleId());


        if (thumbnailSignedUrl == null)
        {
            Log.i(TAG, "thumbnail signed url = null");
            //thumbnail not in cache. get signed url from server
            Battle.getSignedUrlFromServer(b.getThumbnailServerUrl(), getActivity().getApplicationContext(), new Battle.SignedUrlCallback() {
                @Override
                public void onReceivedSignedUrl(String signedUrl) {
                	if (getActivity() == null) {return;}

					thumbnailCacheHelper.putSignedUrl(getActivity().getApplicationContext(),b.getBattleId(), signedUrl);
					Picasso.get().load(signedUrl).fetch();
					Log.i(TAG, "signed url received from server. Fetching image");
                }
            });
        }
        else {
            Picasso.get().load(thumbnailSignedUrl).networkPolicy(NetworkPolicy.OFFLINE).fetch(new Callback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "Preloaded from cache with old signed url");
                }

                @Override
                public void onError(Exception e) {
					Log.i(TAG, "On error picasso. image not in cache, get new signed url and load it");
                    //Image not in cache anymore.
                    //get new signed Url and load into it
                    if (getActivity() == null){return;}
                    Battle.getSignedUrlFromServer(b.getThumbnailServerUrl(), getActivity().getApplicationContext(), new Battle.SignedUrlCallback() {
                        @Override
                        public void onReceivedSignedUrl(String signedUrl) {
                        	if (getActivity() != null) {
								thumbnailCacheHelper.putSignedUrl(getActivity(), b.getBattleId(), signedUrl);
								Picasso.get().load(signedUrl).fetch(new Callback() {
									@Override
									public void onSuccess() {
										Log.i(TAG, "Image not in cache, received new signed url. fetched from server");
									}

									@Override
									public void onError(Exception e) {
										Log.i(TAG, "Image not in cache, received new signed url. ERROR fetched from server");
									}

								});
							}}});

                        }
                    });

                }
            };

}












	
	
	
	

