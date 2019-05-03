package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

import java.util.List;

public class GetUsersRequest
{
    private List<String> userCognitoIDList;

    public List<String> getUserCognitoIDList() {
        return userCognitoIDList;
    }

    public void setUserCognitoIDList(List<String> userCognitoIDList) {
        this.userCognitoIDList = userCognitoIDList;
    }
}
