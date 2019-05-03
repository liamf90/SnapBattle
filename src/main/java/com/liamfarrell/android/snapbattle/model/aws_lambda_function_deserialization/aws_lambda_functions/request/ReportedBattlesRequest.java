package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class ReportedBattlesRequest
{
    private int fetchLimit;

    public int getFetchLimit() {
        return fetchLimit;
    }

    public void setFetchLimit(int fetchLimit) {
        this.fetchLimit = fetchLimit;
    }
}
