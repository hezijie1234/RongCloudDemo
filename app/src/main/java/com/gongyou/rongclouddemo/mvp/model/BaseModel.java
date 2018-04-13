package com.gongyou.rongclouddemo.mvp.model;



import com.gongyou.rongclouddemo.mvp.bean.ResponseInfo;
import com.gongyou.rongclouddemo.mvp.view.BaseView;
import com.gongyou.rongclouddemo.network.MySubscriber;
import com.gongyou.rongclouddemo.network.RXManager;
import com.gongyou.rongclouddemo.network.RequestCode;
import com.gongyou.rongclouddemo.network.ResponseInfoAPI;
import com.gongyou.rongclouddemo.network.Retrofit2Manager;
import com.gongyou.rongclouddemo.utils.LogUtil;

import rx.Observable;
import rx.Subscription;

/**
 * Created by Administrator on 2018/3/13.
 */

public abstract class BaseModel<T,V extends BaseView>{

    public V mView;
    public ResponseInfoAPI mResponseInfoAPI;
    public BaseModel(V mView){
        this.mView = mView;
    }

    public Subscription doSubscribeAPI(final RequestCode requestCode){
        mResponseInfoAPI = Retrofit2Manager.getInstance().create(ResponseInfoAPI.class);
        return execute(requestCode);
    }

    private Subscription execute(final RequestCode requestCode) {
        return RXManager.getInstance().doSubscribe(getObservable(requestCode), new MySubscriber<ResponseInfo<T>>() {
            @Override
            public void onNext(ResponseInfo<T> tResponseInfo) {
                super.onNext(tResponseInfo);
                LogUtil.e("----onNext----"+tResponseInfo.toString());
                if (tResponseInfo.getCode() == 200) {
                    LogUtil.d("解析成功 格式正确 flag == 1 数据正常 !");
                    //测试发现当后台传过来的result是数组格式时会发送gson解析错误。

                    BaseModel.this.onNext(tResponseInfo.result, requestCode);
                } else {
                    //返回数据格式正确,但flag显示不是正确想要的数据(一般msg中会给出错误情况)
                    LogUtil.e("解析成功 格式正确 flag != 1 无正常数据 !   ------------    json格式不合理或数据为空   "+requestCode);
                    BaseModel.this.onError(tResponseInfo.getCode()+"", requestCode);
                }
            }

            @Override
            public void onError(Throwable e) {
                LogUtil.e("----onError----");
                e.printStackTrace();
                super.onError(e);//直接请求错误,根本没有拿到 ResponseInfo<T> 格式数据, 基本断定为网络错误或解析格式错误
                mView.NetWorkError(requestCode);
            }
        });
    }

    protected abstract Observable<ResponseInfo<T>> getObservable(RequestCode requestCode);

    /**
     * 请求获取到了数据,但服务器返回数据显示不是成功数据
     */
    protected void onError(String msg, RequestCode requestCode) {
        mView.onError(msg, requestCode);
    }

    protected abstract void onNext(T data, RequestCode requestCode);
}
