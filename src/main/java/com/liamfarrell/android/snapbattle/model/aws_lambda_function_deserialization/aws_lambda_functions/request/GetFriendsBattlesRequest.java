package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

import java.util.Date;
import java.util.List;

public class GetFriendsBattlesRequest
{
    private List<Integer> battleIDList;
    private Date lastUpdatedDate;

    public List<Integer> getBattleIDList() {
        return battleIDList;
    }

    public void setBattleIDList(List<Integer> battleIDList) {
        this.battleIDList = battleIDList;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}
