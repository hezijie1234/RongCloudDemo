package com.gongyou.rongclouddemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gongyou.rongclouddemo.mvp.bean.second.LoginInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.UserInfoById;
import com.gongyou.rongclouddemo.mvp.presenter.second.LoginPresenter;
import com.gongyou.rongclouddemo.mvp.view.second.LoginView;
import com.gongyou.rongclouddemo.network.RequestCode;
import com.gongyou.rongclouddemo.utils.Constant;
import com.gongyou.rongclouddemo.utils.SpUtils;
import com.gongyou.rongclouddemo.utils.rong.BroadcastManager;
import com.gongyou.rongclouddemo.utils.rong.RongGenerate;

import butterknife.BindView;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

public class LoginActivity extends MVPBaseActivity<LoginView,LoginPresenter> implements LoginView {
   @BindView(R.id.etPhone)
    EditText mEtPhone;
    @BindView(R.id.etPwd)
    EditText mEtPwd;
    @BindView(R.id.btnLogin)
    Button mBtnLogin;
    private String connectResultId;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initData() {
        //从SharePreference中获取存取的token，免登录。
        String cacheToken = SpUtils.getString(this,"token","");
        if (!TextUtils.isEmpty(cacheToken)){
            connect(cacheToken);
        }
    }

    @Override
    protected void addLisenter() {
        super.addLisenter();
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.login(mEtPhone.getText().toString().trim(),mEtPwd.getText().toString().trim());
            }
        });
    }

    @Override
    protected LoginPresenter initPresenter() {
        return new LoginPresenter();
    }

    /**
     * <p>连接服务器，在整个应用程序全局，只需要调用一次，需在 {@link #( Context )} 之后调用。</p>
     * <p>如果调用此接口遇到连接失败，SDK 会自动启动重连机制进行最多10次重连，分别是1, 2, 4, 8, 16, 32, 64, 128, 256, 512秒后。
     * 在这之后如果仍没有连接成功，还会在当检测到设备网络状态变化时再次进行重连。</p>
     *
     * @param token    从服务端获取的用户身份令牌（Token）。
     * @param
     * @return RongIM  客户端核心类的实例。
     */
    private void connect(final String token) {

        if (getApplicationInfo().packageName.equals(MyApp.getCurProcessName(getApplicationContext()))) {

            RongIM.connect(token, new RongIMClient.ConnectCallback() {

                /**
                 * Token 错误。可以从下面两点检查 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
                 *                  2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
                 */
                @Override
                public void onTokenIncorrect() {
                    Log.e("111", "onTokenIncorrect: " );
                }

                /**
                 * 连接融云成功
                 * @param userid 当前 token 对应的用户 id
                 */
                @Override
                public void onSuccess(String userid) {
                    BroadcastManager.getInstance(LoginActivity.this).sendBroadcast(Constant.UPDATE_CONVERSATIONS);
                    connectResultId = userid;
                    SpUtils.putString(LoginActivity.this, Constant.LOGIN_ID,userid);
                    SpUtils.putString(LoginActivity.this,Constant.LOGIN_PHONE,mEtPhone.getText().toString());
                    SpUtils.putString(LoginActivity.this,Constant.LOGIN_TOKEN,token);
                    Log.e("111", "--onSuccess" + userid);
//                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    //同步用户信息。
                    mPresenter.getUserInfoById(userid);
                }

                /**
                 * 连接融云失败
                 * @param errorCode 错误码，可到官网 查看错误码对应的注释
                 */
                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    Log.e("111", "onError: " + errorCode );
                }
            });
        }
    }

    @Override
    public void onError(String msg, RequestCode requestCode) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onError: " + msg );
    }

    @Override
    public void loginSuccess(LoginInfo data, RequestCode requestCode) {
        connect(data.getToken());
        Log.e(TAG, "loginSuccess: 登录成功。"  );
    }

    @Override
    public void getUserInfoById(UserInfoById userInfoById) {
        if (TextUtils.isEmpty(userInfoById.getPortraitUri())){
            //如果头像不存在，就设置默认的图片
            userInfoById.setPortraitUri(RongGenerate.generateDefaultAvatar(userInfoById.getNickname(),userInfoById.getId()));
        }
        String nickName = userInfoById.getNickname();
        String portraitUri = userInfoById.getPortraitUri();
        SpUtils.putString(this,Constant.LOGIN_NAME,nickName);
        SpUtils.putString(this,Constant.LOGIN_PORTRAIT,portraitUri);
        RongIM.getInstance().refreshUserInfoCache(new UserInfo(connectResultId,nickName, Uri.parse(portraitUri)));
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
