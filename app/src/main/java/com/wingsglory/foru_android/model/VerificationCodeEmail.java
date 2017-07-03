package com.wingsglory.foru_android.model;

import java.sql.Timestamp;

/**
 * Created by hezhujun on 2017/6/21.
 */
public class VerificationCodeEmail {
    private String email;
    private String code;
    private Timestamp gmtModified;

    public VerificationCodeEmail() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VerificationCodeEmail that = (VerificationCodeEmail) o;

        return email != null ? email.equals(that.email) : that.email == null;
    }

    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "VerificationCodeEmail{" +
                "email='" + email + '\'' +
                ", code='" + code + '\'' +
                ", gmtModified=" + gmtModified +
                '}';
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
