package com.liamfarrell.android.snapbattle.model.lambda_function_request_objects;

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
