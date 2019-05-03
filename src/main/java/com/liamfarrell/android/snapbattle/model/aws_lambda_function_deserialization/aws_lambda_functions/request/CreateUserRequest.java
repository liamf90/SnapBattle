package com.liamfarrell.android.snapbattle.model.lambda_function_request_objects;

public class CreateUserRequest {
    private String facebookID;
    private String facebookName;

    public String getFacebookID() {
        return facebookID;
    }

    public void setFacebookID(String facebookID) {
        this.facebookID = facebookID;
    }

    public String getFacebookName() {
        return facebookName;
    }

    public void setFacebookName(String facebookName) {
        this.facebookName = facebookName;
    }
}
