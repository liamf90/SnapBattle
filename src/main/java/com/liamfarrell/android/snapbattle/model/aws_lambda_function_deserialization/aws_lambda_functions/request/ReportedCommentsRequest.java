package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class ReportedCommentsRequest
{
    private int fetchLimit;
    private int offset;

    public int getFetchLimit() {
        return fetchLimit;
    }

    public void setFetchLimit(int fetchLimit) {
        this.fetchLimit = fetchLimit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
