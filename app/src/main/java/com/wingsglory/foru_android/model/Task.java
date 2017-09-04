package com.wingsglory.foru_android.model;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {

    public static final String TASK_STATE_NEW = "新建";
    public static final String TASK_STATE_WAIT_FOR_COMPLETE = "待完成";
    public static final String TASK_STATE_WAIT_FOR_CONFIRM = "待确认";
    public static final String TASK_STATE_COMPLETE = "完成";
    public static final String TASK_STATE_OVERDUE = "过期";
    public static final String TASK_STATE_FAIL = "失败";
    public static final String TASK_STATE_DELETE = "删除";

    private Integer id;

    private Integer publisherId;

    private User publisher;

    private Integer recipientId;

    private User recipient;

    private String state;

    private Integer contentId;

    private TaskContent content;

    private Integer evaluationToPublisher;

    private Integer evaluationToRecipient;

    private Date gmtCreate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
    }

    public Integer getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Integer recipientId) {
        this.recipientId = recipientId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state == null ? null : state.trim();
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
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

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public User getPublisher() {
        return publisher;
    }

    public void setPublisher(User publisher) {
        this.publisher = publisher;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public TaskContent getContent() {
        return content;
    }

    public void setContent(TaskContent content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", publisherId=" + publisherId +
                ", publisher=" + publisher +
                ", recipientId=" + recipientId +
                ", recipient=" + recipient +
                ", state='" + state + '\'' +
                ", contentId=" + contentId +
                ", content=" + content +
                ", evaluationToPublisher=" + evaluationToPublisher +
                ", evaluationToRecipient=" + evaluationToRecipient +
                ", gmtCreate=" + gmtCreate +
                '}';
    }
}