package com.liamfarrell.android.snapbattle.model.lambda_function_request_objects;

public class CurrentBattlesRequest
{
    private int offset;
    public int fetchLimit;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
