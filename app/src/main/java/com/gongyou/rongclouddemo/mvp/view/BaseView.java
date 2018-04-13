package com.gongyou.rongclouddemo.mvp.view;


import com.gongyou.rongclouddemo.network.RequestCode;

/**
 * Created by Administrator on 2018/3/13.
 */

public interface BaseView {

    void NetWorkError(RequestCode requestCode);

    void onError(String msg, RequestCode requestCode);
}
