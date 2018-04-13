package com.gongyou.rongclouddemo.mvp.presenter.second;



import com.gongyou.rongclouddemo.mvp.bean.ResponseInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.LoginInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.UserInfoById;
import com.gongyou.rongclouddemo.mvp.model.BaseModel;
import com.gongyou.rongclouddemo.mvp.presenter.BasePresenter;
import com.gongyou.rongclouddemo.mvp.view.second.LoginView;
import com.gongyou.rongclouddemo.network.RequestCode;
import com.gongyou.rongclouddemo.request.LoginRequest;

import rx.Observable;
import rx.Subscription;

/**
 * Created by Administrator on 2018/3/14.
 */

public class LoginPresenter extends BasePresenter<LoginView> {

    public Subscription login(final String mobile, final String pwd){
        mSubscription = new BaseModel<LoginInfo,LoginView>(mView){

            @Override
            protected Observable<ResponseInfo<LoginInfo>> getObservable(RequestCode requestCode) {
                return mResponseInfoAPI.login(getRequestBody(new LoginRequest("86", mobile, pwd)));
            }

            @Override
            protected void onNext(LoginInfo data, RequestCode requestCode) {
                mView.loginSuccess(data,requestCode);
            }
        }.doSubscribeAPI(RequestCode.ox);
        return mSubscription;
    }

    public Subscription getUserInfoById(final String id){
        mSubscription = new BaseModel<UserInfoById,LoginView>(mView){

            @Override
            protected Observable<ResponseInfo<UserInfoById>> getObservable(RequestCode requestCode) {
                return mResponseInfoAPI.getUserInfoById(id);
            }

            @Override
            protected void onNext(UserInfoById data, RequestCode requestCode) {
                mView.getUserInfoById(data);
            }
        }.doSubscribeAPI(RequestCode.rat);
        return mSubscription;
    }
}
