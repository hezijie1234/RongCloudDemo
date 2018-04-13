package com.gongyou.rongclouddemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gongyou.rongclouddemo.greendaobean.Friend;
import com.gongyou.rongclouddemo.mvp.bean.db.CreateGroupRequest;
import com.gongyou.rongclouddemo.mvp.bean.db.CreateGroupResponse;
import com.gongyou.rongclouddemo.mvp.bean.db.QiNiuTokenResponse;
import com.gongyou.rongclouddemo.mvp.bean.db.UserRelationshipResponse;
import com.gongyou.rongclouddemo.network.ResponseInfoAPI;
import com.gongyou.rongclouddemo.network.Retrofit2Manager;
import com.gongyou.rongclouddemo.utils.BottomMenuDialog;
import com.gongyou.rongclouddemo.utils.Constant;
import com.gongyou.rongclouddemo.utils.PhotoUtils;
import com.gongyou.rongclouddemo.utils.SpUtils;
import com.gongyou.rongclouddemo.utils.rong.BroadcastManager;
import com.google.gson.Gson;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;
import okhttp3.RequestBody;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.gongyou.rongclouddemo.utils.Constant.REFRESH_GROUP_UI;

public class CreateGroupActivity extends AppCompatActivity {
    private EditText groupEdit;
    private List<String> mGroupIds = new ArrayList<>();
    private String mGroupId;
    private Button selectImageBtn;
    private BottomMenuDialog dialog;
    private PhotoUtils photoUtils;
    private Uri selectUri;
    private ResponseInfoAPI mResponseInfoAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        groupEdit = findViewById(R.id.group_name_et);
        selectImageBtn = findViewById(R.id.select_image_btn);
        setPortraitChangeListener();
        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoDialog();
            }
        });
        Intent intent = getIntent();
        Serializable serializable = intent.getSerializableExtra("GroupMember");
        final List<Friend> list =  (List<Friend>)intent.getSerializableExtra("GroupMember");
        Log.e("111", "onCreate: "+list );
        if (null != list && list.size() > 0){
            for(Friend friend : list){
                mGroupIds.add(friend.getUserId());
            }
            mGroupIds.add(SpUtils.getString(CreateGroupActivity.this, Constant.LOGIN_ID,""));
        }

        mResponseInfoAPI = Retrofit2Manager.getInstance().create(ResponseInfoAPI.class);
        findViewById(R.id.create_group_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = groupEdit.getText().toString().trim();
                mResponseInfoAPI.createGroup(getRequestBody(new CreateGroupRequest(groupEdit.getText().toString().trim(),mGroupIds)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CreateGroupResponse>() {
                    @Override
                    public void call(CreateGroupResponse createGroupResponse) {
                        if (createGroupResponse.getCode() == 200) {
                            Log.e("111", "call:创建群组 " + createGroupResponse);
                            BroadcastManager.getInstance(CreateGroupActivity.this).sendBroadcast(REFRESH_GROUP_UI);
                            mGroupId = createGroupResponse.getResult().getId();
                            RongIM.getInstance().startConversation(CreateGroupActivity.this, Conversation.ConversationType.GROUP, mGroupId, groupEdit.getText().toString().trim());

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.e("111", "call: 向服务器请求创建群组失败。" );
                    }
                });
//                mResponseInfoAPI.getAllUserRelationship()
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Action1<UserRelationshipResponse>() {
//                            @Override
//                            public void call(UserRelationshipResponse userRelationshipResponse) {
//                                if (userRelationshipResponse != null && userRelationshipResponse.getCode() == 200){
//                                    List<UserRelationshipResponse.ResultEntity> list = userRelationshipResponse.getResult();
////                                    Log.e(TAG, "call: user" + list);
//                                    for(UserRelationshipResponse.ResultEntity entity : list){
//                                        UserRelationshipResponse.ResultEntity.UserEntity user = entity.getUser();
//                                        Friend friend = new Friend();
//                                        friend.setUserId(user.getId());
//                                        friend.setName(user.getNickname());
//                                        friend.setPortraitUri(user.getPortraitUri());
////                                        data_list.add(friend);
//                                    }
////                                    fillSourceDataList();
////                                    updateAdapter();
//                                }
//                            }
//                        });
            }
        });

    }

    private RequestBody getRequestBody(Object obj) {
        String route = new Gson().toJson(obj);
        RequestBody body=RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),route);
//        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), route);
        return body;
    }

    /**
     * 弹出底部框
     */
    private void showPhotoDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new BottomMenuDialog(CreateGroupActivity.this);
        dialog.setConfirmListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.takePicture(CreateGroupActivity.this);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.selectPicture(CreateGroupActivity.this);
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PhotoUtils.INTENT_CROP:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_SELECT:
                photoUtils.onActivityResult(CreateGroupActivity.this, requestCode, resultCode, data);
                break;
        }
    }
    private String imageUrl;
    private UploadManager uploadManager;
    public void uploadImage(final String domain, String imageToken, Uri imagePath) {
        if (TextUtils.isEmpty(domain) && TextUtils.isEmpty(imageToken) && TextUtils.isEmpty(imagePath.toString())) {
            throw new RuntimeException("upload parameter is null!");
        }
        File imageFile = new File(imagePath.getPath());

        if (this.uploadManager == null) {
            this.uploadManager = new UploadManager();
        }
        this.uploadManager.put(imageFile, null, imageToken, new UpCompletionHandler() {

            @Override
            public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                if (responseInfo.isOK()) {
                    try {
                        String key = (String) jsonObject.get("key");
                        imageUrl = "http://" + domain + "/" + key;
                        Log.e("uploadImage", imageUrl);
                        if (!TextUtils.isEmpty(imageUrl)) {
//                            ImageLoader.getInstance().displayImage(imageUrl, asyncImageView);
//                            LoadDialog.dismiss(mContext);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
//                    NToast.shortToast(mContext, getString(R.string.upload_portrait_failed));
//                    LoadDialog.dismiss(mContext);
                    Toast.makeText(CreateGroupActivity.this, "上传头像失败", Toast.LENGTH_SHORT).show();
                    Log.e("111", "complete: 上传头像失败"  );
                }
            }
        }, null);
    }
    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    selectUri = uri;
//                    request(GET_QI_NIU_TOKEN);
                    mResponseInfoAPI.getQiNiuToken()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<QiNiuTokenResponse>() {
                                @Override
                                public void call(QiNiuTokenResponse qiNiuTokenResponse) {
                                    if (qiNiuTokenResponse.getCode() == 200 ){
                                        uploadImage(qiNiuTokenResponse.getResult().getDomain(), qiNiuTokenResponse.getResult().getToken(), selectUri);
                                    }
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {

                                }
                            });

                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });
    }
}
