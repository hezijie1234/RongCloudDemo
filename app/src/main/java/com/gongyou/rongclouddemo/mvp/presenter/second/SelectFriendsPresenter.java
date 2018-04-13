package com.gongyou.rongclouddemo.mvp.presenter.second;

import com.gongyou.rongclouddemo.mvp.bean.ResponseInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.AllFriendsInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.LoginInfo;
import com.gongyou.rongclouddemo.mvp.model.BaseModel;
import com.gongyou.rongclouddemo.mvp.presenter.BasePresenter;
import com.gongyou.rongclouddemo.mvp.view.second.LoginView;
import com.gongyou.rongclouddemo.mvp.view.second.SelectFriendView;
import com.gongyou.rongclouddemo.network.RequestCode;
import com.gongyou.rongclouddemo.request.LoginRequest;

import rx.Observable;
import rx.Subscription;

/**
 * Created by hezijie on 2018/4/11.
 */

public class SelectFriendsPresenter extends BasePresenter<SelectFriendView> {

    public Subscription getAllFriends(){
        mSubscription = new BaseModel<AllFriendsInfo,SelectFriendView>(mView){

            @Override
            protected Observable<ResponseInfo<AllFriendsInfo>> getObservable(RequestCode requestCode) {
                return mResponseInfoAPI.getAllUserRelation();
            }

            @Override
            protected void onNext(AllFriendsInfo data, RequestCode requestCode) {
                mView.getAllUserInfo(data);
            }
        }.doSubscribeAPI(RequestCode.ox);
        return mSubscription;
    }

}
