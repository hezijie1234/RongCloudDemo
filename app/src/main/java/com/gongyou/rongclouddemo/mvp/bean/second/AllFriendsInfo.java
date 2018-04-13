package com.gongyou.rongclouddemo.mvp.bean.second;

/**
 * Created by hezijie on 2018/4/11.
 */

public class AllFriendsInfo {


    /**
     * displayName : 李明凤
     * message : 李
     * status : 20
     * updatedAt : 2017-06-12T07:43:35.000Z
     * user : {"id":"kmdDLuMJm","nickname":"李明凤的昵称","region":"86","phone":"18916614537","portraitUri":""}
     */

    private String displayName;
    private String message;
    private int status;
    private String updatedAt;
    private UserBean user;

    @Override
    public String toString() {
        return "AllFriendsInfo{" +
                "displayName='" + displayName + '\'' +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", updatedAt='" + updatedAt + '\'' +
                ", user=" + user +
                '}';
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public static class UserBean {
        /**
         * id : kmdDLuMJm
         * nickname : 李明凤的昵称
         * region : 86
         * phone : 18916614537
         * portraitUri :
         */

        private String id;
        private String nickname;
        private String region;
        private String phone;
        private String portraitUri;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPortraitUri() {
            return portraitUri;
        }

        public void setPortraitUri(String portraitUri) {
            this.portraitUri = portraitUri;
        }

        @Override
        public String toString() {
            return "UserBean{" +
                    "id='" + id + '\'' +
                    ", nickname='" + nickname + '\'' +
                    ", region='" + region + '\'' +
                    ", phone='" + phone + '\'' +
                    ", portraitUri='" + portraitUri + '\'' +
                    '}';
        }
    }
}
