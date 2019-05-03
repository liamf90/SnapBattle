package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

import java.util.ArrayList;

public class FollowUserWithFacebookIDsRequest
{
    private ArrayList<String> facebookFriendIdList;

    public ArrayList<String> getFacebookFriendIdList() {
        return facebookFriendIdList;
    }

    public void setFacebookFriendIdList(ArrayList<String> facebookFriendIdList) {
        this.facebookFriendIdList = facebookFriendIdList;
    }
}
