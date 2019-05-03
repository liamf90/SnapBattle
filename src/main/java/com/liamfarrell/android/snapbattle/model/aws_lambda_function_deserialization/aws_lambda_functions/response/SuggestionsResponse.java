package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

public class SuggestionsResponse
{
    private String battle_name;
    private int count;

    public String getBattleName() {
        return battle_name;
    }

    public void setBattleName(String battle_name) {
        this.battle_name = battle_name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
