package com.liamfarrell.android.snapbattle.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.ListFragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BattleRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.VideoSubmittedRequest;
import com.liamfarrell.android.snapbattle.mvvm_ui.ViewCommentsFragment;
import com.liamfarrell.android.snapbattle.ui.createbattle.ChooseVotingFragment;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.Video;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseBattle;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.VideoSubmittedResponse;
import com.liamfarrell.android.snapbattle.service.MyGcmListenerService;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ViewBattleFragment extends ListFragment
{
	public static final String TAG = "ViewBattleFragment";
	public static final String BATTLE_ID_EXTRA = "com.liamfarrell.android.snapbattle.battle_id_extra";
	public static final String VIDEO_ID_EXTRA = "com.liamfarrell.android.snapbattle.videoidextra";
	public static final String VIDEO_FILEPATH_EXTRA = "com.liamfarrell.android.snapbattle.videofilepathextra";
	public static final String USER_BANNED_ERROR = "USER_BANNED_ERROR";

	private static final int WRITE_EXTERNAL_REQUEST_CODE = 30;


	private Battle mBattle;
	private TextView BattleNameTextView,  RoundsTextView, challengerUsernameTextView, challengedUsernameTextView, vsTextView, statusTextView;
	private ArrayList<Video> videos;
	private String battleID;
	private ImageButton saveToDeviceButton;
	private Button ViewCommentsButton;
    private ImageButton play_whole_battleButton;
	private View mProgressContainer;
	private ConstraintLayout mCompletedConstraintLayout;
	private TextView mLikeCountTextView, mDislikeCountTextView;
	private ProgressBar mFinalVideoTranscodingProgressBar;

	//voting textviews
	private TextView challengerResultTextView, challengedResultTextView, challengerVotesTextView, challengedVotesTextView, votingTypeTextView, votingLengthTextView, timeUntilVoteEndsTextView;
	private ConstraintLayout votingLayout;


    @Override
    public void onDestroy() {
        Log.i(TAG, "On Destroyed");
        destroyRegister();
        super.onDestroy();
    }


    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "broadcast intent received");
            if(intent.getStringExtra(MyGcmListenerService.TYPE_INTENT_EXTRA).equals(MyGcmListenerService.TYPE_VIDEO_SUBMITTED)
                    && intent.getIntExtra(MyGcmListenerService.BATTLE_ID_INTENT_EXTRA, -1) == mBattle.getBattleId()) {
                Log.i(TAG, "video submitted");
                // TODO If we receive this, we're visible, so cancel the notification
                //setResultCode(Activity.RESULT_CANCELED);
                populateList();
                }
                else if (intent.getStringExtra(MyGcmListenerService.TYPE_INTENT_EXTRA).equals(MyGcmListenerService.UPLOAD_FULL_VIDEO)
                    && intent.getIntExtra(MyGcmListenerService.BATTLE_ID_INTENT_EXTRA, -1) == mBattle.getBattleId()) {
                Log.i(TAG, "final video submitted");
                populateList();

            }
        }
    };

    private void registerReceiver(){
        //register receivers to update the list when a video is submitted (if fragment are still visible)
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyGcmListenerService.ACTION_VIDEO_SUBMITTED);
        filter.addAction(MyGcmListenerService.ACTION_FULL_VIDEO_FINISHED);

        getActivity().registerReceiver(mOnShowNotification, filter, MyGcmListenerService.PERM_PRIVATE, null);

    }

    private void destroyRegister() {
        getActivity().unregisterReceiver(mOnShowNotification);
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
        Log.i(TAG, "On Create");
		super.onCreate(savedInstanceState);
		videos = new ArrayList<Video>();
		BattleVideoAdapter adapter = new BattleVideoAdapter(videos);
		setListAdapter(adapter);
		setRetainInstance(true);

        battleID = getActivity().getIntent().getStringExtra(BATTLE_ID_EXTRA);
		Log.i(TAG, "BattleId: " + battleID);
        registerReceiver();
		populateList();

		
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) 
	{

		View v = inflater.inflate(R.layout.fragment_view_battle, parent, false);

		mProgressContainer = v.findViewById(R.id.current_list_progressContainer);
		BattleNameTextView = v.findViewById(R.id.battle_name_TextView);
		RoundsTextView = v.findViewById(R.id.battle_rounds_TextView);
        vsTextView = v.findViewById(R.id.vs_TextView);
        mCompletedConstraintLayout = v.findViewById(R.id.completed_battle_constraint_layout);
		play_whole_battleButton = v.findViewById(R.id.play_whole_battleButton);
		play_whole_battleButton.setOnClickListener(new View.OnClickListener() 
		{	
			@Override
			public void onClick(View v) 
			{
				//check if the file is stored, otherwise stream it
                File file = new File( getActivity().getFilesDir().getAbsolutePath() + "/" + mBattle.getFinalVideoFilename());
				if (file.exists())
				{
					//Play the file
					Intent intent = new Intent(getActivity(), VideoViewActivity.class);
					intent.putExtra(VIDEO_FILEPATH_EXTRA, file.getAbsolutePath());
					intent.putExtra(VideoViewFragment.VIDEO_TYPE_EXTRA,VideoViewFragment.VIDEO_TYPE_LOCAL);
					intent.putExtra(VideoViewFragment.VIDEO_ROTATION_LOCK_EXTRA, mBattle.getOrientationLock());
					startActivity(intent);
				}
				else
				{
					//Stream the file
					String filepath = mBattle.getServerFinalVideoUrl(FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId());
					Battle.SignedUrlCallback callback = new Battle.SignedUrlCallback() {
						@Override
						public void onReceivedSignedUrl(String signedUrl) {
                            Log.i(TAG, signedUrl);
                            mProgressContainer.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(getActivity(), VideoViewActivity.class);
                            intent.putExtra(ViewBattleFragment.VIDEO_FILEPATH_EXTRA, signedUrl);
                            intent.putExtra(VideoViewFragment.VIDEO_TYPE_EXTRA, VideoViewFragment.VIDEO_TYPE_STREAM);
                            intent.putExtra(VideoViewFragment.VIDEO_ROTATION_LOCK_EXTRA, mBattle.getOrientationLock());
                            startActivity(intent);

						}
					};
					Battle.getSignedUrlFromServer(filepath, getActivity(),callback);

                    mProgressContainer.setVisibility(View.VISIBLE);

				}

                //createMovie  create = new createMovie(false);
                //create.start();

				
			}
		});
		saveToDeviceButton = v.findViewById(R.id.save_to_device_button);
		saveToDeviceButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
                if (isStoragePermissionGranted())
                {
                    saveFileToDevice();
                }

			}
		});
		votingLayout = v.findViewById(R.id.votingLayout);

		mLikeCountTextView = v.findViewById(R.id.likeCountTextView);
		mDislikeCountTextView = v.findViewById(R.id.dislikeCountTextView);
        ViewCommentsButton = v.findViewById(R.id.viewCommentsButton);
        challengerUsernameTextView = v.findViewById(R.id.challenger_name_TextView);
        challengerUsernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent  i = new Intent(getActivity(), UsersBattlesActivity.class);
                i.putExtra(UsersBattlesActivity.EXTRA_COGNITO_ID, mBattle.getChallengerCognitoID());
                startActivity(i);
            }
        });
        challengedUsernameTextView = v.findViewById(R.id.challenged_name_TextView);
        challengedUsernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent  i = new Intent(getActivity(), UsersBattlesActivity.class);
                i.putExtra(UsersBattlesActivity.EXTRA_COGNITO_ID, mBattle.getChallengedCognitoID());
                startActivity(i);
            }
        });
		statusTextView = v.findViewById(R.id.statusTextView);
		mFinalVideoTranscodingProgressBar = v.findViewById(R.id.finalTranscodingProgressBar);

        //Voting Text Views
		challengerResultTextView = v.findViewById(R.id.challenger_result_TextView);
		challengedResultTextView = v.findViewById(R.id.challenged_result_TextView);
		challengerVotesTextView = v.findViewById(R.id.challenger_votes_TextView);
		challengedVotesTextView = v.findViewById(R.id.challenged_votes_TextView);
		votingTypeTextView = v.findViewById(R.id.voting_type_TextView);
		timeUntilVoteEndsTextView = v.findViewById(R.id.time_until_vote_endsTextView);
		votingLengthTextView = v.findViewById(R.id.voting_length_TextView);


		
		return v;
	}

	

	

	
	@Override
	public void onPause()
	{
		super.onPause();
		if (mProgressContainer != null)
		{
			mProgressContainer.setVisibility(View.INVISIBLE);
		}
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if (resultCode== Activity.RESULT_CANCELED)
		{
			//this is called when an error has occurred. video file may be deleted. reload list to disable play button
			((BattleVideoAdapter)getListAdapter()).notifyDataSetChanged();
		}
		if (requestCode == 100)
		{
	    	if (data == null)
	    	{
	    		return;
	    	}


	    	if (data.getStringExtra(VideoRecordActivity.EXTRA_ORIENTATION_LOCK) != null)
	    	{
	    		if (data.getStringExtra(VideoRecordActivity.EXTRA_ORIENTATION_LOCK).equals(Battle.ORIENTATION_LOCK_PORTRAIT))
	    		{
	    			mBattle.setOrientationLock(Battle.ORIENTATION_LOCK_PORTRAIT);
	    		}
	    		if (data.getStringExtra(VideoRecordActivity.EXTRA_ORIENTATION_LOCK).equals(Battle.ORIENTATION_LOCK_LANDSCAPE))
	    		{
					mBattle.setOrientationLock(Battle.ORIENTATION_LOCK_LANDSCAPE);
	    		}
	    	}

	    	
	    	populateList();

		}
		
    }

    private void saveFileToDevice()
    {
        final String filepath = mBattle.getServerFinalVideoUrl(FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId());
        Battle.SignedUrlCallback callback = new Battle.SignedUrlCallback() {
            @Override
            public void onReceivedSignedUrl(String signedUrl) {
                Log.i(TAG, signedUrl);

                DownloadFileFromURL downloadFileFromURL = new DownloadFileFromURL(signedUrl, mBattle.getFinalVideoFilename());
                downloadFileFromURL.execute();
            }
        };
        mBattle.getSignedUrlFromServer(filepath, getActivity(),callback);

    }

	
	private class BattleVideoAdapter extends ArrayAdapter<Video> 
	{
		public BattleVideoAdapter(ArrayList<Video> videos) {
			super(getActivity(), 0, videos);
		}
		
		

		@NonNull
		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{

			// Configure the view for this video
			final Video video = getItem(position);
			
			// If we wern't given a view inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(
                        R.layout.list_item_video_user, null);
            }
			LinearLayout videoLayout = convertView.findViewById(R.id.videoLinearLayout);


            final float scale = getResources().getDisplayMetrics().density;

            if (video.isCurrentUser(FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId()))
            {
                if (video.getVideoNumber() <= mBattle.getVideosUploaded() + 1) {
                    videoLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.video_view_user));

                }
                else
                {
                    videoLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.video_view_future));

                }

                int LeftMargin_in_dp = 32;
                int Leftmargin_in_px = (int) (LeftMargin_in_dp * scale + 0.5f);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)videoLayout.getLayoutParams();
                layoutParams.leftMargin = Leftmargin_in_px;
                videoLayout.setLayoutParams(layoutParams);

                int RightMargin_in_dp = 8;
                int Rightmargin_in_px = (int) (RightMargin_in_dp * scale + 0.5f);
                layoutParams.rightMargin = Rightmargin_in_px;
                videoLayout.setLayoutParams(layoutParams);
            }
            else
            {
                if (video.getVideoNumber() <= mBattle.getVideosUploaded() + 1) {
                    videoLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.video_view_opponent));
                }
                else
                {
                    videoLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.video_view_future));

                }

                int LeftMargin_in_dp = 8;
                int Leftmargin_in_px = (int) (LeftMargin_in_dp * scale + 0.5f);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)videoLayout.getLayoutParams();
                layoutParams.leftMargin = Leftmargin_in_px;
                videoLayout.setLayoutParams(layoutParams);

                int RightMargin_in_dp = 32;
				layoutParams.rightMargin = (int) (RightMargin_in_dp * scale + 0.5f);
                videoLayout.setLayoutParams(layoutParams);
            }



			

			TextView roundNumberTextView = (TextView) convertView
					.findViewById(R.id.round_number);
			if ((video.getVideoNumber() % 2)!= 0)
			{
				roundNumberTextView.setVisibility(View.VISIBLE);
				roundNumberTextView.setText(getActivity().getResources().getString(R.string.round_number,  video.getRoundNumber()));
				if (video.getVideoNumber() > mBattle.getVideosUploaded() + 1)
                {
                    roundNumberTextView.setTextColor(getActivity().getResources().getColor(R.color.dark_gray));
                }
                else
                {
                    roundNumberTextView.setTextColor(getResources().getColor(R.color.secondary_text_dark));
                }
			}
			else
			{
				roundNumberTextView.setVisibility(View.GONE);
			}



			
			final TextView name1TextView = (TextView)convertView.findViewById(R.id.name1);

			
			switch(video.getVideoStatus(mBattle.getVideosUploaded(), FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId())) {
                case RECEIVED:
                    name1TextView.setText(getActivity().getResources().getString(R.string.received, video.getTimeSinceUploaded()));
                    break;
                case SENT:
                    name1TextView.setText(getActivity().getResources().getString(R.string.sent, video.getTimeSinceUploaded()));
                    break;
                case YOUR_TURN:
                    name1TextView.setText(R.string.your_turn);
                    break;
                case OPPONENT_TURN:
                    name1TextView.setText(R.string.opponent_turn);
                    break;
                case OPPONENT_FUTURE:
                    name1TextView.setText("");
                    break;
                case YOUR_FUTURE:
                    name1TextView.setText("");
                    break;
                case ERROR:
                    name1TextView.setText(R.string.error);
                    break;
                default:
                    name1TextView.setText(R.string.error);
                    break;
            }



			Button playButton1 = (Button)convertView.findViewById(R.id.playButton1);
			if (!video.displayPlayButton(getActivity()))
			{
				playButton1.setVisibility(View.INVISIBLE);
			}
			else
			{
				playButton1.setVisibility(View.VISIBLE);
			}
			
			playButton1.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					
					final String filepath = getActivity().getFilesDir().getAbsolutePath() +  "/" + video.getVideoFilename();
					File file = new File(filepath);
					
					if(!file.exists()) 
					{
                        playWithCloudFrontSignedUrl(video.getVideoFilename());
                    }
					else
					{
						//Go to view video
						Intent intent = new Intent(getActivity(), VideoViewActivity.class);
						intent.putExtra(VIDEO_FILEPATH_EXTRA, filepath);
						startActivity(intent);
					}
				}
			});
			
			Button recordButton1 = convertView.findViewById(R.id.recordButton1);

			if (!video.displayRecordButton(mBattle.getVideosUploaded(), FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId()))
			{
				Log.i(TAG, "MAKING VIEW BUTTON INVISIBLE FOR ROUND: " + video.getRoundNumber());
				recordButton1.setVisibility(View.INVISIBLE);
				
			}
			else
			{
				recordButton1.setVisibility(View.VISIBLE);
			}
			
			
			recordButton1.setOnClickListener(new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{

					Dexter.withActivity(getActivity())
							.withPermissions(
									android.Manifest.permission.CAMERA,
									android.Manifest.permission.RECORD_AUDIO
									)
							.withListener(new MultiplePermissionsListener() {
								@Override
								public void onPermissionsChecked(MultiplePermissionsReport report) {
									// check if all permissions are granted
									if (report.areAllPermissionsGranted()) {
										Intent  i = new Intent(getActivity(), VideoRecordActivity.class);
										i.putExtra(VIDEO_ID_EXTRA, video.getVideoID());
										if (video.getVideoNumber() == 1)
										{
											i.putExtra(VideoRecordActivity.EXTRA_ORIENTATION_LOCK, Battle.ORIENTATION_LOCK_UNDEFINED);
										}
										else
										{

											i.putExtra(VideoRecordActivity.EXTRA_ORIENTATION_LOCK, mBattle.getOrientationLock() );
										}
										startActivityForResult(i, 100);
									}

									// check for permanent denial of any permission
									if (report.isAnyPermissionPermanentlyDenied()) {
										// permission is denied permenantly, navigate user to app settings
										// check for permanent denial of permission

											showSettingsDialog();

									}
								}

								@Override
								public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
									token.continuePermissionRequest();
								}
							})
                            .
                                    withErrorListener(new PermissionRequestErrorListener() {
                                        @Override
                                        public void onError(DexterError error) {
                                            Toast.makeText(getApplicationContext(), R.string.generic_error_toast, Toast.LENGTH_SHORT).show();
                                        }
                                    })
							.onSameThread()
							.check();


						
				}
			});
			
			final Button submitButton1 = (Button)convertView.findViewById(R.id.submitButton1);
			if (!video.displaySubmitButton(getActivity(), mBattle.getVideosUploaded(), FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId()))
			{
				submitButton1.setVisibility(View.INVISIBLE);
			}
			else
			{
				submitButton1.setVisibility(View.VISIBLE);
			}
			
			submitButton1.setOnClickListener(new View.OnClickListener() 
			{
				
				@Override
				public void onClick(View v) 
				{
					name1TextView.setText(R.string.uploading);
					submitButton1.setEnabled(false);		
					//TODO get opponent facebook ID NOT Cognito ID
					//new uploadVideo().execute(video.getVideoFilename(), null);
					uploadVideo(video.getVideoFilename(), mBattle.getOpponentCognitoID(FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId()), Integer.toString(video.getVideoID()));
		
				}
			});			
			return convertView;

		}

	}
	
	private void populateList() {
		BattleRequest battle = new BattleRequest();
		battle.setBattleID(battleID);
		new PopulateListTask(getActivity(), this).execute(battle);
	}

	private static class PopulateListTask extends AsyncTask<BattleRequest, Void, AsyncTaskResult<ResponseBattle>>
	{
		private WeakReference<Activity> activityReference;
		private WeakReference<ViewBattleFragment> fragmentReference;

		PopulateListTask(Activity activity, ViewBattleFragment fragment)
		{
			fragmentReference = new WeakReference<>(fragment);
			activityReference = new WeakReference<>(activity);
		}

		@Override
		protected AsyncTaskResult<ResponseBattle> doInBackground(BattleRequest... params) {
		// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
		LambdaInvokerFactory factory = new LambdaInvokerFactory(
				activityReference.get().getApplicationContext(),
				Regions.US_EAST_1,
				FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

			final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class, new CustomLambdaDataBinder());

				try {
					ResponseBattle response= lambdaFunctionsInterface.getBattleFunction(params[0]);
					return new AsyncTaskResult<>(response);
				} catch (LambdaFunctionException lfe) {
					Log.i("ERROR", lfe.getDetails());
					Log.i("ERROR",lfe.getStackTrace().toString());
					lfe.printStackTrace();

					return new AsyncTaskResult<>(lfe);				}
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
			protected void onPostExecute( AsyncTaskResult<ResponseBattle> asyncResult) {
				// get a reference to the activity and fragment if it is still there
				ViewBattleFragment fragment = fragmentReference.get();
				Activity activity = activityReference.get();
				if (fragment == null || fragment.isRemoving()) return;
				if (activity == null || activity.isFinishing()) return;
				ResponseBattle result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
                    return;
                }
				//String currentUserCognitoID = BattleFragment.getCredentialsProvider(getActivity()).getIdentityId();
				//mBattle = new BattlePOJO(result.sql_result, currentUserCognitoID);
				fragment.setValues(result);
				fragment.mProgressContainer.setVisibility(View.GONE);
				//Toast.makeText(getActivity(), "Error: " + result.getSqlResult().getBattleid()), Toast.LENGTH_LONG).show();
				//Toast.makeText(getActivity(), "Battleid: " + result.sql_result.get(0).battleid, Toast.LENGTH_SHORT).show();
			}
	}

    /**
     * Set the values
     *
     * @param result
     */
	private void setValues(ResponseBattle result)
    {
        mBattle = result.getSqlResult();
        ViewCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewCommentsActivity.class);
                intent.putExtra(ViewCommentsFragment.Companion.getEXTRA_BATTLEID(), mBattle.getBattleId());
                startActivity(intent);

            }
        });

        //Set main text views
		Resources res = getResources();
		String battleName =  mBattle.getBattleName();
		BattleNameTextView.setText(res.getString(R.string.battle_name, battleName));
        challengerUsernameTextView.setText(mBattle.getChallengerUsername());
        challengedUsernameTextView.setText(mBattle.getChallengedUsername());
        vsTextView.setVisibility(View.VISIBLE);
		RoundsTextView.setText(res.getQuantityString(R.plurals.rounds, mBattle.getRounds(), mBattle.getRounds()));
        votingTypeTextView.setText(mBattle.getVoting().getVotingChoice().toString());
        if (mBattle.getVoting().getVotingChoice() != ChooseVotingFragment.VotingChoice.NONE)
		{
			votingLengthTextView.setVisibility(View.VISIBLE);
			votingLengthTextView.setText(getString(R.string.voting_length_title_included, mBattle.getVoting().getVotingLength().toString(getActivity())));
		}

        if (!mBattle.getIsBattleAccepted()) {
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.setText(getResources().getString(R.string.declined_battle_status_message, mBattle.getChallengedUsername()));

        }

        if (mBattle.isBattleDone())
        {

			statusTextView.setVisibility(View.VISIBLE);
            if (!mBattle.getIsFinalVideoReady()) {
                statusTextView.setText(R.string.final_video_transcoding_status_message);
                mFinalVideoTranscodingProgressBar.setVisibility(View.VISIBLE);
            }
            else{
                mCompletedConstraintLayout.setVisibility(View.VISIBLE);
                mFinalVideoTranscodingProgressBar.setVisibility(View.GONE);
                statusTextView.setText(mBattle.getCompletedBattleStatus());

                mLikeCountTextView.setText(Integer.toString(mBattle.getLikeCount()));
                mDislikeCountTextView.setText(Integer.toString(mBattle.getDislikeCount()));
                ViewCommentsButton.setText(res.getQuantityString(R.plurals.view_comments_button, mBattle.getCommentCount(), mBattle.getCommentCount()));

                play_whole_battleButton.setVisibility(View.VISIBLE);
                saveToDeviceButton.setVisibility(View.VISIBLE);




                //Set voting fields if battle has voting

                if (mBattle.getVoting().getVotingChoice() == ChooseVotingFragment.VotingChoice.NONE)
                {
                    votingLayout.setVisibility(View.GONE);
                }
                else {
                    if ((mBattle.getVoting().getVotingTimeEnd() == null))
                    {
                        //voting hasnt begun yet
                        challengerResultTextView.setVisibility(View.GONE);
                        challengedResultTextView.setVisibility(View.GONE);
                        challengerVotesTextView.setVisibility(View.GONE);
                        challengedVotesTextView.setVisibility(View.GONE);
                        timeUntilVoteEndsTextView.setVisibility(View.GONE);
                    }
                    if (mBattle.getVoting().getVotingTimeEnd() != null && mBattle.getVoting().getVotingTimeEnd().after(new Date(System.currentTimeMillis()))) {
                        //voting is still going
                        challengerResultTextView.setVisibility(View.GONE);
                        challengedResultTextView.setVisibility(View.GONE);
                        challengerVotesTextView.setVisibility(View.GONE);
                        challengedVotesTextView.setVisibility(View.GONE);
                        timeUntilVoteEndsTextView.setText(res.getString(R.string.voting_time_left, Video.getTimeUntil(mBattle.getVoting().getVotingTimeEnd())));
                    } else if (mBattle.getVoting().getVotingTimeEnd() != null && !mBattle.getVoting().getVotingTimeEnd().after(new Date(System.currentTimeMillis())))
                    {
                        //voting has finished
                        timeUntilVoteEndsTextView.setVisibility(View.GONE);
                        challengerResultTextView.setText(mBattle.getVoting().getChallengerVotingResult());
                        challengedResultTextView.setText(mBattle.getVoting().getChallengedVotingResult());
                        challengerVotesTextView.setText(res.getQuantityString(R.plurals.votes, mBattle.getVoting().getVoteChallenger(), mBattle.getVoting().getVoteChallenger()));
                        challengedVotesTextView.setText(res.getQuantityString(R.plurals.votes,mBattle.getVoting().getVoteChallenged(), mBattle.getVoting().getVoteChallenged()));
                    }
                }
            }








        }
        else
        {
            mCompletedConstraintLayout.setVisibility(View.GONE);
        }


        //Get array of videos
        videos.clear();
        if (mBattle.getIsBattleAccepted()) {
			videos.addAll(mBattle.getVideos());
		}

        //reset adapter
        ((BattleVideoAdapter)getListAdapter()).notifyDataSetChanged();
        mProgressContainer.setVisibility(View.INVISIBLE);

    }



	/**
	 * Showing Alert Dialog with Settings option
	 * Navigates user to app settings
	 * NOTE: Keep proper title and messageTextView depending on your app
	 */
	private void showSettingsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.app_permission_dialog_title);
		builder.setMessage(R.string.app_need_permissions_not_given);
		builder.setPositiveButton(R.string.app_permission_goto_settings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				openSettings();
			}
		});
		builder.setNegativeButton(R.string.app_permission_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();

	}

	// navigating user to app settings
	private void openSettings() {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
		intent.setData(uri);
		startActivityForResult(intent, 101);
	}


	private static void videoSubmitted(int videoID, String videoRotationLock, Battle battle, WeakReference<Context> contextReference, WeakReference<ViewBattleFragment> fragmentReference) {
		VideoSubmittedRequest request = new VideoSubmittedRequest();
		if (battle.getVideosUploaded() == 0)
		{
			request.setVideoRotationLock(videoRotationLock);
		}

		request.setBattleID(battle.getBattleId());
		request.setVideoID(videoID);
		new VideoSubmittedTask(contextReference, fragmentReference).execute(request);
	}

	private static class VideoSubmittedTask extends  AsyncTask<VideoSubmittedRequest, Void,AsyncTaskResult<VideoSubmittedResponse>>
	{
		private WeakReference<Context> contextReference;
        private WeakReference<ViewBattleFragment> fragmentReference;

		VideoSubmittedTask(WeakReference<Context> contextReference, WeakReference<ViewBattleFragment> fragmentReference)
		{
            this.contextReference = contextReference;
            this.fragmentReference = fragmentReference;
		}

		@Override
		protected AsyncTaskResult<VideoSubmittedResponse> doInBackground(VideoSubmittedRequest... params) {


            // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
            LambdaInvokerFactory factory = new LambdaInvokerFactory(
                    contextReference.get().getApplicationContext(),
                    Regions.US_EAST_1,
                    FacebookLoginFragment.getCredentialsProvider(contextReference.get()));
		    final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class, new CustomLambdaDataBinder());
				try {
                    VideoSubmittedResponse response= lambdaFunctionsInterface.VideoSubmitted(params[0]);
                    return new AsyncTaskResult<>(response);
				} catch (LambdaFunctionException lfe) {
					Log.i("LAMBDA ERROR", lfe.getDetails());
					Log.i("LAMBDA ERROR",lfe.getStackTrace().toString());
					lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
				}
				catch (AmazonServiceException ase) {
					// invalid credentials, incorrect AWS signature, etc
					Log.i("LAMBDA ERROR", ase.getErrorMessage());
                    return new AsyncTaskResult<>(ase);
				}
				catch (AmazonClientException ace) {
					// Network issue
					Log.i("LAMBDA ERROR", ace.toString());
                    return new AsyncTaskResult<>(ace);
				}
			}

			@Override
			protected void onPostExecute(AsyncTaskResult<VideoSubmittedResponse> asyncResult) {
                // get a reference to the activity and fragment if it is still there
                ViewBattleFragment fragment = fragmentReference.get();
                Context context = contextReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (context == null) return;

                VideoSubmittedResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), context, null);
                    return;
                }


				if (result.getError() != null && result.getError().equals(USER_BANNED_ERROR))
                {
                    Date timeBanEnds = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    if (result.getTime_ban_ends() != null)
                    {
                        try {
                            timeBanEnds = sdf.parse(result.getTime_ban_ends());
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    Toast.makeText(context, context.getResources().getString(R.string.banned_toast, timeBanEnds.toString()), Toast.LENGTH_SHORT).show();
                }
                else if (result.getSuccess() == 1){


                	//delete video from phone.
					fragment.mBattle.getVideos().get(fragment.mBattle.getVideosUploaded()).deleteVideo(context);

					Log.i("ViewBattleFragment", "Success = 1");
					//video Submitted
					//if last video of battle. create movie + thumbnail then upload movie + thumbnail
					fragment.mBattle.setVideosUploaded(fragment.mBattle.getVideosUploaded() + 1);
					if (result.getVoting_time_end() != null)
                    {
						fragment.mBattle.getVoting().setVotingTimeEnd(result.getVoting_time_end());
                    }
					// video has been submitted and database updated.
					// Update the list to reflect this.
                    if (fragment.getActivity() != null) {
                        fragment.populateList();
                    }
				}
			}
	}

	
	
	private void uploadVideo(String filename, String opponentCognitoID, String videoID)
    {
	    new UploadVideoTask(getApplicationContext(), this).execute(filename, opponentCognitoID, videoID);
    }
    private static class UploadVideoTask extends AsyncTask<String, Void, Void>
    {
        private WeakReference<Context> contextReference;
        private WeakReference<ViewBattleFragment> fragmentReference;

        UploadVideoTask(Context context, ViewBattleFragment fragment) {
            fragmentReference = new WeakReference<>(fragment);
            contextReference = new WeakReference<>(context);
        }

			@Override
			protected  Void doInBackground(final String... params) {

					final AmazonS3 s3 = new AmazonS3Client(FacebookLoginFragment.getCredentialsProvider(contextReference.get()));
					final Battle b = fragmentReference.get().mBattle;
					final String bucketName = "snapbattlevideos";
                    String CognitoID =  FacebookLoginFragment.getCredentialsProvider(contextReference.get()).getIdentityId();
                    File file = new File(contextReference.get().getFilesDir().getAbsolutePath() + "/" + params[0]);
                    final String fileName = params[0];
                    final String key = CognitoID + "/" + file.getName();

                    final String cognitoIDOpponent = params[1];
                    final String videoID = params[2];
                    final String CognitoIDUser = FacebookLoginFragment.getCredentialsProvider(contextReference.get()).getIdentityId();
                     final String orientationLock = Video.orientationHintToLock(Integer.parseInt(Video.getVideoRotation(contextReference.get(), b.getVideos().get(b.getVideosUploaded()))));



                try {
						
				            System.out.println("Uploading a new object to S3 from a file\n");
				            PutObjectRequest por = new PutObjectRequest( bucketName, key, file);
				            por.setGeneralProgressListener(new com.amazonaws.event.ProgressListener(){

								@Override
								public void progressChanged(
										com.amazonaws.event.ProgressEvent arg0) {
									if (arg0.getEventCode() == com.amazonaws.event.ProgressEvent.COMPLETED_EVENT_CODE)
									{
										//VideoPOJO has been uploaded.
										//copy video to opponents bucket

										String dstKey =  cognitoIDOpponent + "/" + fileName;
										String srcKey = CognitoIDUser + "/" + fileName;
										CopyObjectRequest cor = new CopyObjectRequest(bucketName, srcKey, bucketName, dstKey);
										s3.copyObject(cor);
										videoSubmitted(Integer.parseInt(videoID), orientationLock, b, contextReference, fragmentReference);
									}
									
								}});
				            s3.putObject(por);
				            

				         } catch (AmazonServiceException ase)
                        {
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (contextReference.get() != null) {
                                        Toast.makeText(contextReference.get(), R.string.server_error_toast, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


				        } catch (AmazonClientException ace) 
				        {
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (contextReference.get() != null) {
                                        Toast.makeText(contextReference.get(), R.string.server_error_toast, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
				        }
				       
					 	return null;
			}


	}


    private void playWithCloudFrontSignedUrl(String s3Path)
    {
        String url = "https://djbj27vmux1mw.cloudfront.net/" + FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId() + "/" + s3Path;
        Battle.getSignedUrlFromServer(url, getActivity(), new Battle.SignedUrlCallback() {
            @Override
            public void onReceivedSignedUrl(String signedUrl) {
                Intent intent = new Intent(getActivity(), VideoViewActivity.class);
                intent.putExtra(VIDEO_FILEPATH_EXTRA,signedUrl);
                Log.i(TAG, "URL: " + signedUrl);
                startActivity(intent);

            }
        });
    }

	

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                requestPermissions( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_REQUEST_CODE);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "On request permissions result. request code: ");
        if(requestCode == WRITE_EXTERNAL_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            saveFileToDevice();
        }
    }



    //this class is used to download the battle video before saving it to device
	class DownloadFileFromURL extends AsyncTask<String, String, String> {

		private String mInputUrl;
		private String mSaveFilename;

		public DownloadFileFromURL(String inputUrl, String saveFilename) {
			mInputUrl = inputUrl;
			mSaveFilename = saveFilename;
		}
		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected String doInBackground(String... inputStrings) {
			int count;
			try {
                String savedPathFile = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) ;
                File dir = new File(savedPathFile);
                File saveFile = new File(savedPathFile, mSaveFilename);
                //Make sure the movies directory exists
				dir.mkdirs();

                Log.i(TAG, "Downloading in background");
                Log.i(TAG, "To: " + saveFile);
				URL url = new URL(mInputUrl);
				URLConnection conection = url.openConnection();
				conection.connect();

				// this will be useful so that you can show a tipical 0-100%
				// progress bar
				int lenghtOfFile = conection.getContentLength();

				// download the file
				InputStream input = new BufferedInputStream(url.openStream(),
						8192);

				// Output stream
				OutputStream output = new FileOutputStream(saveFile);

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					// After this onProgressUpdate will be called
					publishProgress("" + (int) ((total * 100) / lenghtOfFile));

					// writing data to file
					output.write(data, 0, count);

                    Log.i(TAG, "Writing to file");
				}

				// flushing output
				output.flush();

				// closing streams
				output.close();
				input.close();

				//refresh the snapbattle folder so gallery instantly updates with the saved file, allowing easy uploading to facebook.
				updateGalleryWithFile(saveFile);


			} catch (Exception e) {
				Log.e("Error: ", e.getMessage());
			}

			return null;
		}




		@Override
		protected void onPostExecute(String file_url) {
            Log.i(TAG, "Finished writing to file");
            Toast.makeText(getActivity(), getResources().getString(R.string.video_saved_to_device), Toast.LENGTH_SHORT).show();

		}


    }

    private void updateGalleryWithFile(File out)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Intent mediaScanIntent = new Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri contentUri = Uri.fromFile(out); //out is your file you saved/deleted/moved/copied
			mediaScanIntent.setData(contentUri);
			getActivity().sendBroadcast(mediaScanIntent);
		} else {
			getActivity().sendBroadcast(new Intent(
					Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://"
							+ Environment.getExternalStorageDirectory())));
		}
	}
}

	
	
	






	

	

