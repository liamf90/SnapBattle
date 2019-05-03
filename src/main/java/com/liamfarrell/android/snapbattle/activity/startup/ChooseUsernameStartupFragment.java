package com.liamfarrell.android.snapbattle.activity.startup;



import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.activity.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateUsernameResponse;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.UpdateUsernameRequest;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;

import java.lang.ref.WeakReference;

/**
 * Created by Liam on 1/01/2018.
 */

public class ChooseUsernameStartupFragment extends Fragment
{
    public final static String  usernamameNotValidErorrCode = "USERNAME_NOT_VALID";
    public final static String  usernameTooLongErrorCode = "USERNAME_TOO_LONG";
    public final static String  usernameUsernameAlreadyExistsErrorCode = "USERNAME_ALREADY_EXISTS";

    private EditText mUsernameEditText;
    private String mDefaultUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mDefaultUsername = ((StartupActivity)getActivity()).getUsername();
        View v = inflater.inflate(R.layout.fragment_choose_username_startup, container, false);
        mUsernameEditText = v.findViewById(R.id.usernameEditText);
        //username will be premade on server, user can update it if they want
        mUsernameEditText.setText(mDefaultUsername);


        //next button only enabled if length of username > 0
        mUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    ((StartupActivity)getActivity()).setEnableNextButton(true);
                }
                else{
                    ((StartupActivity)getActivity()).setEnableNextButton(false);
                }

                //make username all lowercase
                if (!editable.toString().equals(editable.toString().toLowerCase()))
                {
                    mUsernameEditText.setText(editable.toString().toLowerCase());
                    mUsernameEditText.setSelection(mUsernameEditText.getText().toString().length());
                }
            }
        });

        return v;

    }


    public void updateUsername() {
        String newUsername = mUsernameEditText.getText().toString();
        if (mDefaultUsername.equals(newUsername)) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            sharedPref.edit().putString(FacebookLoginFragment.USERNAME_SHAREDPREFS,newUsername).commit();
            ((StartupActivity) getActivity()).finishStartup();
        } else {
            UpdateUsernameRequest request = new UpdateUsernameRequest();
            request.setUsername(newUsername);
            ((StartupActivity) getActivity()).showProgressSpinner();
            new UpdateUsernameTask(getActivity(), this).execute(request);
        }
    }


        private static class UpdateUsernameTask extends AsyncTask<UpdateUsernameRequest, Void, AsyncTaskResult<UpdateUsernameResponse>>
        {
        private WeakReference<Activity> activityReference;
        private WeakReference<ChooseUsernameStartupFragment> fragmentReference;

        UpdateUsernameTask(Activity activity, ChooseUsernameStartupFragment fragment)
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
         final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);

                try {
                    UpdateUsernameResponse response = lambdaFunctionsInterface.UpdateUsername(params[0]);
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
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<UpdateUsernameResponse> asyncResult) {
                // get a reference to the activity and fragment if it is still there
                ChooseUsernameStartupFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                ((StartupActivity) activity).hideProgressSpiner();
                UpdateUsernameResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null) {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,null);
                    return;
                }

                //Username Updated
                if (result != null) {
                    if (result.getResult().equals(UpdateUsernameResponse.getResultUsernameUpdated())) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                        sharedPref.edit().putString(FacebookLoginFragment.USERNAME_SHAREDPREFS, fragment.mUsernameEditText.getText().toString()).commit();

                        ((StartupActivity) activity).finishStartup();
                    } else if (result.getResult().equals(ChooseUsernameStartupFragment.usernamameNotValidErorrCode)) {

                        Toast.makeText(activity, R.string.username_wrong_characters_toast, Toast.LENGTH_SHORT).show();
                    } else if (result.getResult().equals(ChooseUsernameStartupFragment.usernamameNotValidErorrCode)) {
                        Toast.makeText(activity, R.string.username_too_long_toast, Toast.LENGTH_SHORT).show();
                    } else if (result.getResult().equals(ChooseUsernameStartupFragment.usernamameNotValidErorrCode)) {
                        Toast.makeText(activity, R.string.username_already_exists_toast, Toast.LENGTH_SHORT).show();
                    }
                }


            }

    }





}
