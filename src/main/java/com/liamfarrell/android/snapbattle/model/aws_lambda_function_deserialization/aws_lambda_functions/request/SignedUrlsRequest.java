package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

import java.util.List;

public class SignedUrlsRequest
{
    private List<String> cognitoIdToGetSignedUrlList;

    public List<String> getCognitoIdToGetSignedUrlList() {
        return cognitoIdToGetSignedUrlList;
    }

    public void setCognitoIdToGetSignedUrlList(List<String> cognitoIdToGetSignedUrlList) {
        this.cognitoIdToGetSignedUrlList = cognitoIdToGetSignedUrlList;
    }
}
