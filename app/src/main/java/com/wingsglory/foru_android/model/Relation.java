package com.wingsglory.foru_android.model;

import java.io.Serializable;

public class Relation extends RelationKey implements Serializable {

    public static final String RELATION_NORMAL = "正常";
    public static final String RELATION_FRIEND = "关注";
    public static final String RELATION_BLACK = "拉黑";

    private User user1;

    private User user2;

    private Integer interactionCount;

    private String relation;

    public Relation() {
    }

    public Relation(Integer user1Id, Integer user2Id) {
        super(user1Id, user2Id);
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public Integer getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(Integer interactionCount) {
        this.interactionCount = interactionCount;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation == null ? null : relation.trim();
    }

    @Override
    public String toString() {
        return "Relation{" +
                "user1=" + user1 +
                ", user2=" + user2 +
                ", interactionCount=" + interactionCount +
                ", relation='" + relation + '\'' +
                '}';
    }
}