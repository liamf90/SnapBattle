package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetNewSignedUrlResponse;

import java.util.List;

public class GetSignedUrlsResponse {

    private List<GetNewSignedUrlResponse> new_signed_urls;

    public List<GetNewSignedUrlResponse> getNewSignedUrls() {
        return new_signed_urls;
    }

    public void setNewSignedUrls(List<GetNewSignedUrlResponse> new_signed_urls) {
        this.new_signed_urls = new_signed_urls;
    }
}
