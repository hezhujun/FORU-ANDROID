package com.wingsglory.foru_android.model;

import java.io.Serializable;

/**
 * Created by hezhujun on 2017/6/22.
 */
public class RelationId implements Serializable {
    private Integer user1Id;
    private Integer user2Id;

    public RelationId() {
    }

    public RelationId(Integer user1Id, Integer user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RelationId that = (RelationId) o;

        if (user1Id != null ? !user1Id.equals(that.user1Id) : that.user1Id != null) return false;
        return user2Id != null ? user2Id.equals(that.user2Id) : that.user2Id == null;
    }

    @Override
    public int hashCode() {
        int result = user1Id != null ? user1Id.hashCode() : 0;
        result = 31 * result + (user2Id != null ? user2Id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RelationId{" +
                "user1Id=" + user1Id +
                ", user2Id=" + user2Id +
                '}';
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
}
