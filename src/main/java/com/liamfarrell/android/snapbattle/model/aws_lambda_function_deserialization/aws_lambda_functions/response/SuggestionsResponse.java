package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuggestionsResponse that = (SuggestionsResponse) o;
        return count == that.count &&
                Objects.equals(battle_name, that.battle_name);
    }

}
