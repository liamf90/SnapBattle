package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

import java.util.Date;
import java.util.List;

public class GetFriendsBattlesRequestOld
{
    private List<Integer> battleIDList;
    private String lastUpdatedDate;

    public List<Integer> getBattleIDList() {
        return battleIDList;
    }

    public void setBattleIDList(List<Integer> battleIDList) {
        this.battleIDList = battleIDList;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}
