package com.gongyou.rongclouddemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gongyou.rongclouddemo.greendaobean.Friend;
import com.gongyou.rongclouddemo.greendaobean.GroupMember;
import com.gongyou.rongclouddemo.greendaobean.Groups;
import com.gongyou.rongclouddemo.mvp.bean.db.GetGroupMemberResponse;
import com.gongyou.rongclouddemo.mvp.bean.db.GetGroupResponse;
import com.gongyou.rongclouddemo.request.DismissGroupRequest;
import com.gongyou.rongclouddemo.request.QuitGroupRequest;
import com.gongyou.rongclouddemo.switchbutton.SwitchButton;
import com.gongyou.rongclouddemo.utils.CharacterParser;
import com.gongyou.rongclouddemo.utils.Constant;
import com.gongyou.rongclouddemo.utils.SelectableRoundedImageView;
import com.gongyou.rongclouddemo.utils.rong.BroadcastManager;
import com.gongyou.rongclouddemo.utils.rong.RongGenerate;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.utilities.PromptPopupDialog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import okhttp3.RequestBody;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class GroupDetailActivity extends AppCompatActivity {
    @BindView(R.id.btn_left)
    ImageView leftBtn;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.gridview)
    DemoGridView gridView;
    @BindView(R.id.sw_group_notfaction)
    SwitchButton switchButton;
    @BindView(R.id.group_clean)
    LinearLayout cleanChatBtn;
    @BindView(R.id.group_dismiss)
    Button mDismissBtn;
    @BindView(R.id.group_quit)
    Button mQuitBtn;
    private String fromConversationId;
    private Conversation.ConversationType mConversationType;
    private List<Groups> mGroupList = new ArrayList<>();
    private List<GroupMember> mGroupMemberList = new ArrayList<>();
    private Groups group;
    private boolean isCreated = false;
    private GridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        ButterKnife.bind(this);
        tvTitle.setText("群组信息");
        fromConversationId = getIntent().getStringExtra("TargetId");
        Log.e("111", "fromConversationId " + fromConversationId );
        mConversationType = (Conversation.ConversationType) getIntent().getSerializableExtra("conversationType");
        getGroups();
        getGroupMembers();
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (group != null){
                        setConverstionNotif(GroupDetailActivity.this, Conversation.ConversationType.GROUP, group.getUserId(), true);
                    }
                }else if (group != null){
                    setConverstionNotif(GroupDetailActivity.this, Conversation.ConversationType.GROUP, group.getUserId(), false);
                }
            }
        });

        mDismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogWithYesOrNoUtils.getInstance().showDialog(GroupDetailActivity.this, "确认解散群组", new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void executeEvent() {
                        MyApp.getmResponseInfoAPI().dissmissGroup(getRequestBody(new DismissGroupRequest(group.getUserId())))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<QuitGroupResponse>() {
                                    @Override
                                    public void call(QuitGroupResponse quitGroupResponse) {
                                        Log.e("111", "call: " + quitGroupResponse );
                                        if (quitGroupResponse.getCode() == 200){
                                            Log.e("111", "call: 向服务器请求解散群组成功。" );
                                            RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, fromConversationId, new RongIMClient.ResultCallback<Conversation>() {
                                                @Override
                                                public void onSuccess(Conversation conversation) {
                                                    RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, fromConversationId, new RongIMClient.ResultCallback<Boolean>() {
                                                        @Override
                                                        public void onSuccess(Boolean aBoolean) {
                                                            RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, fromConversationId, new RongIMClient.ResultCallback<Boolean>() {
                                                                @Override
                                                                public void onSuccess(Boolean aBoolean) {
                                                                    Log.e("111", "removeConversation:onSuccess "  );
                                                                }

                                                                @Override
                                                                public void onError(RongIMClient.ErrorCode errorCode) {
                                                                    Log.e("111", "removeConversation:onError "  + errorCode );
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onError(RongIMClient.ErrorCode errorCode) {
                                                            Log.e("111", "clearMessages onError: " + errorCode );
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onError(RongIMClient.ErrorCode errorCode) {
                                                    Log.e("111", "getConversation onError: " + errorCode );
                                                }
                                            });
                                        }
                                        BroadcastManager.getInstance(GroupDetailActivity.this).sendBroadcast(Constant.GROUP_LIST_UPDATE);
                                        startActivity(new Intent(GroupDetailActivity.this,MainActivity.class));
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                });
                    }

                    @Override
                    public void executeEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
            }
        });

        mQuitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogWithYesOrNoUtils.getInstance().showDialog(GroupDetailActivity.this, "确认退出群组", new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void executeEvent() {
                        if (group == null){
                            RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, fromConversationId, new RongIMClient.ResultCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {
                                    Log.e("111", "group==null removeConversation:onSuccess "  );
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {

                                }
                            });
                            return;
                        }
                        MyApp.getmResponseInfoAPI().quitGroup(getRequestBody(new QuitGroupRequest(group.getUserId())))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<QuitGroupResponse>() {
                                    @Override
                                    public void call(QuitGroupResponse quitGroupResponse) {
                                        if (quitGroupResponse.getCode() == 200){
                                            RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, fromConversationId, new RongIMClient.ResultCallback<Conversation>() {
                                                @Override
                                                public void onSuccess(Conversation conversation) {
                                                    RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, fromConversationId, new RongIMClient.ResultCallback<Boolean>() {
                                                        @Override
                                                        public void onSuccess(Boolean aBoolean) {
                                                            RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, fromConversationId, new RongIMClient.ResultCallback<Boolean>() {
                                                                @Override
                                                                public void onSuccess(Boolean aBoolean) {
                                                                    Log.e("111", "quit removeConversation:onSuccess "  );
                                                                }

                                                                @Override
                                                                public void onError(RongIMClient.ErrorCode errorCode) {

                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onError(RongIMClient.ErrorCode e) {

                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onError(RongIMClient.ErrorCode e) {

                                                }
                                            });
                                        }
                                        BroadcastManager.getInstance(GroupDetailActivity.this).sendBroadcast(Constant.GROUP_LIST_UPDATE);
                                        startActivity(new Intent(GroupDetailActivity.this,MainActivity.class));
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                });
                    }

                    @Override
                    public void executeEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
            }
        });

        cleanChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PromptPopupDialog.newInstance(GroupDetailActivity.this,
                        "确定删除聊天记录吗？").setLayoutRes(io.rong.imkit.R.layout.rc_dialog_popup_prompt_warning)
                        .setPromptButtonClickedListener(new PromptPopupDialog.OnPromptButtonClickedListener() {
                            @Override
                            public void onPositiveButtonClicked() {
                                if (RongIM.getInstance() != null) {
                                    if (group != null) {
                                        RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, group.getUserId(), new RongIMClient.ResultCallback<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean aBoolean) {
//                                                NToast.shortToast(mContext, getString(R.string.clear_success));
                                            }

                                            @Override
                                            public void onError(RongIMClient.ErrorCode errorCode) {
//                                                NToast.shortToast(mContext, getString(R.string.clear_failure));
                                            }
                                        });
                                        RongIMClient.getInstance().cleanRemoteHistoryMessages(Conversation.ConversationType.GROUP, group.getUserId(), System.currentTimeMillis(), null);
                                    }
                                }
                            }
                        }).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private RequestBody getRequestBody(Object obj) {
        String route = new Gson().toJson(obj);
        RequestBody body=RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),route);
//        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), route);
        return body;
    }

    public  void setConverstionNotif(final Context context, Conversation.ConversationType conversationType, String targetId, boolean state) {
        Conversation.ConversationNotificationStatus cns;
        if (state) {
            cns = Conversation.ConversationNotificationStatus.DO_NOT_DISTURB;
        } else {
            cns = Conversation.ConversationNotificationStatus.NOTIFY;
        }
        RongIM.getInstance().setConversationNotificationStatus(conversationType, targetId, cns, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                if (conversationNotificationStatus == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB) {
//                    NToast.shortToast(context, "设置免打扰成功");
                } else if (conversationNotificationStatus == Conversation.ConversationNotificationStatus.NOTIFY) {
//                    NToast.shortToast(context, "取消免打扰成功");
                }

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                Toast.makeText(context, "设置失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class GridAdapter extends BaseAdapter {

        private List<GroupMember> list;
        Context context;


        public GridAdapter(Context context, List<GroupMember> list) {
            if (list.size() >= 31) {
                this.list = list.subList(0, 30);
            } else {
                this.list = list;
            }

            this.context = context;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.social_chatsetting_gridview_item, parent, false);
            }
            SelectableRoundedImageView iv_avatar = (SelectableRoundedImageView) convertView.findViewById(R.id.iv_avatar);
            TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);
            ImageView badge_delete = (ImageView) convertView.findViewById(R.id.badge_delete);

            // 最后一个item，减人按钮
            if (position == getCount() - 1 ) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.icon_btn_deleteperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GroupDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("isDeleteGroupMember", true);
                        intent.putExtra("GroupId", group.getUserId());
                        startActivityForResult(intent, 101);
                    }

                });
            } else if (position == getCount() - 2 ) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.jy_drltsz_btn_addperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GroupDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("isAddGroupMember", true);
                        intent.putExtra("GroupId", group.getUserId());
                        startActivityForResult(intent, 100);

                    }
                });
            } else { // 普通成员
                final GroupMember bean = mGroupMemberList.get(position);
//                Friend friend = SealUserInfoManager.getInstance().getFriendByID(bean.getUserId());
                Friend friend = null;
                String name = null;
                if (friend != null && !TextUtils.isEmpty(friend.getDisplayName())) {
                    tv_username.setText(friend.getDisplayName());
                    name = friend.getDisplayName();
                } else {
                    tv_username.setText(bean.getName());
                    name = bean.getName();
                }

//                String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(bean);
                String  portraitUri = bean.getPortraitUri();
                if (TextUtils.isEmpty(portraitUri)){
                    portraitUri = RongGenerate.generateDefaultAvatar(name,bean.getUserId());
                }
                ImageLoader.getInstance().displayImage(portraitUri, iv_avatar, MyApp.getOptions());
//                Glide.with(GroupDetailActivity.this)
//                        .load(Uri.parse(portraitUri))
//                        .into(iv_avatar);
                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        UserInfo userInfo = new UserInfo(bean.getUserId(), bean.getName(), TextUtils.isEmpty(bean.getPortraitUri()) ? Uri.parse(RongGenerate.generateDefaultAvatar(bean.getName(), bean.getUserId())) : Uri.parse(bean.getPortraitUri()));
//                        Intent intent = new Intent(context, UserDetailActivity.class);
//                        Friend friend = CharacterParser.getInstance().generateFriendFromUserInfo(userInfo);
//                        intent.putExtra("friend", friend);
//                        intent.putExtra("conversationType", Conversation.ConversationType.GROUP.getValue());
//                        //Groups not Serializable,just need group name
//                        intent.putExtra("groupName", group.getName());
//                        intent.putExtra("type", CLICK_CONVERSATION_USER_PORTRAIT);
//                        context.startActivity(intent);
                    }

                });

            }

            return convertView;
        }

        @Override
        public int getCount() {
                return mGroupMemberList.size() + 2;
        }

        @Override
        public Object getItem(int position) {
            return mGroupMemberList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<GroupMember> list) {
            this.list = list;
            notifyDataSetChanged();
        }

    }

    private void getGroupMembers() {
        MyApp.getmResponseInfoAPI().getGroupMember(fromConversationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GetGroupMemberResponse>() {
                    @Override
                    public void call(GetGroupMemberResponse getGroupMemberResponse) {
                        if (getGroupMemberResponse != null && getGroupMemberResponse.getCode() == 200) {
                            List<GetGroupMemberResponse.ResultEntity> result = getGroupMemberResponse.getResult();
                            if (result == null || result.size() == 0)
                                return;
                            for(GetGroupMemberResponse.ResultEntity resultEntity : result){
                                String name = null;
                                GroupMember member = new GroupMember();
                                member.setUserId(resultEntity.getUser().getId());
                                if (!TextUtils.isEmpty(resultEntity.getDisplayName())){
                                    name = resultEntity.getDisplayName();
                                }else{
                                    name = resultEntity.getUser().getNickname();
                                }
                                member.setName(name);
                                String portraitUri = resultEntity.getUser().getPortraitUri();
                                if (TextUtils.isEmpty(portraitUri)){
                                    portraitUri = RongGenerate.generateDefaultAvatar(name,resultEntity.getUser().getId());
                                }
                                member.setPortraitUri(portraitUri);
                                mGroupMemberList.add(member);
                            }
                            gridAdapter = new GridAdapter( GroupDetailActivity.this, mGroupMemberList);

                            gridView.setAdapter(gridAdapter);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    private void addGroups(List<GetGroupResponse.ResultEntity> list) {
        if (list != null && list.size() > 0){
            for(GetGroupResponse.ResultEntity resultEntity : list){
                String portraitUri = resultEntity.getGroup().getPortraitUri();
                if (TextUtils.isEmpty(portraitUri)) {
                    portraitUri = RongGenerate.generateDefaultAvatar(resultEntity.getGroup().getName(), resultEntity.getGroup().getId());
                }
                Groups groups = new Groups();
                groups.setUserId(resultEntity.getGroup().getId());
                groups.setName(resultEntity.getGroup().getName());
                groups.setPortraitUri(portraitUri);
                groups.setRole(String.valueOf(resultEntity.getRole()));
                mGroupList.add(groups);
            }
        }
    }

    private void getGroups() {
        MyApp.getmResponseInfoAPI().getGroups()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GetGroupResponse>() {
                    @Override
                    public void call(GetGroupResponse getGroupResponse) {
                        if (getGroupResponse != null && getGroupResponse.getCode() == 200) {
                            List<GetGroupResponse.ResultEntity> list = getGroupResponse.getResult();
                            addGroups(list);
                            for(Groups groups : mGroupList){
                                if (fromConversationId.equals(groups.getUserId())){
                                    group = groups;
                                    if (group.getRole().equals("0")){
                                        Log.e("111", "call: " + group.getUserId() );
                                        isCreated = true;
                                    }
                                    initGroupData();
                                }
                            }
                        }
                    }
                });
    }

    private void initGroupData() {
        RongIM.getInstance().getConversationNotificationStatus(Conversation.ConversationType.GROUP, group.getUserId(), new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                if (conversationNotificationStatus == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB) {
                    switchButton.setChecked(true);
                } else {
                    switchButton.setChecked(false);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });

        if (isCreated) {
            mDismissBtn.setVisibility(View.VISIBLE);
            mQuitBtn.setVisibility(View.GONE);
        }

    }
}
