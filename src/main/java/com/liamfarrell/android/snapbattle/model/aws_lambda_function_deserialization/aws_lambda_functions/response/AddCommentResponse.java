package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.Comment;

import java.util.List;

public class AddCommentResponse
{
    private List<Comment> sql_result;
    private String error;
    private String time_ban_ends;
    private static final String USER_NOT_MINIMUM_FRIENDS_ERROR = "USER_NOT_MINIMUM_FRIENDS";
    private static final String USER_BANNED_ERROR = "USER_BANNED_ERROR";

    public static String getUserNotMinimumFriendsError() {
        return USER_NOT_MINIMUM_FRIENDS_ERROR;
    }

    public static String getUserBannedError() {
        return USER_BANNED_ERROR;
    }

    public List<Comment> getSqlResult() {
        return sql_result;
    }

    public void setSqlResult(List<Comment> sql_result) {
        this.sql_result = sql_result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTimeBanEnds() {
        return time_ban_ends;
    }

    public void setTimeBanEnds(String time_ban_ends) {
        this.time_ban_ends = time_ban_ends;
    }
}
