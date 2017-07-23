package com.wingsglory.foru_android.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class TaskContent implements Serializable {
    private Integer id;

    private String title;

    private String content;

    private String targetPosition;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer addresseeId;

    private Addressee addressee;

    private Date timeout;

    private BigDecimal reward = new BigDecimal(0);

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public String getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(String targetPosition) {
        this.targetPosition = targetPosition == null ? null : targetPosition.trim();
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Integer getAddresseeId() {
        return addresseeId;
    }

    public void setAddresseeId(Integer addresseeId) {
        this.addresseeId = addresseeId;
    }

    public Date getTimeout() {
        return timeout;
    }

    public void setTimeout(Date timeout) {
        this.timeout = timeout;
    }

    public BigDecimal getReward() {
        return reward;
    }

    public void setReward(BigDecimal reward) {
        this.reward = reward;
    }

    public Addressee getAddressee() {
        return addressee;
    }

    public void setAddressee(Addressee addressee) {
        this.addressee = addressee;
    }

    @Override
    public String toString() {
        return "TaskContent{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", targetPosition='" + targetPosition + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", addresseeId=" + addresseeId +
                ", addressee=" + addressee +
                ", timeout=" + timeout +
                ", reward=" + reward +
                '}';
    }
}