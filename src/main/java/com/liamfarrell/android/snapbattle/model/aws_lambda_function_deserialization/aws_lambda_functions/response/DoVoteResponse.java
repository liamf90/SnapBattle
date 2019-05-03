package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import java.util.List;

public class DoVoteResponse {

    private int battleID;
    private List<String> messages;

    public int getBattleID() {
        return battleID;
    }
}
