package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

import java.util.ArrayList;

public class AddFollowerRequestWithCognitoIDs
{
    private ArrayList<String> cognitoIDFollowList;

    public ArrayList<String> getCognitoIDFollowList() {
        return cognitoIDFollowList;
    }

    public void setCognitoIDFollowList(ArrayList<String> cognitoIDFollowList) {
        this.cognitoIDFollowList = cognitoIDFollowList;
    }
}
