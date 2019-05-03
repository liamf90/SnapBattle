package com.liamfarrell.android.snapbattle.activity.createbattle;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.activity.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CreateBattleResponse;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.CreateBattleRequest;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;

public class VerifyBattleFragment extends Fragment
{
	//Intent extras
    public static final String SNACKBAR_MESSAGE_EXTRA = "com.liamfarrell.android.snapbattle.createbattlefragment.snackbar_message_extra";
	public static final String BATTLE_NAME_EXTRA = "com.liamfarrell.android.snapbattle.createbattlefragment.battle_name_extra";
    public static final String OPPONENT_FACEBOOK_ID_EXTRA = "com.liamfarrell.android.snapbattle.createbattlefragment.facebook_id_extra";

    //Error names
    public static final String battleNameTooLongError = "BATTLE_NAME_TOO_LONG";
    public static final String ROUNDS_WRONG_AMOUNT_ERROR = "ROUNDS_WRONG_AMOUNT";
    public static final String NOT_BEEN_LONG_ENOUGH_ERROR = "NOT_BEEN_LONG_ENOUGH_ERROR";
    public static final String USER_BANNED_ERROR = "USER_BANNED_ERROR";

    private TextView mChosenOpponentTextView;
    private String mChosenOpponentFacebookID;
    private String mChosenOpponentCognitoID;

