package com.liamfarrell.android.snapbattle.activity.startup;
/**
 * Created by Liam on 31/12/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.liamfarrell.android.snapbattle.caches.CurrentUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.R;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChooseProfilePictureStartupFragment extends Fragment
{
    public static final int PICK_IMAGE = 400;
    public static final String TAG = "ChooseProfileFragment";
    public static final String PROFILE_PIC_UPDATED_BROADCAST = "com.liamfarrell.android.snapbattle.profilepicupdatedbroadcast";

    private Fragment mThisFragment;
    private CurrentUsersProfilePicCacheManager mCurrentUsersProfilePicCacheManager;
    private String mTempSavedProfilePicPath;


    private CircleImageView profileImageView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState)
    {
        mThisFragment = this;
        View v = inflater.inflate(R.layout.fragment_choose_profile_picture_startup, parent, false);
        mCurrentUsersProfilePicCacheManager = new CurrentUsersProfilePicCacheManager(getActivity().getApplicationContext());
        View progressContainer = v.findViewById(R.id.progressContainer);
        progressContainer.setVisibility(View.GONE);
        Button changeProfilePictureButton = v.findViewById(R.id.changeProfilePictureButton);
        profileImageView = v.findViewById(R.id.profileImageView);


        profileImageView.setImageResource(R.drawable.default_profile_pic);
        changeProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(200,200).start(getActivity(), mThisFragment);
            }});
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_IMAGE)
        {
            //image has been selected, now crop it
            Uri chosenPhotoUri = data.getData();
            CropImage.activity(chosenPhotoUri).setAspectRatio(200,200).start(getContext(), this);
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            //image has been cropped
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK)
            {
                Uri resultUri = result.getUri();
                profileImageView.setImageURI(resultUri);
                ((StartupActivity)getActivity()).setEnableNextButton(true);
                mTempSavedProfilePicPath = resultUri.getPath();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getActivity(), R.string.generic_error_toast, Toast.LENGTH_SHORT).show();
            }
        }

    }
    public void uploadProfilePic() {
            mCurrentUsersProfilePicCacheManager.updateProfilePicture(mTempSavedProfilePicPath, 0, new CurrentUsersProfilePicCacheManager.ProfilePicCopiedCallback() {
                @Override
                public void onProfilePicCopied(Context context) {
                    Log.i(TAG, "On Profile Pic Copied");
                    //Send broadcast to update the profile pic
                    Intent intent = new Intent(PROFILE_PIC_UPDATED_BROADCAST);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            });



    }
}


