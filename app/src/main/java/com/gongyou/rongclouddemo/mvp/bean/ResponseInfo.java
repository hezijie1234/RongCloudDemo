package com.gongyou.rongclouddemo.mvp.bean;

/**
 * 服务器回复数据
 */

public class ResponseInfo<T> {

    /**result 是一个对象，当后台传递过来的是数组时就会报错。
     * code : 200
     * result : {"id":"8Dv5kWUMD","token":"PZxIwK6i4RIGh+VkhUxezXxpRjANxKgfakOnYLFljI+6lR6y3E7UAEtPhSPPKxkbYyK8/JtC+Qr8vxnJuHiiWQ=="}
     */

    private int code;
    public T result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "ResponseInfo{" +
                "code=" + code +
                ", result=" + result +
                '}';
    }
}
