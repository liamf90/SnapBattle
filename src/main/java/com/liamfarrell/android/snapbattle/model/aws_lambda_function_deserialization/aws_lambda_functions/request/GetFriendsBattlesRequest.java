package com.liamfarrell.android.snapbattle.model.lambda_function_request_objects;

import java.util.List;

public class GetFriendsBattlesRequest
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
