package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class CompletedBattlesRequest
{
    private String getAfterDate;
    private int fetchLimit;

    public String getGetAfterDate() {
        return getAfterDate;
    }

    public void setGetAfterDate(String getAfterDate) {
        this.getAfterDate = getAfterDate;
    }

    public int getFetchLimit() {
        return fetchLimit;
    }

    public void setFetchLimit(int fetchLimit) {
        this.fetchLimit = fetchLimit;
    }
}