	private ChooseVotingFragment.VotingChoice mChosenVotingType;
	private ChooseVotingFragment.VotingLength mChosenVotingLength;
    //private ArrayList<Opponent> judgesSelectedList; <<- FUTURE IMPLEMENTATION
	private View mProgressContainer;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)
	{
		View v = inflater.inflate(R.layout.fragment_create_battle, container, false);

        TextView battleNameTextView = v.findViewById(R.id.battleName);
        battleNameTextView.setText(((CreateBattleActivity)getActivity()).getBattleName());

        TextView numberOfRoundsTextView = v.findViewById(R.id.numberOfRounds);
		numberOfRoundsTextView.setText(Integer.toString(((CreateBattleActivity)getActivity()).getRounds()));

		mChosenOpponentTextView = v.findViewById(R.id.chosenOpponent);
		if (((CreateBattleActivity)getActivity()).getOpponent().getUsername() != null){
		mChosenOpponentTextView.setText(((CreateBattleActivity)getActivity()).getOpponent().getUsername());}
		else{
			mChosenOpponentTextView.setText(((CreateBattleActivity)getActivity()).getOpponent().getFacebookName());
		}

		if (((CreateBattleActivity)getActivity()).getOpponent().getCognitoId() != null) {
            mChosenOpponentCognitoID = ((CreateBattleActivity) getActivity()).getOpponent().getCognitoId();
        }
        else {
            mChosenOpponentFacebookID = ((CreateBattleActivity) getActivity()).getOpponent().getFacebookUserId();
        }

		mChosenVotingType = ((CreateBattleActivity)getActivity()).getVotingChoice();
		mChosenVotingLength = ((CreateBattleActivity)getActivity()).getVotingLength();

        TextView chosenVotingTextView = v.findViewById(R.id.chosenVotingTextView);
		chosenVotingTextView.setText(mChosenVotingType.toString());

        mProgressContainer = v.findViewById(R.id.progressContainer);

        TextView votingTimeTextView = v.findViewById(R.id.votingLengthTextView);
        LinearLayout votingLengthLayout = v.findViewById(R.id.votingLengthLinearLayout);

        //If chosen voting type != none, show voting info
		if (mChosenVotingType != ChooseVotingFragment.VotingChoice.NONE)
        {
            votingTimeTextView.setText(mChosenVotingLength.toString(getActivity()));
            if (mChosenVotingType == ChooseVotingFragment.VotingChoice.SELECTED)
            {
            	//TODO FUTURE RELEASE: HAVE A LISTVIEW WITH THE CHOSEN JUDGES
            }
        }
        else
        {
            votingLengthLayout.setVisibility(View.GONE);
        }


        Button sendBattleRequestButton = v.findViewById(R.id.sendBattleRequest);
		sendBattleRequestButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
                mProgressContainer.setVisibility(View.VISIBLE);
                createBattleOnServer();

			}
		});
		setRetainInstance(true);
		return v;
	} 
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		if (resultCode == getActivity().RESULT_OK && requestCode == 250)
		{
            if (data.hasExtra(ChooseOpponentFragment.EXTRA_FACEBOOK_ID))
            {
			    mChosenOpponentFacebookID = data.getStringExtra(ChooseOpponentFragment.EXTRA_FACEBOOK_ID);
            }
            mChosenOpponentTextView.setText(data.getStringExtra(ChooseOpponentFragment.EXTRA_FACEBOOK_NAME));
		}
		else if (resultCode == Activity.RESULT_OK && requestCode == 100)
		{
			//FUTURE IMPLEMENTATION OF SELECTING JUDGES
			/*
			judgesSelectedList = new ArrayList<Opponent>();
			String[] facebookIDArray = data.getStringArrayExtra(SelectJudgesFragment.EXTRA_FACEBOOK_ID_ARRAY);
			String[] facebookNameArray = data.getStringArrayExtra(SelectJudgesFragment.EXTRA_FACEBOOK_NAME_ARRAY);
			for (int x=0; x < facebookIDArray.length; x++)
			{
				judgesSelectedList.add(new Opponent(null, facebookNameArray[x], null, facebookIDArray[x]));
			}
			*/
		}
    }
	

	private void createBattleOnServer() {
		CreateBattleRequest createBattleRequest = new CreateBattleRequest();
		if (mChosenOpponentFacebookID != null)
		{
			createBattleRequest.setChallengedFacebookID(mChosenOpponentFacebookID);
		}
		else if (mChosenOpponentCognitoID != null)
		{
			createBattleRequest.setChallengedCognitoID(mChosenOpponentCognitoID);
		}
		createBattleRequest.setVotingChoice(mChosenVotingType.name());
		if (mChosenVotingType != ChooseVotingFragment.VotingChoice.NONE) {
			createBattleRequest.setVotingLength(mChosenVotingLength.name());
		}
		if (mChosenVotingType == ChooseVotingFragment.VotingChoice.SELECTED)
		{


		}
		createBattleRequest.setNumberOfRounds(((CreateBattleActivity)getActivity()).getRounds());
		createBattleRequest.setBattleName(((CreateBattleActivity)getActivity()).getBattleName());
		new CreateBattleOnServerTask(getActivity(), this).execute(createBattleRequest);

		//TODO SELECTED JUDGES
		//createBattleRequest.selectedJudgesArray = judgesSelectedList;

	}

	private static class CreateBattleOnServerTask extends AsyncTask<CreateBattleRequest, Void, AsyncTaskResult<CreateBattleResponse>>
	{
		private WeakReference<Activity> activityReference;
		private WeakReference<VerifyBattleFragment> fragmentReference;

		CreateBattleOnServerTask(Activity activity, VerifyBattleFragment fragment)
		{
			fragmentReference = new WeakReference<>(fragment);
			activityReference = new WeakReference<>(activity);
		}
		@Override
		protected AsyncTaskResult<CreateBattleResponse> doInBackground(CreateBattleRequest... params) {
		// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
		LambdaInvokerFactory factory = new LambdaInvokerFactory(
				activityReference.get().getApplicationContext(),
				Regions.US_EAST_1,
				FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

		// Create the Lambda proxy object with default Json data binder.
		// You can provide your own data binder by implementing
		// LambdaDataBinder
		final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
		// The Lambda function invocation results in a network call
		// Make sure it is not called from the main thread

				// invoke "echo" method. In case it fails, it will throw a
				// LambdaFunctionException.
				try {
					CreateBattleResponse response =   lambdaFunctionsInterface.CreateBattle(params[0]);
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
			protected void onPostExecute( AsyncTaskResult<CreateBattleResponse> asyncResult) {
				// get a reference to the activity and fragment if it is still there
				VerifyBattleFragment fragment = fragmentReference.get();
				Activity activity = activityReference.get();
				if (fragment == null || fragment.isRemoving()) return;
				if (activity == null || activity.isFinishing()) return;

				fragment.mProgressContainer.setVisibility(View.GONE);
				CreateBattleResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
					new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
					return;
                }

                //Add snackbar message to display in the main activity when battle request is successfully sent.
                Intent data = new Intent();
				//check for errors and display toast if they have occurred
				if (result.getError() != null) {
					if (result.getError().equals(battleNameTooLongError)) {
						//This error should not happen, because the battle length is checked client side
						Toast.makeText(activity, R.string.battle_name_too_long_toast, Toast.LENGTH_SHORT).show();
					} else if (result.getError().equals(ROUNDS_WRONG_AMOUNT_ERROR)) {
						//This error should not happen, because the rounds amount is checked client side
						Toast.makeText(activity, R.string.incorrect_rounds_toast, Toast.LENGTH_SHORT).show();
					} else if (result.getError().equals(NOT_BEEN_LONG_ENOUGH_ERROR)) {
						//This error should not happen, because the rounds amount is checked client side

						Toast.makeText(activity, R.string.not_long_enough_wait_challenge_opponent_toast, Toast.LENGTH_SHORT).show();
					}
					else if (result.getError().equals(USER_BANNED_ERROR))
                    {
                        Date timeBanEnds = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
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
                        Toast.makeText(activity, activity.getString(R.string.banned_toast ,timeBanEnds.toString()), Toast.LENGTH_SHORT).show();
                    }
				}
				else
				{
                    data.putExtra(SNACKBAR_MESSAGE_EXTRA, activity.getString(R.string.battle_request_sent_snackbar_message));
				}
				activity.setResult(Activity.RESULT_OK, data);
				activity.finish();
			}
	}

}
