package com.gongyou.rongclouddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gongyou.rongclouddemo.adapter.ConversationListAdapterEx;
import com.gongyou.rongclouddemo.greendao.DBManager;
import com.gongyou.rongclouddemo.utils.Constant;
import com.gongyou.rongclouddemo.utils.NetUtils;
import com.gongyou.rongclouddemo.utils.rong.BroadcastManager;

import butterknife.BindView;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

public class MainActivity extends BaseActivity {
    @BindView(R.id.go_to_conversation_list)
    Button mGoConversationList;
    @BindView(R.id.create_group)
    Button mCreateGroup;
    private Conversation.ConversationType[] mConversationsTypes = null;
    private ConversationListFragment mConversationListFragment = null;
    private boolean isDebug;
    private Fragment conversationList;
    //连接成功才加载会话列表。
    private boolean flag;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        //同步所有用户信息给融云的服务器。
        DBManager.getInstance().getAllUserInfo();
        fragmentManager = getSupportFragmentManager();
    }

    @Override
    protected void initView() {
        registerBR();
        isDebug = getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false);
        conversationList = initConversationList();
    }

    @Override
    protected void addLisenter() {
        super.addLisenter();
        mGoConversationList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if (conversationList.isAdded()){
                        if (conversationList.isHidden()){
                            fragmentManager.beginTransaction().show(conversationList).commit();
                        }
                    }else {
                        fragmentManager.beginTransaction().add(R.id.list_container, conversationList).commit();

                    }
            }
        });
        mCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SelectFriendsActivity.class));
            }
        });
        //连接状态监听。
//        RongIM.setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
//            @Override
//            public void onChanged(ConnectionStatus connectionStatus) {
//                switch (connectionStatus){
//
//                    case CONNECTED://连接成功。
//                        flag = true;
//                        break;
//                    case DISCONNECTED://断开连接。
//                        flag = false;
//                        break;
//                    case CONNECTING://连接中。
//
//                        break;
//                    case NETWORK_UNAVAILABLE://网络不可用。
//
//                        break;
//                    case KICKED_OFFLINE_BY_OTHER_CLIENT://用户账户在其他设备登录，本机会被踢掉线
//
//                        break;
//                }
//            }
//        });
    }

    private Fragment initConversationList() {
        if (mConversationListFragment == null) {
            ConversationListFragment listFragment = new ConversationListFragment();
            listFragment.setAdapter(new ConversationListAdapterEx(RongContext.getInstance()));
            Uri uri;
            if (isDebug) {
                uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                        .appendPath("conversationlist")
                        .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "true") //设置私聊会话是否聚合显示
                        .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "true")//群组
                        .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                        .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//订阅号
                        .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")//系统
                        .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "true")
                        .build();
                mConversationsTypes = new Conversation.ConversationType[]{Conversation.ConversationType.PRIVATE,
                        Conversation.ConversationType.GROUP,
                        Conversation.ConversationType.PUBLIC_SERVICE,
                        Conversation.ConversationType.APP_PUBLIC_SERVICE,
                        Conversation.ConversationType.SYSTEM,
                        Conversation.ConversationType.DISCUSSION
                };

            } else {
                uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                        .appendPath("conversationlist")
                        .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                        .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//群组
                        .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                        .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//订阅号
                        .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")//系统
                        .build();
                mConversationsTypes = new Conversation.ConversationType[]{Conversation.ConversationType.PRIVATE,
                        Conversation.ConversationType.GROUP,
                        Conversation.ConversationType.PUBLIC_SERVICE,
                        Conversation.ConversationType.APP_PUBLIC_SERVICE,
                        Conversation.ConversationType.SYSTEM
                };
            }
            listFragment.setUri(uri);
            mConversationListFragment = listFragment;
            return listFragment;
        } else {
            return mConversationListFragment;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBR();
    }

    private void registerBR() {
        BroadcastManager.getInstance(this).register(Constant.FETCH_COMPLETE, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                hideWaitingDialog();
//                hideLoading();
            }
        });
    }

    private void unRegisterBR() {
        BroadcastManager.getInstance(this).unregister(Constant.FETCH_COMPLETE);
    }
}
