package com.wingsglory.foru_android.model;

import java.util.Date;

public class VerificationCodeEmail {
    private String phone;

    private String code;

    private Date gmtModified;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    @Override
    public String toString() {
        return "VerificationCodeEmail{" +
                "phone='" + phone + '\'' +
                ", code='" + code + '\'' +
                ", gmtModified=" + gmtModified +
                '}';
    }
}