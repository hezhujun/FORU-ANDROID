package com.wingsglory.foru_android.model;

/**
 * Created by hezhujun on 2017/6/21.
 */
public class Relation {
    public static final String NORMAL = "正常";
    public static final String CONCERN = "关注";
    public static final String BLACK = "拉黑";

    private RelationId id;
    private Integer interactionCount = 0;
    private String relation = NORMAL;

    public Relation() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Relation relation = (Relation) o;

        return id != null ? id.equals(relation.id) : relation.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "id=" + id +
                ", interactionCount=" + interactionCount +
                ", relation='" + relation + '\'' +
                '}';
    }

    public RelationId getId() {
        return id;
    }

    public void setId(RelationId id) {
        this.id = id;
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
        this.relation = relation;
    }

}
