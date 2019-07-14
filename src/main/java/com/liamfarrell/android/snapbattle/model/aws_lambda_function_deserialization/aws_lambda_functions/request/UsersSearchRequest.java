package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class UsersSearchRequest
{
    private String userSearchQuery;

    public String getUserSearchQuery() {
        return userSearchQuery;
    }

    public void setUserSearchQuery(String userSearchQuery) {
        this.userSearchQuery = userSearchQuery;
    }
}
