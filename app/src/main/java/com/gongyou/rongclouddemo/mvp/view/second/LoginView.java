package com.gongyou.rongclouddemo.mvp.view.second;





import com.gongyou.rongclouddemo.mvp.bean.second.LoginInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.UserInfoById;
import com.gongyou.rongclouddemo.mvp.view.BaseView;
import com.gongyou.rongclouddemo.network.RequestCode;

import java.util.List;

/**
 * 作    者: ZhangLC
 * 创建时间: 2017/6/19.
 * 说    明:
 */

public interface LoginView extends BaseView {
    void loginSuccess(LoginInfo data, RequestCode requestCode);

    void getUserInfoById(UserInfoById userInfoById);
//    void setWorkTypeData(List<WorkType> data, RequestCode requestCode);
//
//    void setQQLoginCallBack(QQUnionId qqUnionInfo);
}
