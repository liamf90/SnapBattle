package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetBattlesByNameRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetBattlesByNameResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;

import java.lang.ref.WeakReference;

public class ViewBattlesFromNameFragment extends BattleCompletedListFragment
{


    @Override
    protected void getBattles() {
        GetBattlesByNameRequest request = new GetBattlesByNameRequest();
        request.setBattleName(getActivity().getIntent().getStringExtra(ViewBattlesFromNameActivity.EXTRA_BATTLE_NAME));
        request.setFetchLimit(BATTLES_PER_FETCH);
        if (mBattles.size() > 0)
        {
            request.setAfterBattleID(mBattles.get(mBattles.size() - 1).getBattleId());
        }
        else
        {
            request.setAfterBattleID(-1);
        }
        new GetBattlesTask(getActivity(), this).execute(request);

    }

    @Override
    protected View.OnClickListener getBattleOnClickListener(final Battle b)
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filepath = b.getServerFinalVideoUrl(b.getChallengerCognitoID());
                Intent intent = new Intent(getActivity(), FullBattleVideoPlayerActivity.class);
                intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_BATTLEID, b.getBattleId());
                intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_FILE_VIDEO_PATH, filepath);
                intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGER_USERNAME, b.getChallengerUsername());
                intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGED_USERNAME, b.getChallengedUsername());
                startActivityForResult(intent, FullBattleVideoPlayerFragment.VOTE_DONE_REQUEST_CODE);
            }
        };

    }


    private static class GetBattlesTask extends AsyncTask<GetBattlesByNameRequest, Void, AsyncTaskResult<GetBattlesByNameResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<ViewBattlesFromNameFragment> fragmentReference;

            GetBattlesTask(Activity activity, ViewBattlesFromNameFragment fragment) {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }



            @Override
        protected  AsyncTaskResult<GetBattlesByNameResponse> doInBackground(GetBattlesByNameRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                IdentityManager.getDefaultIdentityManager().getCredentialsProvider());

        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class, new CustomLambdaDataBinder());




                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    GetBattlesByNameResponse response = lambdaFunctionsInterface.GetBattlesByName(params[0]);
                    return new AsyncTaskResult<>(response);
                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);                }
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
            protected void onPostExecute(AsyncTaskResult<GetBattlesByNameResponse> asyncResult)  {
                // get a reference to the callbacks and fragment if it is still there
                ViewBattlesFromNameFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                GetBattlesByNameResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,fragment.mProgressContainer);
                    return;
                }

                if (result == null) {
                    Toast.makeText(activity, R.string.no_result_toast, Toast.LENGTH_LONG).show();
                    return;

                }
                int oldLastIndex = fragment.mBattles.size();
                for (Battle bat: result.getSqlResult())
                {
                    if (bat.isDeleted() == false) {
                       fragment.mBattles.add(bat);
                    }
                }


                fragment. mCurrentBattleAdapter.notifyItemRangeInserted(oldLastIndex, fragment.mBattles.size());

                if (result.getSqlResult().size() != BATTLES_PER_FETCH)
                {
                    fragment.allBattlesFetched = true;
                }
                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
            }
    }





}
