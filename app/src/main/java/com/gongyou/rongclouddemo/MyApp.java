package com.gongyou.rongclouddemo;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.gongyou.rongclouddemo.network.ResponseInfoAPI;
import com.gongyou.rongclouddemo.network.Retrofit2Manager;
import com.gongyou.rongclouddemo.utils.Constant;
import com.gongyou.rongclouddemo.utils.rong.BroadcastManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallSession;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.display.FadeInBitmapDisplayer;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupNotificationMessageData;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.GroupNotificationMessage;

import static com.gongyou.rongclouddemo.utils.Constant.GROUP_DISMISS;

/**
 * Created by hezijie on 2018/4/3.
 */

public class MyApp extends Application {
    public static int mStatusHeight = 0;
    private static Context context;
    private static Handler handler;
    private static ResponseInfoAPI mResponseInfoAPI;
    private static DisplayImageOptions options;
    public static ResponseInfoAPI getmResponseInfoAPI() {
        return mResponseInfoAPI;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RongIM.init(this);
        context = getApplicationContext();
        handler = new Handler();
        mResponseInfoAPI = Retrofit2Manager.getInstance().create(ResponseInfoAPI.class);
//        setOnReceiverListener();

        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))) {
            options = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(com.gongyou.rongclouddemo.R.drawable.de_default_portrait)
                    .showImageOnFail(com.gongyou.rongclouddemo.R.drawable.de_default_portrait)
                    .showImageOnLoading(com.gongyou.rongclouddemo.R.drawable.de_default_portrait)
                    .displayer(new FadeInBitmapDisplayer(300))
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
        }
    }

    public static DisplayImageOptions getOptions() {
        return options;
    }

    private void setOnReceiverListener() {
        RongIM.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
            @Override
            public boolean onReceived(Message message, int i) {
                MessageContent content = message.getContent();
                if (content instanceof GroupNotificationMessage){
                    GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) content;
                    String groupId = message.getTargetId();
                    GroupNotificationMessageData data = null;
                    try{
                        String currentID = RongIM.getInstance().getCurrentUserId();
                        try{
                            data = jsonToBean(groupNotificationMessage.getData());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        if (groupNotificationMessage.getOperation().equals("Dismiss")){
                            //解散群组。
                            hangUpWhenQuitGroup();
                            handleGroupDismiss(groupId);
                        }else if (groupNotificationMessage.getOperation().equals("Quit")){
                            //退出群组
//                            handleGroupDismiss(groupId);
                            if (data != null) {
                                List<String> quitUserIDs = data.getTargetUserIds();
                                if (quitUserIDs.contains(currentID)) {
                                    hangUpWhenQuitGroup();
                                }
//                                SealUserInfoManager.getInstance().deleteGroupMembers(groupID, quitUserIDs);
                                BroadcastManager.getInstance(context).sendBroadcast(Constant.UPDATE_GROUP_MEMBER, groupId);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                return false;
            }
        });
    }

    private GroupNotificationMessageData jsonToBean(String data) {
        GroupNotificationMessageData dataEntity = new GroupNotificationMessageData();
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has("operatorNickname")) {
                dataEntity.setOperatorNickname(jsonObject.getString("operatorNickname"));
            }
            if (jsonObject.has("targetGroupName")) {
                dataEntity.setTargetGroupName(jsonObject.getString("targetGroupName"));
            }
            if (jsonObject.has("timestamp")) {
                dataEntity.setTimestamp(jsonObject.getLong("timestamp"));
            }
            if (jsonObject.has("targetUserIds")) {
                JSONArray jsonArray = jsonObject.getJSONArray("targetUserIds");
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataEntity.getTargetUserIds().add(jsonArray.getString(i));
                }
            }
            if (jsonObject.has("targetUserDisplayNames")) {
                JSONArray jsonArray = jsonObject.getJSONArray("targetUserDisplayNames");
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataEntity.getTargetUserDisplayNames().add(jsonArray.getString(i));
                }
            }
            if (jsonObject.has("oldCreatorId")) {
                dataEntity.setOldCreatorId(jsonObject.getString("oldCreatorId"));
            }
            if (jsonObject.has("oldCreatorName")) {
                dataEntity.setOldCreatorName(jsonObject.getString("oldCreatorName"));
            }
            if (jsonObject.has("newCreatorId")) {
                dataEntity.setNewCreatorId(jsonObject.getString("newCreatorId"));
            }
            if (jsonObject.has("newCreatorName")) {
                dataEntity.setNewCreatorName(jsonObject.getString("newCreatorName"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataEntity;
    }

    private void handleGroupDismiss(final String groupID) {
        RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, groupID, new RongIMClient.ResultCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, groupID, new RongIMClient.ResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, groupID, null);
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
        BroadcastManager.getInstance(context).sendBroadcast(Constant.GROUP_LIST_UPDATE);
        BroadcastManager.getInstance(context).sendBroadcast(GROUP_DISMISS, groupID);
    }

    private void hangUpWhenQuitGroup() {
        RongCallSession session = RongCallClient.getInstance().getCallSession();
        if (session != null) {
            RongCallClient.getInstance().hangUpCall(session.getCallId());
        }
    }

    public static Handler getHandler() {
        return handler;
    }

    public static Context getContext() {
        return context;
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

}
