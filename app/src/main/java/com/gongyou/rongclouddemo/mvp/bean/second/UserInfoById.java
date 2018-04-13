package com.gongyou.rongclouddemo.mvp.bean.second;

/**
 * Created by hezijie on 2018/4/9.
 */

public class UserInfoById {

    /**
     * id : 10YVscJI3
     * nickname : 阿明
     * portraitUri :
     */

    private String id;
    private String nickname;
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

    public String getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    @Override
    public String toString() {
        return "UserInfoById{" +
                "id='" + id + '\'' +
                ", nickname='" + nickname + '\'' +
                ", portraitUri='" + portraitUri + '\'' +
                '}';
    }
}
