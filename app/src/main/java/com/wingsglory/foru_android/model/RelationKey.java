package com.wingsglory.foru_android.model;

import java.io.Serializable;

public class RelationKey implements Serializable {
    private Integer user1Id;

    private Integer user2Id;

    public RelationKey() {
    }

    public RelationKey(Integer user1Id, Integer user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }

    public Integer getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(Integer user1Id) {
        this.user1Id = user1Id;
    }

    public Integer getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(Integer user2Id) {
        this.user2Id = user2Id;
    }

    @Override
    public String toString() {
        return "RelationKey{" +
                "user1Id=" + user1Id +
                ", user2Id=" + user2Id +
                '}';
    }
}