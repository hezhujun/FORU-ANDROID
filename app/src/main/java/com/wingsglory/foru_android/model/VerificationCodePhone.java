package com.wingsglory.foru_android.model;

import java.sql.Timestamp;

/**
 * Created by hezhujun on 2017/6/21.
 */
public class VerificationCodePhone {
    private String phone;
    private String code;
    private Timestamp gmtModified;

    public VerificationCodePhone() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VerificationCodePhone that = (VerificationCodePhone) o;

        return phone != null ? phone.equals(that.phone) : that.phone == null;
    }

    @Override
    public int hashCode() {
        return phone != null ? phone.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "VerificationCodePhone{" +
                "phone='" + phone + '\'' +
                ", code='" + code + '\'' +
                ", gmtModified=" + gmtModified +
                '}';
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Timestamp getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Timestamp gmtModified) {
        this.gmtModified = gmtModified;
    }
}
