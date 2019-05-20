package com.liamfarrell.android.snapbattle.caches;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.User;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateProfilePictureRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetProfileResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;

/**
 * Created by Liam on 10/01/2018.
 * Deals with managing the users profile picture. Functions include uploading new profile picture, checking for updates, retrieving profile picture
 * from the server and saving to the device.
 */

public class CurrentUsersProfilePicCacheManager {
    private Context mContext;


    public interface ProfilePicCopiedCallback
    {
        void onProfilePicCopied(Context context);
    }
    public CurrentUsersProfilePicCacheManager(Context context)
    {
        mContext = context;

    }



    private void updateProfilePicCountMySql(final int uploadedProfilePicCount) {
        UpdateProfilePictureRequest request = new UpdateProfilePictureRequest();
        request.setProfilePicCountUploaded(uploadedProfilePicCount);
        new UpdateProfilePicCountTask(mContext, this, uploadedProfilePicCount).execute(request);
    }

    private static class UpdateProfilePicCountTask extends AsyncTask<UpdateProfilePictureRequest, Void, AsyncTaskResult<DefaultResponse>>
    {
        private WeakReference<Context> contextReference;
        private WeakReference<CurrentUsersProfilePicCacheManager> classReference;
        int uploadedProfilePicCount;

        UpdateProfilePicCountTask(Context context, CurrentUsersProfilePicCacheManager thisClass,int uploadedProfilePicCount)
        {
            contextReference = new WeakReference<>(context);
            classReference = new WeakReference<>(thisClass);
            this.uploadedProfilePicCount = uploadedProfilePicCount;
        }
        @Override
        protected  AsyncTaskResult<DefaultResponse> doInBackground(UpdateProfilePictureRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                contextReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(contextReference.get()));

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);



