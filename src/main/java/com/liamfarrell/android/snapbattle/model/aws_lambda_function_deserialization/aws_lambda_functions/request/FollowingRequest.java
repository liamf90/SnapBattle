package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

public class FollowingRequest
{
    private boolean shouldGetProfilePic;

    public boolean isShouldGetProfilePic() {
        return shouldGetProfilePic;
    }

    public void setShouldGetProfilePic(boolean shouldGetProfilePic) {
        this.shouldGetProfilePic = shouldGetProfilePic;
    }
}
