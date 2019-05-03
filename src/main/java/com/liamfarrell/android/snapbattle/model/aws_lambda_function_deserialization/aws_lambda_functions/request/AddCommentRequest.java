package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

import java.util.List;

public class AddCommentRequest
{
    private String comment;
    private int battleID;
    private List<String> usernames_to_tag;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getBattleID() {
        return battleID;
    }

    public void setBattleID(int battleID) {
        this.battleID = battleID;
    }

    public List<String> getUsernamesToTag() {
        return usernames_to_tag;
    }

    public void setUsernamesToTag(List<String> usernamesToTagList) {
        this.usernames_to_tag = usernamesToTagList;
    }
}