        // The Lambda function invocation results in a network call
        // Make sure it is not called from the main thread


                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    DefaultResponse response =  lambdaFunctionsInterface.updateProfilePicture(params[0]);
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
            protected void onPostExecute(AsyncTaskResult<DefaultResponse> asyncResult)
            { CurrentUsersProfilePicCacheManager thisClass = classReference.get();
                Context context = contextReference.get();
                if (thisClass == null) return;
                if (context == null) return;

                if (asyncResult.getError() != null)
                {
                    if (asyncResult.getError() instanceof AmazonClientException)
                    {

                        return;
                    }
                    else if (asyncResult.getError() instanceof AmazonServiceException || asyncResult.getError() instanceof LambdaFunctionException)
                    {
                        return;
                    }

                }

                classReference.get().updateProfilePicCountSharedPrefs( uploadedProfilePicCount);

                //ProfilePOJO Pic Count updated
                //update the profile pic count
            }

    }


    private String getProfilePicturePathS3(Context context)
    {
        try {
            String filename = FacebookLoginFragment.getCredentialsProvider(mContext).getIdentityId() + "/" + FacebookLoginFragment.getCredentialsProvider(context).getIdentityId() + "-" + getProfilePicCountSharedPrefs(context) + "-ProfilePic.png";
            return filename;
        } catch (com.amazonaws.services.cognitoidentity.model.NotAuthorizedException e)
        {
            return "";
        }

    }

    private String getNextProfilePicturePathS3(Context context)
    {
        int nextProfilePicCount = getProfilePicCountSharedPrefs(context) + 1;
        String path = FacebookLoginFragment.getCredentialsProvider(mContext).getIdentityId() + "/" + FacebookLoginFragment.getCredentialsProvider(context).getIdentityId() + "-" +  nextProfilePicCount + "-ProfilePic.png";
        return path;
    }


    public static String getProfilePictureSavePath(Context context)
    {
        return context.getFilesDir().getAbsolutePath() + "/" + FacebookLoginFragment.getCredentialsProvider(context).getCachedIdentityId() + "-ProfilePic.png";

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





    public void getProfilePicSaved(ImageView imageView)
    {
        File profilePic = new File(getProfilePictureSavePath(mContext));
        if (profilePic.exists())
        {
            Uri uriPic = Uri.parse(getProfilePictureSavePath(mContext));
            imageView.setImageURI(uriPic);
        }
        else
        {
            imageView.setImageResource(R.drawable.default_profile_pic);
        }

    }

    public void checkForProfilePicUpdate(ImageView imageView)
    {
        new getProfilePicCount(imageView).execute();
    }

    public void updateProfilePicture(String newPhotoUrl, int profilePicCountCurrent,  ProfilePicCopiedCallback profilePicCopiedCallbackCallback)
    {
        new uploadProfilePicture(profilePicCountCurrent, profilePicCopiedCallbackCallback).execute(newPhotoUrl);
    }


    private class getProfilePicture extends AsyncTask<Integer, Void, Void>
    {
        private ImageView mImageView;
        private getProfilePicture(ImageView imageView)
        {
            mImageView = imageView;
        }

        @Override
        protected Void doInBackground(final Integer... params) {
            final File file = new File(getProfilePictureSavePath(mContext));
            String s3Path = getProfilePicturePathS3(mContext);

            //download file
            AmazonS3 s3 = new AmazonS3Client(FacebookLoginFragment.getCredentialsProvider(mContext));
            String bucketName  = "snapbattlevideos";

            GetObjectRequest gor = new GetObjectRequest(bucketName, s3Path);

            gor.setGeneralProgressListener(new com.amazonaws.event.ProgressListener(){

                @Override
                public void progressChanged(
                        com.amazonaws.event.ProgressEvent arg0)
                {
                    if (arg0.getEventCode() == com.amazonaws.event.ProgressEvent.COMPLETED_EVENT_CODE)
                    {
                        Handler mainHandler = new Handler(mContext.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {mImageView.setImageURI(Uri.fromFile(file));} // This is your code
                        };
                        mainHandler.post(myRunnable);
                    }
                }});
            try
            {
                s3.getObject(gor, file);
            }
            catch (AmazonServiceException ase)
            {
                System.out.println("Caught an AmazonServiceException, which " +
                        "means your request made it " +
                        "to Amazon S3, but was rejected with an error response" +
                        " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace)
            {
                System.out.println("Caught an AmazonClientException, which " +
                        "means the client encountered " +
                        "an internal error while trying to " +
                        "communicate with S3, " +
                        "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            }
            return null;
        }
    }

    private void updateProfilePicCountSharedPrefs(int profilePicCount)
    {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FacebookLoginFragment.getCredentialsProvider(mContext).getIdentityId(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("PROFILE_PIC_COUNT", profilePicCount);
        editor.commit();
    }

    private static int getProfilePicCountSharedPrefs(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(FacebookLoginFragment.getCredentialsProvider(context).getIdentityId(), Context.MODE_PRIVATE);
        int profilePicCount = sharedPref.getInt("PROFILE_PIC_COUNT", 0);
        return profilePicCount;
    }






    private class uploadProfilePicture extends AsyncTask<String, Void, Void>
    {
        private int mProfilePicCountCurrent;
        private ProfilePicCopiedCallback mProfilePicCopiedCallbackCallback;

        public uploadProfilePicture(int profilePicCountCurrent,  ProfilePicCopiedCallback profilePicCopiedCallbackCallback) {
            mProfilePicCountCurrent = profilePicCountCurrent;
            mProfilePicCopiedCallbackCallback = profilePicCopiedCallbackCallback;
        }

        @Override
        protected Void doInBackground(final String... params) {


            ///AmazonS3 s3 = createS3Bucket();
            AmazonS3 s3 = new AmazonS3Client(FacebookLoginFragment.getCredentialsProvider(mContext));

            String bucketName = "snapbattlevideos";
            final File newProfilePic = new File(params[0]);
            String fileName = getNextProfilePicturePathS3(mContext);


            try {

                System.out.println("Uploading a new object to S3 from a file\n");
                PutObjectRequest por = new PutObjectRequest( bucketName, fileName, newProfilePic);
                por.setGeneralProgressListener(new com.amazonaws.event.ProgressListener(){

                    @Override
                    public void progressChanged(
                            com.amazonaws.event.ProgressEvent arg0) {
                        if (arg0.getEventCode() == com.amazonaws.event.ProgressEvent.COMPLETED_EVENT_CODE)
                        {
                            File profilePicDst = new File(getProfilePictureSavePath(mContext));
                            try {
                                copy(newProfilePic, profilePicDst);


                                Handler mainHandler = new Handler(mContext.getMainLooper());
                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {mProfilePicCopiedCallbackCallback.onProfilePicCopied(mContext);}
                                };
                                mainHandler.post(myRunnable);

                                int uploadedProfilePicCount = mProfilePicCountCurrent + 1;
                                updateProfilePicCountMySql(uploadedProfilePicCount);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }

                    }});
                s3.putObject(por);


            } catch (AmazonServiceException ase)
            {
                System.out.println("Caught an AmazonServiceException, which " +
                        "means your request made it " +
                        "to Amazon S3, but was rejected with an error response" +
                        " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace)
            {
                System.out.println("Caught an AmazonClientException, which " +
                        "means the client encountered " +
                        "an internal error while trying to " +
                        "communicate with S3, " +
                        "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            }

            return null;
        }
    }



    private class getProfilePicCount extends AsyncTask<Void, Void, GetProfileResponse> {
        private ImageView mImageView;
        private getProfilePicCount(ImageView imageView)
        {
            mImageView = imageView;
        }


        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                mContext,
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(mContext));

        // Create the Lambda proxy object with default Json data binder.
// You can provide your own data binder by implementing
// LambdaDataBinder
        final LambdaFunctionsInterface mLambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);

        @Override
        protected GetProfileResponse doInBackground(Void... params) {
            // invoke "echo" method. In case it fails, it will throw a
            // LambdaFunctionException.
            try {
                return mLambdaFunctionsInterface.GetProfile();

            } catch (LambdaFunctionException lfe) {
                Log.i("ERROR", lfe.getDetails());
                Log.i("ERROR",lfe.getStackTrace().toString());
                lfe.printStackTrace();

                return null;
            }
            catch (AmazonServiceException ase) {
                // invalid credentials, incorrect AWS signature, etc
                Log.i("ERROR", ase.getErrorMessage());
                return null;
            }
            catch (AmazonClientException ace) {
                // Network issue
                Log.i("ERROR", ace.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(GetProfileResponse result) {
            if (result == null) {

                return;

            }
            User user = result.getSqlResult().get(0);
            int profilePicCount = user.getProfilePicCount();
            Log.i("TEST1",FacebookLoginFragment.getCredentialsProvider(mContext).getCachedIdentityId());
           SharedPreferences sharedPref =   mContext.getSharedPreferences(FacebookLoginFragment.getCredentialsProvider(mContext).getCachedIdentityId(), Context.MODE_PRIVATE);
            int profilePicCountSharedPrefs = sharedPref.getInt("Profile_Pic_Count", -1);


            if (profilePicCountSharedPrefs != profilePicCount)
            {
                //update the profile pic count in shared prefs
                updateProfilePicCountSharedPrefs(profilePicCount);

                if (profilePicCount > 0)
                {
                    //get new profile pic
                    new getProfilePicture(mImageView).execute();
                }


            }



        }
    }

}
