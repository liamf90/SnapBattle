package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

public class VideoSubmittedResponse
{
    private String error;
    private String time_ban_ends;
    private String voting_time_end;
    private int success;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTime_ban_ends() {
        return time_ban_ends;
    }

    public void setTimeBanEnds(String time_ban_ends) {
        this.time_ban_ends = time_ban_ends;
    }

    public String getVoting_time_end() {
        return voting_time_end;
    }

    public void setVotingTimeEnd(String voting_time_end) {
        this.voting_time_end = voting_time_end;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }
}
