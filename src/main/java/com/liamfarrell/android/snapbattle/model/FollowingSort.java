package com.liamfarrell.android.snapbattle.model;

/**
 * Created by Liam on 5/08/2017.
 *
 * An Extending class of user, allowing an integer to be set to the following user, indicating how close the search string matches the follower.
 * Allows for sorting User objects in an list
 */

public class FollowingSort extends User {
    private int mSortFactor;
    private boolean mHasProfilePicDownloadError;

    public FollowingSort(String FacebookName, String CognitoID,String username,  String FacebookID, int profilePicCount, String newSignedUrlProfilePic) {
        super(CognitoID, username,FacebookName, profilePicCount, newSignedUrlProfilePic);
        mHasProfilePicDownloadError = false;
    }

    public FollowingSort(String FacebookID, String FacebookName) {
        super(FacebookID, FacebookName);
    }


    public void setSortFactor(int sortFactor)
    {
        mSortFactor = sortFactor;
    }
    public int getSortFactor()
    {
        return mSortFactor;
    }

    public void setProfilePicDownloadError(boolean hasError)
    {
        mHasProfilePicDownloadError = hasError;
    }

    public boolean getHasProfilePicDownloadError()
    {
        return mHasProfilePicDownloadError;
    }





}
