package com.gongyou.rongclouddemo.greendaobean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

import android.net.Uri;
import android.os.Parcel;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

import io.rong.common.ParcelUtils;
import io.rong.imlib.model.UserInfo;

/**
 * Entity mapped to table FRIEND.
 */
@Entity
public class Friend implements Serializable{
    private static final long serialVersionUID = 4002042486942197448L;
    /**定义主键*/
    @Id(autoincrement = true)
    private long id;
    private String userId;
    private String name;
    private String portraitUri;
    private String displayName;
    private String region;
    private String phoneNumber;
    private String status;
    private Long timestamp;
    private String letters;
    private String nameSpelling;
    private String displayNameSpelling;
    @Generated(hash = 287143722)
    public Friend() {
    }
    @Generated(hash = 1594950639)
    public Friend(long id, String userId, String name, String portraitUri, String displayName, String region, String phoneNumber, String status, Long timestamp, String letters, String nameSpelling, String displayNameSpelling) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.portraitUri = portraitUri;
        this.displayName = displayName;
        this.region = region;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.timestamp = timestamp;
        this.letters = letters;
        this.nameSpelling = nameSpelling;
        this.displayNameSpelling = displayNameSpelling;
    }
    @Override
    public String toString() {
        return "Friend{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", portraitUri=" + portraitUri +
                ", region='" + region + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                ", letters='" + letters + '\'' +
                ", nameSpelling='" + nameSpelling + '\'' +
                ", displayNameSpelling='" + displayNameSpelling + '\'' +
                '}';
    }

    public void setId(long id) {

        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }

    public void setNameSpelling(String nameSpelling) {
        this.nameSpelling = nameSpelling;
    }

    public void setDisplayNameSpelling(String displayNameSpelling) {
        this.displayNameSpelling = displayNameSpelling;
    }

    public long getId() {

        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public String getRegion() {
        return region;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getLetters() {
        return letters;
    }

    public String getNameSpelling() {
        return nameSpelling;
    }

    public String getDisplayNameSpelling() {
        return displayNameSpelling;
    }
}
