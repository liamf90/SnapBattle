package com.liamfarrell.android.snapbattle.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.ActivityMainNavigationDrawer;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.activity.startup.ChooseUsernameStartupFragment;
import com.liamfarrell.android.snapbattle.caches.CurrentUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.User;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetProfileResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateNameResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateUsernameResponse;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.UpdateNameRequest;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.UpdateUsernameRequest;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment
{

	private static final String TAG = "ProfileFragment";
	public static final String resultUsernameUpdated = "USERNAME_CHANGED";

	public static final int PICK_IMAGE = 400;

	private EditText usernameEditText, nameEditText;
    private Button changeUsernameConfirmButton;
    private Button changeNameButton;
	private CircleImageView profileImageView;
	private CurrentUsersProfilePicCacheManager mCurrentUsersProfilePicCacheManager;
	private Fragment thisFragment;
	private String username;
	private String name;
	private int profilePicCount;
	private View ProgressContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState)
	{

		View v = inflater.inflate(R.layout.fragment_profile, parent, false);
		thisFragment = this;
        ProgressContainer = v.findViewById(R.id.progressContainer);
        ProgressContainer.setVisibility(View.VISIBLE);
		nameEditText = v.findViewById(R.id.nameEditText);
		usernameEditText = v.findViewById(R.id.usernameEditText);
		usernameEditText.setOnEditorActionListener(new DoneOnEditorActionListener());
        changeUsernameConfirmButton = v.findViewById(R.id.changeUsernameButton);
        changeUsernameConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUsername(usernameEditText.getText().toString());
            }
        });
		changeNameButton = v.findViewById(R.id.changeNameButton);
		changeNameButton.setOnEditorActionListener(new DoneOnEditorActionListener());
		changeNameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                updateName();
			}
		});

        Button changeProfilePictureButton = v.findViewById(R.id.changeProfilePictureButton);
		changeProfilePictureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CropImage.activity().setAspectRatio(200,200).start(getActivity(), thisFragment);
			}
		});
		mCurrentUsersProfilePicCacheManager = new CurrentUsersProfilePicCacheManager(getActivity());
		profileImageView = (CircleImageView)v.findViewById(R.id.profileImageView);
		mCurrentUsersProfilePicCacheManager.getProfilePicSaved(profileImageView);

        addTextChangedListeners();
		getProfileDetails();
		return v;
	}


	private void addTextChangedListeners()
    {
        nameEditText.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


        }

        @Override
        public void afterTextChanged(Editable s) {
            if (name == null || !name.equals(nameEditText.getText().toString())) {
                changeNameButton.setVisibility(View.VISIBLE);
            }
            else
            {
                changeNameButton.setVisibility(View.GONE);
            }

        }
    });

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (username == null || !username.equals(usernameEditText.getText().toString())) {
                    changeUsernameConfirmButton.setVisibility(View.VISIBLE);
                }
                else
                {
                    changeUsernameConfirmButton.setVisibility(View.GONE);
                }

                if (!s.toString().equals(s.toString().toLowerCase()))
                {
                    usernameEditText.setText(s.toString().toLowerCase());
                    usernameEditText.setSelection(usernameEditText.getText().toString().length());
                }

            }

        });


    }

	private void getProfileDetails() {
	    new GetProfileDetailsTask(getActivity(), this).execute();
    }

    private static class GetProfileDetailsTask extends AsyncTask<Void, Void, AsyncTaskResult<GetProfileResponse>> {
        private WeakReference<Activity> activityReference;
        private WeakReference<ProfileFragment> fragmentReference;

        GetProfileDetailsTask(Activity activity, ProfileFragment fragment) {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<GetProfileResponse> doInBackground(Void... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
            try {
                GetProfileResponse response = lambdaFunctionsInterface.GetProfile();
                return new AsyncTaskResult<GetProfileResponse>(response);

            } catch (LambdaFunctionException lfe) {
                Log.i("ERROR", lfe.getDetails());
                Log.i("ERROR", lfe.getStackTrace().toString());
                lfe.printStackTrace();

                return new AsyncTaskResult<GetProfileResponse>(lfe);
            } catch (AmazonServiceException ase) {
                // invalid credentials, incorrect AWS signature, etc
                Log.i("ERROR", ase.getErrorMessage());
                return new AsyncTaskResult<GetProfileResponse>(ase);
            } catch (AmazonClientException ace) {
                // Network issue
                Log.i("ERROR", ace.toString());
                return new AsyncTaskResult<GetProfileResponse>(ace);
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<GetProfileResponse> asyncResult) {
            // get a reference to the activity and fragment if it is still there
            ProfileFragment fragment = fragmentReference.get();
            Activity activity = activityReference.get();
            if (fragment == null || fragment.isRemoving()) return;
            if (activity == null || activity.isFinishing()) return;

            GetProfileResponse result = asyncResult.getResult();
            if (asyncResult.getError() != null) {
                new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
                return;

            }

            User user = result.getSqlResult().get(0);
            if (user.getUsername() != null) {
                fragment.username = user.getUsername();
                fragment.usernameEditText.append(user.getUsername());
            }
            if (user.getFacebookName() != null) {
                fragment.name = user.getFacebookName();
                fragment.nameEditText.append(user.getFacebookName());
            }
            fragment.profilePicCount = user.getProfilePicCount();
            fragment.ProgressContainer.setVisibility(View.GONE);


        }
    }


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	   if (requestCode == PICK_IMAGE)
	   {
		   Uri chosenPhotoUri = data.getData();
		   CropImage.activity(chosenPhotoUri).setAspectRatio(200,200).start(getActivity(), this);
	   }

	   else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
	   {
	       CropImage.ActivityResult result = CropImage.getActivityResult(data);
	       if (resultCode == Activity.RESULT_OK) {
	           Uri resultUri = result.getUri();
			   profileImageView.setImageURI(resultUri);

               //Upload to s3.
			   mCurrentUsersProfilePicCacheManager.updateProfilePicture(resultUri.getPath(), profilePicCount, new CurrentUsersProfilePicCacheManager.ProfilePicCopiedCallback() {
                   @Override
                   public void onProfilePicCopied(Context context) {
                       ((ActivityMainNavigationDrawer)getActivity()).updateImageView();
                   }
               });


	       } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
	           Toast.makeText(getActivity(), R.string.generic_error_toast, Toast.LENGTH_SHORT).show();
	       }
	   }

	}

    public void copy(File src, File dst) throws IOException
    {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }



    private void updateUsername(String newUsername) {
        UpdateUsernameRequest request = new UpdateUsernameRequest();
        request.setUsername(newUsername);
        new UpdateUsernameTask(getActivity(), this).execute(request);
    }

    private static class UpdateUsernameTask extends AsyncTask<UpdateUsernameRequest, Void, AsyncTaskResult<UpdateUsernameResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ProfileFragment> fragmentReference;

        UpdateUsernameTask(Activity activity, ProfileFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<UpdateUsernameResponse> doInBackground(UpdateUsernameRequest... params) {

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

                try {
                    UpdateUsernameResponse response =  lambdaFunctionsInterface.UpdateUsername(params[0]);
                    return new AsyncTaskResult<>(response);

				} catch (LambdaFunctionException lfe) {
					Log.i("ERROR", lfe.getDetails());
					Log.i("ERROR",lfe.getStackTrace().toString());
					lfe.printStackTrace();

                    return new AsyncTaskResult<UpdateUsernameResponse>(lfe);
				}
				catch (AmazonServiceException ase) {
					// invalid credentials, incorrect AWS signature, etc
					Log.i("ERROR", ase.getErrorMessage());

                    return new AsyncTaskResult<UpdateUsernameResponse>(ase);
				}
				catch (AmazonClientException ace) {
					// Network issue
					Log.i("ERROR", ace.toString());

                    return new AsyncTaskResult<UpdateUsernameResponse>(ace);
				}
            }

            @Override
            protected void onPostExecute( AsyncTaskResult<UpdateUsernameResponse> asyncResult) {
                // get a reference to the activity and fragment if it is still there
                ProfileFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                UpdateUsernameResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
                    return;

                }


                if (result != null) {
					if (result.getResult().equals(resultUsernameUpdated)) {
                        fragment.changeUsernameConfirmButton.setVisibility(View.GONE);
						SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
						sharedPrefs.edit().putString(FacebookLoginFragment.USERNAME_SHAREDPREFS, fragment.usernameEditText.getText().toString()).commit();
						//update textview in main activity showing username
						((ActivityMainNavigationDrawer)activity).loadUsernameAndName();
						Log.i(TAG, "Username updated");
                        final CoordinatorLayout coordinatorLayout =activity.findViewById(R.id.coordinatorLayoutMain);
                        Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, R.string.username_updated_snackbar_message, Snackbar.LENGTH_SHORT);
                        snackbar.show();


					}
					else if (result.getResult().equals(ChooseUsernameStartupFragment.usernamameNotValidErorrCode))
					{

						Toast.makeText(activity, R.string.username_wrong_characters_toast, Toast.LENGTH_SHORT ).show();
					}
					else if(result.getResult().equals(ChooseUsernameStartupFragment.usernameTooLongErrorCode))
					{
						Toast.makeText(activity, R.string.username_too_long_toast, Toast.LENGTH_SHORT ).show();
					}
					else if (result.getResult().equals(ChooseUsernameStartupFragment.usernameUsernameAlreadyExistsErrorCode))
					{
						Toast.makeText(activity, R.string.username_already_exists_toast, Toast.LENGTH_SHORT ).show();
					}
				}
            }
    }

    private void updateName() {
        String newName = nameEditText.getText().toString();
        UpdateNameRequest request = new UpdateNameRequest();
        request.setName(newName);
        new UpdateNameTask(getActivity(), this).execute(request);

    }
    private static class UpdateNameTask extends AsyncTask<UpdateNameRequest, Void,  AsyncTaskResult<UpdateNameResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ProfileFragment> fragmentReference;

        UpdateNameTask(Activity activity, ProfileFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }
        @Override
        protected  AsyncTaskResult<UpdateNameResponse> doInBackground(UpdateNameRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
                try {
                    UpdateNameResponse response =  lambdaFunctionsInterface.UpdateName(params[0]);
                    return new AsyncTaskResult<UpdateNameResponse>(response);

                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<UpdateNameResponse>(lfe);
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
            protected void onPostExecute( AsyncTaskResult<UpdateNameResponse> asyncResult) {
                // get a reference to the activity and fragment if it is still there
                ProfileFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;


                UpdateNameResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,null);
                    return;


                }


                //Name Updated
                if (result != null) {
                    if (result.getResult().equals(UpdateNameResponse.getResultNameUpdated())) {
                        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) activity.findViewById(R.id.coordinatorLayoutMain);
                        Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, R.string.name_updated_snackbar_message, Snackbar.LENGTH_SHORT);
                        snackbar.show();

                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                        sharedPref.edit().putString(FacebookLoginFragment.NAME_SHAREDPREFS, fragment.nameEditText.getText().toString()).commit();

                        ((ActivityMainNavigationDrawer) activity).loadUsernameAndName();
                    }
                    else if(result.getResult().equals(UpdateNameResponse.getNameTooLongErrorCode()))
                    {
                        Toast.makeText(activity, R.string.name_too_long_toast, Toast.LENGTH_SHORT ).show();
                    }
                }
            }
    }

    class DoneOnEditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        }
    }












}


