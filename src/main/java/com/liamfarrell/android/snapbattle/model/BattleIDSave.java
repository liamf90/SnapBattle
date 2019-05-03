package com.liamfarrell.android.snapbattle.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class BattleIDSave implements Serializable {
    private int mBattleID;
    private Date mTimeAddedToCache;

    public BattleIDSave(int battleID) {
        mBattleID = battleID;
        Calendar now = Calendar.getInstance();
        mTimeAddedToCache = now.getTime();
}

public int getBattleID() {
return mBattleID;
}

public Date getTimeAddedToCache() {
return mTimeAddedToCache;
}
}
