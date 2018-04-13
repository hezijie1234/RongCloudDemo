package com.gongyou.rongclouddemo.mvp.view.second;

import com.gongyou.rongclouddemo.mvp.bean.second.AllFriendsInfo;
import com.gongyou.rongclouddemo.mvp.view.BaseView;

/**
 * Created by hezijie on 2018/4/11.
 */

public interface SelectFriendView extends BaseView {

    void getAllUserInfo(AllFriendsInfo friendsInfo);
}
