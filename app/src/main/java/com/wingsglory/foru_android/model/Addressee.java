package com.wingsglory.foru_android.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by hezhujun on 2017/6/21.
 */
public class Addressee implements Serializable, Cloneable {
    private Integer id;
    private String name;
    private String phone;
    private String address;
    private String addressDetail;
    private String latitude;
    private String longitude;
    private Integer userId;
    private Timestamp gmtCreate;
    private Timestamp gmtModified;

    public Addressee() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Addressee addressee = (Addressee) o;

        return id != null ? id.equals(addressee.id) : addressee.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Addressee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", addressDetail='" + addressDetail + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", userId=" + userId +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                '}';
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    @Override
    public Object clone() throws CloneNotSupportedException {
        Addressee addressee = (Addressee) super.clone();
        if (addressee.getGmtCreate() != null) {
            addressee.setGmtCreate(new Timestamp(addressee.getGmtCreate().getTime()));
        }
        if (addressee.getGmtModified() != null) {
            addressee.setGmtModified(new Timestamp(addressee.getGmtModified().getTime()));
        }
        if (addressee.getId() != null) {
            addressee.setId(new Integer(addressee.getId()));
        }
        if (addressee.getUserId() != null) {
            addressee.setUserId(new Integer(addressee.getUserId()));
        }
        return addressee;
    }


}
