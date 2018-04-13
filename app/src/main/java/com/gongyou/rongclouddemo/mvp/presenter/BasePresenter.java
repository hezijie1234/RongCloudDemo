package com.gongyou.rongclouddemo.mvp.presenter;





import com.gongyou.rongclouddemo.mvp.view.BaseView;
import com.google.gson.Gson;

import okhttp3.RequestBody;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Administrator on 2018/3/13.
 */

public abstract class BasePresenter<V extends BaseView>  {
    public V mView;
    public void attach(V mView){
        this.mView = mView;
    }

    public BasePresenter(){

    }
    public RequestBody getRequestBody(Object obj) {
        String route = new Gson().toJson(obj);
        RequestBody body=RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),route);
//        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), route);
        return body;
    }

    public Subscription mSubscription;

    public void detach(){
        if(mSubscription != null && mSubscription.isUnsubscribed()){
            mSubscription.unsubscribe();
        }
    }
}
