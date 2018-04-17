package com.gongyou.rongclouddemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.gongyou.rongclouddemo.greendaobean.GroupMember;
import com.gongyou.rongclouddemo.greendaobean.Groups;
import com.gongyou.rongclouddemo.mvp.bean.db.GetGroupMemberResponse;
import com.gongyou.rongclouddemo.mvp.bean.db.GetGroupResponse;
import com.gongyou.rongclouddemo.network.ResponseInfoAPI;
import com.gongyou.rongclouddemo.utils.LogUtil;
import com.gongyou.rongclouddemo.utils.rong.RongGenerate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class GroupDetailActivity extends AppCompatActivity {
    @BindView(R.id.btn_left)
    ImageView leftBtn;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    private String fromConversationId;
    private Conversation.ConversationType mConversationType;
    private List<Groups> mGroupList = new ArrayList<>();
    private List<GroupMember> mGroupMemberList = new ArrayList<>();
    private Groups group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        ButterKnife.bind(this);
        fromConversationId = getIntent().getStringExtra("TargetId");
        mConversationType = (Conversation.ConversationType) getIntent().getSerializableExtra("conversationType");
        getGroups();
        getGroupMembers();
    }

    private void getGroupMembers() {
        MyApp.getmResponseInfoAPI().getGroupMember(fromConversationId)
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
                                if (TextUtils.isEmpty(resultEntity.getDisplayName())){
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
                .observeOn(Schedulers.io())
                .subscribe(new Action1<GetGroupResponse>() {
                    @Override
                    public void call(GetGroupResponse getGroupResponse) {
                        if (getGroupResponse != null && getGroupResponse.getCode() == 200) {
                            List<GetGroupResponse.ResultEntity> list = getGroupResponse.getResult();
                            addGroups(list);
                            for(Groups groups : mGroupList){
                                if (fromConversationId.equals(groups.getUserId())){
                                    group = groups;
                                }
                            }
                        }
                    }
                });
    }
}
