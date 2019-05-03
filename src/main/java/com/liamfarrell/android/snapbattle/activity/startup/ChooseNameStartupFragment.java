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
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateNameResponse;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.UpdateNameRequest;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;

import java.lang.ref.WeakReference;

/**
 * Created by Liam on 1/01/2018.
 */

public class ChooseNameStartupFragment extends Fragment
{
    private EditText mNameEditText;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_choose_name_startup, container, false);
        mNameEditText = v.findViewById(R.id.nameEditText);
        //only show next button if name entered length > 0
        mNameEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0)
                {
                    ((StartupActivity)getActivity()).setEnableNextButton(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0)
                {
                    ((StartupActivity)getActivity()).setEnableNextButton(false);
                }

            }
        });

        try
        {
            //Use the facebook name of the user as suggested name
            mNameEditText.setText(((StartupActivity)getActivity()).getName());
        }
        catch (NullPointerException e){
            ((StartupActivity)getActivity()).setEnableNextButton(false);
        }


        return v;

    }





    public String getNameEditText()
    {
        return mNameEditText.getText().toString();
    }

    public void updateName() {
        ((StartupActivity) getActivity()).showProgressSpinner();
        String newName = mNameEditText.getText().toString();
        UpdateNameRequest request = new UpdateNameRequest();
        request.setName(newName);
        new UpdateNameTask(getActivity(), this).execute(request);
    }

    private static class UpdateNameTask extends AsyncTask<UpdateNameRequest, Void, AsyncTaskResult<UpdateNameResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ChooseNameStartupFragment> fragmentReference;

        UpdateNameTask(Activity activity, ChooseNameStartupFragment fragment)
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
                    UpdateNameResponse response = lambdaFunctionsInterface.UpdateName(params[0]);
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
            protected void onPostExecute(AsyncTaskResult<UpdateNameResponse> asyncResult) {
                // get a reference to the activity and fragment if it is still there
                ChooseNameStartupFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                ((StartupActivity)activity).hideProgressSpiner();
                UpdateNameResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,null);
                    return;

                }
                //Name Updated
                if (result != null) {
                    if (result.getResult().equals(UpdateNameResponse.getResultNameUpdated())) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                        sharedPref.edit().putString(FacebookLoginFragment.NAME_SHAREDPREFS, fragment.mNameEditText.getText().toString()).commit();

                        ((StartupActivity) activity).nextFragment();
                    }
                    else if(result.getResult().equals(UpdateNameResponse.getNameTooLongErrorCode()))
                    {
                        Toast.makeText(activity, R.string.name_too_long_toast, Toast.LENGTH_SHORT ).show();
                    }
                }
            }

    }





}
