package com.wingsglory.foru_android.model;

import java.sql.Timestamp;

/**
 * Created by hezhujun on 2017/6/21.
 */
public class Task {
    public static final String PUBLISHED = "发布";
    public static final String ACCEPTED = "接受";
    public static final String CONFIRMED = "确认";
    public static final String COMPLETE = "完成";
    public static final String DELETED = "失效";
    public static final String FAILED = "失败";

    public static final int PUBLISHER_CONFIRM_ZERO = 0;
    public static final int PUBLISHER_CONFIRM_FIRST = 1;
    public static final int PUBLISHER_CONFIRM_SECOND = 2;
    public static final int RECIPIENT_CONFIRM_ZERO = 0;
    public static final int RECIPIENT_CONFIRM_FIRST = 1;
    public static final int RECIPIENT_CONFIRM_SECOND = 2;

    private Integer id;
    private Integer publisher;
    private Integer recipient;
    private String state = ACCEPTED;
    private String expressWaybillNumber;
    private Integer publisherConfirm = PUBLISHER_CONFIRM_ZERO;
    private Integer recipientConfirm = RECIPIENT_CONFIRM_ZERO;
    private Integer evaluationToPublisher;
    private Integer evaluationToRecipient;
    private Timestamp gmtCreate;
    private Timestamp gmtModified;

    public Task() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        return id != null ? id.equals(task.id) : task.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", publisher=" + publisher +
                ", recipient=" + recipient +
                ", state=" + state +
                ", expressWaybillNumber='" + expressWaybillNumber + '\'' +
                ", publisherConfirm=" + publisherConfirm +
                ", recipientConfirm=" + recipientConfirm +
                ", evaluationToPublisher=" + evaluationToPublisher +
                ", evaluationToRecipient=" + evaluationToRecipient +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPublisher() {
        return publisher;
    }

    public void setPublisher(Integer publisher) {
        this.publisher = publisher;
    }

    public Integer getRecipient() {
        return recipient;
    }

    public void setRecipient(Integer recipient) {
        this.recipient = recipient;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getExpressWaybillNumber() {
        return expressWaybillNumber;
    }

    public void setExpressWaybillNumber(String expressWaybillNumber) {
        this.expressWaybillNumber = expressWaybillNumber;
    }

    public Integer getPublisherConfirm() {
        return publisherConfirm;
    }

    public void setPublisherConfirm(Integer publisherConfirm) {
        this.publisherConfirm = publisherConfirm;
    }

    public Integer getRecipientConfirm() {
        return recipientConfirm;
    }

    public void setRecipientConfirm(Integer recipientConfirm) {
        this.recipientConfirm = recipientConfirm;
    }

    public Integer getEvaluationToPublisher() {
        return evaluationToPublisher;
    }

    public void setEvaluationToPublisher(Integer evaluationToPublisher) {
        this.evaluationToPublisher = evaluationToPublisher;
    }

    public Integer getEvaluationToRecipient() {
        return evaluationToRecipient;
    }

    public void setEvaluationToRecipient(Integer evaluationToRecipient) {
        this.evaluationToRecipient = evaluationToRecipient;
    }

    public Timestamp getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Timestamp gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Timestamp getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Timestamp gmtModified) {
        this.gmtModified = gmtModified;
    }
}
