package com.gongyou.rongclouddemo;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

public class ConversationActivity extends FragmentActivity {
    private Conversation.ConversationType mConversationType;
    @BindView(R.id.btn_left)
    ImageView leftBtn;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_right)
    ImageView rightBtn;
    private String title;
    private String mTargetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        title = intent.getData().getQueryParameter("title");
        mTargetId = intent.getData().getQueryParameter("targetId");
        if (intent == null || intent.getData() == null)
            return;

        mConversationType = Conversation.ConversationType.valueOf(intent.getData().getLastPathSegment().toUpperCase(Locale.US));
        setActionBarTitle(mConversationType);
        addlistener();
    }

    private void addlistener() {
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (mConversationType == Conversation.ConversationType.GROUP) {
                    intent = new Intent(ConversationActivity.this, GroupDetailActivity.class);
                    intent.putExtra("conversationType", Conversation.ConversationType.GROUP);
                } else if (mConversationType == Conversation.ConversationType.PRIVATE) {
                    intent = new Intent(ConversationActivity.this, PrivateChatDetailActivity.class);
                    intent.putExtra("conversationType", Conversation.ConversationType.PRIVATE);
                }
                intent.putExtra("TargetId",mTargetId);
                if (intent != null){
                    startActivity(intent);
                }
            }
        });
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setActionBarTitle(Conversation.ConversationType mConversationType) {
        if (mConversationType == null)
            return;
        if (mConversationType.equals(Conversation.ConversationType.GROUP)){
            if (!TextUtils.isEmpty(title)){
                tvTitle.setText(title);
            }else {
                tvTitle.setText(mTargetId);
            }
            rightBtn.setImageDrawable(getResources().getDrawable(R.drawable.icon2_menu));
        }else if (mConversationType.equals(Conversation.ConversationType.PRIVATE)){
            rightBtn.setImageDrawable(getResources().getDrawable(R.drawable.icon1_menu));
            if (!TextUtils.isEmpty(title)) {
                if (title.equals("null")) {
                    if (!TextUtils.isEmpty(mTargetId)) {
                        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(mTargetId);
                        if (userInfo != null) {
                            tvTitle.setText(userInfo.getName());
                        }
                    }
                } else {
                    tvTitle.setText(title);
                }

            } else {
                tvTitle.setText(mTargetId);
            }
        }else if (mConversationType.equals(Conversation.ConversationType.SYSTEM)){
            tvTitle.setText(title);
        }else {
            tvTitle.setText("聊天");
        }
    }
}
