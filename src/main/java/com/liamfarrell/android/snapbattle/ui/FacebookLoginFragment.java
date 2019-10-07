package com.liamfarrell.android.snapbattle.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.liamfarrell.android.snapbattle.ActivityMainNavigationDrawer;
import com.liamfarrell.android.snapbattle.caches.AllBattlesFeedCache;
import com.liamfarrell.android.snapbattle.caches.FollowingBattleCache;
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache;
import com.liamfarrell.android.snapbattle.caches.NotificationCache;
import com.liamfarrell.android.snapbattle.caches.OtherUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.caches.ThumbnailCacheHelper;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CreateUserRequest;
import com.liamfarrell.android.snapbattle.ui.startup.StartupActivity;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CreateUserResponse;
import com.liamfarrell.android.snapbattle.service.RegistrationIntentService;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class FacebookLoginFragment extends Fragment
{
    public static final String USER_ALREADY_EXISTS_RESULT = "USER_ALREADY_EXISTS";
    public static final String USER_ADDED_RESULT = "NEW_USER_ADDED";

    private static final String TAG ="FacebookLoginFragment";

    public static final String USERNAME_SHAREDPREFS = "username";
	public static final String NAME_SHAREDPREFS = "facebook_name";
	public static final String COGNITO_ID_SHAREDPREFS = "cognito_id";
	private static Context sApplicationContext;
    private AccessTokenTracker mAccessTokenTracker;
    private boolean mIsLoggedIn;
	private View mProgressContainer;
	private static CognitoCachingCredentialsProvider sCredentialsProvider;
    private com.amazonaws.auth.IdentityChangedListener mListener;
    CallbackManager mCallbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);






        sApplicationContext = getActivity().getApplicationContext();
        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken)
            {

                Log.i(TAG, "Access Token changed");
                Log.i(TAG, "Old Access Token: " + oldAccessToken);
                Log.i(TAG, "New Access Token: " + currentAccessToken);
                if (currentAccessToken != null) {
                    //User has logged in

                    //Get User Info
                    /* make the API call */
                    GraphRequest request = GraphRequest.newMeRequest(
                            AccessToken.getCurrentAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(
                                        JSONObject object,
                                        GraphResponse response) {
                                    try {

                                        Log.i(TAG, "Name: " + object.getString("name"));
                                        //Save username to sharedprefs

                                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(sApplicationContext);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString(NAME_SHAREDPREFS, object.getString("name"));
                                        editor.commit();
                                        Log.i(TAG, "Contains Cognito SHARED: " + sharedPref.contains(FacebookLoginFragment.COGNITO_ID_SHAREDPREFS));

                                        //register facebook with cognito
                                        registerFacebookWithCognito(sApplicationContext);

                                    } catch (JSONException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "name");
                    request.setParameters(parameters);
                    request.executeAsync();
                }
                else
				{
				    //user has logged out
                    FacebookLoginFragment.getCredentialsProvider(getActivity()).clear();
                    //Clear Shared Preferences
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(sApplicationContext);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.remove(NAME_SHAREDPREFS);
                    editor.remove(COGNITO_ID_SHAREDPREFS);
                    editor.commit();
                    mIsLoggedIn = false;

				}

            }
        };


    }



	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{

		super.onActivityCreated(savedInstanceState);


        Log.i(TAG, "On Activity created. Access Token: " + AccessToken.getCurrentAccessToken());
		if (AccessToken.getCurrentAccessToken() != null) {

            try {
                mIsLoggedIn = true;

                //Make sure caches are all closed before advancing
                AllBattlesFeedCache.closeCache();
                FollowingBattleCache.closeCache();
                FollowingUserCache.closeCache();
                OtherUsersProfilePicCacheManager.closeCache();
                NotificationCache.closeCache();
                ThumbnailCacheHelper.closeCache();


                Log.i(TAG, "On Activity created. CurrentAccessToken DOES NOT EQUAL NULL");
                //check that the app has cognito id cached. if not get it from the server before advancing


                FacebookLoginFragment.getCredentialsProvider(getActivity()).getIdentityId();
                Log.i(TAG, "STRAIGHT THROUGH");
                Intent i = new Intent(getActivity(), ActivityMainNavigationDrawer.class);
                startActivity(i);
                getActivity().finish();


            } catch (android.os.NetworkOnMainThreadException e) {
                //No cognito id set, get the id in backround
                Log.i(TAG, "IDENTITY ID NOT IN CACHE. RETRIEVING FROM SERVER");
                mProgressContainer.setVisibility(View.VISIBLE);
                goToMainActivity();


            }
        }
        else
        {
            mIsLoggedIn = false;
        }
		//Wait for user to press login button}
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)
	{
        Log.i(TAG, "On Create View");
		View view = inflater.inflate(R.layout.fragment_facebook_login, container, false);
		mProgressContainer = view.findViewById(R.id.loginProgressContainer);
		mProgressContainer.setVisibility(View.GONE);
		mCallbackManager = CallbackManager.Factory.create();
		setUpCognitoCallback();
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
		loginButton.setReadPermissions("user_friends", "public_profile");


		// If using in a fragment
		loginButton.setFragment(this);
		// Other app specific specialization

		// Callback registration
		loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess( LoginResult loginResult)
			{
				//new updateCredentialsAsync().execute();
                mProgressContainer.setVisibility(View.VISIBLE);
				Log.i(TAG, "LoginResult: " + loginResult.toString());
			}

			@Override
			public void onCancel() {
				// App code
				Log.i(TAG, "CANCELLED ");
			}

			@Override
			public void onError(FacebookException exception) {
				// App code
				Log.i(TAG, "ERROR1: " + exception.toString());
			}
		});


		return view;
	}




	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		 super.onActivityResult(requestCode, resultCode, data);
		mCallbackManager.onActivityResult(requestCode, resultCode, data);
	}

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mAccessTokenTracker.stopTracking();
        FacebookLoginFragment.getCredentialsProvider(getActivity()).unregisterIdentityChangedListener(mListener);
    }







	private void createUserServer(final Context context) {
        CreateUserRequest request = new CreateUserRequest();
        request.setFacebookID(AccessToken.getCurrentAccessToken().getUserId());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        request.setFacebookName(sharedPref.getString(FacebookLoginFragment.NAME_SHAREDPREFS, ""));

        new CreateUserServerTask(getActivity(), this).execute(request);

    }
    private static class CreateUserServerTask extends AsyncTask<CreateUserRequest, Void, AsyncTaskResult<CreateUserResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<FacebookLoginFragment> fragmentReference;

        CreateUserServerTask(Activity activity, FacebookLoginFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<CreateUserResponse> doInBackground(CreateUserRequest... params) {
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
                    CreateUserResponse response = lambdaFunctionsInterface.createUser(params[0]);
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

			@SuppressLint("ApplySharedPref")
            @Override
			protected void onPostExecute(AsyncTaskResult<CreateUserResponse> asyncResult) {
                // get a reference to the callbacks if it is still there
                FacebookLoginFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                CreateUserResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity, fragment.mProgressContainer);
                    return;
                }


                Log.i(TAG, "Time to register with GCM");
				Intent intent = new Intent(activity, RegistrationIntentService.class);
                activity.startService(intent);

                if (result.getUserExists().equals(USER_ALREADY_EXISTS_RESULT))
                {
                    Log.i(TAG,"LOGIN USERNAME: " + result.getUsername());
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                    sharedPref.edit().putString(FacebookLoginFragment.NAME_SHAREDPREFS, result.getName()).commit();
                    sharedPref.edit().putString(FacebookLoginFragment.USERNAME_SHAREDPREFS, result.getUsername()).commit();

                    Intent i = new Intent(activity, ActivityMainNavigationDrawer.class);
                    activity.startActivity(i);
                    activity.finish();
                }
                else if (result.getUserExists().equals(USER_ADDED_RESULT))
                {
                    Log.i(TAG,"LOGIN NAME: " + result.getName());
                    Intent i = new Intent(activity, StartupActivity.class);
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                    i.putExtra(StartupActivity.EXTRA_USERNAME, result.getUsername());
                    i.putExtra(StartupActivity.EXTRA_NAME, sharedPref.getString(FacebookLoginFragment.NAME_SHAREDPREFS, ""));
                    activity.startActivity(i);
                    activity.finish();

                }

			}
	}

	private void goToMainActivity()
    {
        new GoToMainActivityTask(getActivity()).execute();
    }


    private static class GoToMainActivityTask extends AsyncTask<Void, Void, String> {
        private WeakReference<Activity> activityReference;
        GoToMainActivityTask(Activity activity)
        {
            activityReference = new WeakReference<>(activity);
        }
        @Override
        protected String doInBackground(Void... params) {
            return FacebookLoginFragment.getCredentialsProvider(activityReference.get()).getIdentityId();
        }
        @Override
        protected void onPostExecute(String result) {
            // get a reference to the callbacks if it is still there
            Activity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            Intent i = new Intent(activity, ActivityMainNavigationDrawer.class);
            activity.startActivity(i);
            activity.finish();
        }
    }



	private  static void registerFacebookWithCognito(final Context context)
	{


                Log.i(TAG, "Token credentials updated");
                if (AccessToken.getCurrentAccessToken() != null)
                {

                    Log.i(TAG, "AccessToken does not = null");
                    Map<String, String> logins = new HashMap<String, String>();
                    logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
                    FacebookLoginFragment.getCredentialsProvider(context).setLogins(logins);
                    new AsyncTask<Void, Void, Void>() {
                     @Override
                        protected Void doInBackground(Void... params) {
                         Log.i(TAG, "Refreshing credentials provider");
                         FacebookLoginFragment.getCredentialsProvider(context).refresh();
                         return null;
                     }}.execute();

                }
                else
                {
                    /*
                    Log.i(TAG, "AccessToken does == null. Clearing credentials provider");
                    FacebookLoginFragment.getCredentialsProvider(context).clear();
                    //Clear Shared Preferences
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(sApplicationContext);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.remove(NAME_SHAREDPREFS);
                    editor.remove(COGNITO_ID_SHAREDPREFS);
                    editor.commit();
                    */

                }



	}

	private void setUpCognitoCallback()
	{
        mListener = new com.amazonaws.auth.IdentityChangedListener(){

            //COGNITO Changed Listener
            @Override
            public void identityChanged(String oldIdentityId, String newIdentityId) {
                //Create Listener

                //TODO if oldIdentityId != null. Delete it from the server
                Log.i(TAG, "IDENTITY CHANGED: " + newIdentityId);
                if (newIdentityId != null)
                {


                    //Add Cognito ID to shared prefs
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(sApplicationContext);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(FacebookLoginFragment.COGNITO_ID_SHAREDPREFS, newIdentityId);
                    editor.commit();
                    Log.i(TAG, "Contains FBName SHARED: " + sharedPref.contains(FacebookLoginFragment.NAME_SHAREDPREFS));
                    Log.i(TAG, "Logged In: " + sharedPref.contains(FacebookLoginFragment.NAME_SHAREDPREFS));
                    if (sharedPref.contains(FacebookLoginFragment.NAME_SHAREDPREFS) && !mIsLoggedIn)
                    {
                        createUserServer(sApplicationContext);

                    }
                    else
                    {
                        Intent i = new Intent(getActivity(), ActivityMainNavigationDrawer.class);
                        startActivity(i);
                        getActivity().finish();
                    }
                }

            }};
		    FacebookLoginFragment.getCredentialsProvider(getActivity()).registerIdentityChangedListener(mListener);
	}


	public static CognitoCachingCredentialsProvider getCredentialsProvider(final Context context) throws NetworkOnMainThreadException
	{
		//IF CREDENTIALS PROVIDER IS NULL MAKE A NEW ONE

        throw new Error("Cannot access this");
//		if (sCredentialsProvider == null)
//		{
//            Log.i(TAG, "Creating the credentials provider");
//			sCredentialsProvider = new CognitoCachingCredentialsProvider(
//					context,    /* get the context for the current callbacks */
//					"us-east-1:e6478f31-2dbe-4ad8-aadd-b4964691350c",    /* Identity Pool ID */
//					Regions.US_EAST_1          /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
//			);
//            if (!sCredentialsProvider.getLogins().containsKey("graph.facebook.com"))
//            {
//                Log.i(TAG, "No facebook credentials hooked up to cognito");
//            }
//
//            if (AccessToken.getCurrentAccessToken() != null)
//            {
//                Log.i(TAG, "Access token != null");
//                Map<String, String> logins = new HashMap<String, String>();
//                logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
//                sCredentialsProvider.setLogins(logins);
//            }
//		}
//
//		return sCredentialsProvider;
	}
}
