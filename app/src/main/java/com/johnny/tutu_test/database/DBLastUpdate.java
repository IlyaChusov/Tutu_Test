package com.johnny.tutu_test.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class DBLastUpdate {

    @PrimaryKey
    private int actualTimeId = 1;
    private Date lastUpdateTime;

    public int getActualTimeId() {
        return actualTimeId;
    }

    public void setActualTimeId(int actualTimeId) {
        this.actualTimeId = actualTimeId;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
