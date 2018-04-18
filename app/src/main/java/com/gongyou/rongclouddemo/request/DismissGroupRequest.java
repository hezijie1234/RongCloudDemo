package com.gongyou.rongclouddemo.request;

/**
 * Created by hezijie on 2018/4/18.
 */

public class DismissGroupRequest {
    private String groupId;

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {

        return groupId;
    }

    public DismissGroupRequest(String groupId) {

        this.groupId = groupId;
    }
}
