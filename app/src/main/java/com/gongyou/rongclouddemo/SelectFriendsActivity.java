package com.gongyou.rongclouddemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gongyou.rongclouddemo.greendaobean.Friend;
import com.gongyou.rongclouddemo.mvp.bean.db.UserRelationshipResponse;
import com.gongyou.rongclouddemo.mvp.bean.second.AllFriendsInfo;
import com.gongyou.rongclouddemo.mvp.presenter.second.SelectFriendsPresenter;
import com.gongyou.rongclouddemo.mvp.view.second.SelectFriendView;
import com.gongyou.rongclouddemo.network.RequestCode;
import com.gongyou.rongclouddemo.network.ResponseInfoAPI;
import com.gongyou.rongclouddemo.network.Retrofit2Manager;
import com.gongyou.rongclouddemo.utils.CharacterParser;
import com.gongyou.rongclouddemo.utils.PinyinComparator;
import com.gongyou.rongclouddemo.utils.SelectableRoundedImageView;
import com.gongyou.rongclouddemo.utils.rong.RongGenerate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.mention.MemberMentionedActivity;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.model.UserInfo;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SelectFriendsActivity extends MVPBaseActivity<SelectFriendView,SelectFriendsPresenter> implements SelectFriendView {
    @BindView(R.id.dis_friendlistview)
    ListView mListView;
    @BindView(R.id.dis_show_no_friend)
    TextView mNoFriends;
    @BindView(R.id.create_group_btn)
    TextView mCreateGroupBtn;
    private List<Friend> data_list = new ArrayList<>();
    private List<Friend> sourceDataList = new ArrayList<>();
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser mCharacterParser;
    /**
     * 发起讨论组的 adapter
     */
    private StartDiscussionAdapter adapter;
    private boolean isStartPrivateChat;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_select_friends;
    }
    //被选中的人员集合。
    private List<Friend> mSelectedFriend;
    private LinearLayout mSelectedFriendsLinearLayout;

    @Override
    protected void initView() {
        super.initView();
//        mSelectedFriendsLinearLayout = (LinearLayout) findViewById(R.id.ll_selected_friends);
        mSelectedFriend = new ArrayList<>();
        pinyinComparator = PinyinComparator.getInstance();
        //实例化汉字转拼音类
        mCharacterParser = CharacterParser.getInstance();
        adapter = new StartDiscussionAdapter(SelectFriendsActivity.this, sourceDataList);
        mListView.setAdapter(adapter);
    }
    private List<String> startDisList;
    private List<Friend> createGroupList;
    @Override
    protected void addLisenter() {
        super.addLisenter();
        mCreateGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCBFlag != null && sourceDataList != null && sourceDataList.size() > 0) {
                    startDisList = new ArrayList<>();
                    List<String> disNameList = new ArrayList<>();
                    createGroupList = new ArrayList<>();
                    for (int i = 0; i < sourceDataList.size(); i++) {
                        if (mCBFlag.get(i)) {
                            startDisList.add(sourceDataList.get(i).getUserId());
                            disNameList.add(sourceDataList.get(i).getName());
                            createGroupList.add(sourceDataList.get(i));
                        }
                    }
                }
                if (createGroupList.size() > 0) {
                    Intent intent = new Intent(SelectFriendsActivity.this, CreateGroupActivity.class);
                    intent.putExtra("GroupMember", (Serializable) createGroupList);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SelectFriendsActivity.this, "请至少邀请一位好友创建群组", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void initData() {
        mRootView.setVisibility(View.VISIBLE);
        ResponseInfoAPI mResponseInfoAPI = Retrofit2Manager.getInstance().create(ResponseInfoAPI.class);
        mResponseInfoAPI.getAllUserRelationship()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UserRelationshipResponse>() {
                    @Override
                    public void call(UserRelationshipResponse userRelationshipResponse) {
                        if (userRelationshipResponse != null && userRelationshipResponse.getCode() == 200){
                            List<UserRelationshipResponse.ResultEntity> list = userRelationshipResponse.getResult();
                            Log.e(TAG, "call: user" + list);
                            for(UserRelationshipResponse.ResultEntity entity : list){
                                UserRelationshipResponse.ResultEntity.UserEntity user = entity.getUser();
                                Friend friend = new Friend();
                                friend.setUserId(user.getId());
                                friend.setName(user.getNickname());
                                friend.setPortraitUri(user.getPortraitUri());
                                data_list.add(friend);
                            }
                            fillSourceDataList();
                            updateAdapter();
                        }
                    }
                });

//        mPresenter.getAllFriends();
    }

    private void updateAdapter() {
        adapter.setData(sourceDataList);
        adapter.notifyDataSetChanged();
    }


    private void fillSourceDataList() {
        if (data_list != null && data_list.size() > 0) {
            sourceDataList = filledData(data_list); //过滤数据为有字母的字段  现在有字母 别的数据没有
        } else {
            mNoFriends.setVisibility(View.VISIBLE);
        }

        //还原除了带字母字段的其他数据
        for (int i = 0; i < data_list.size(); i++) {
            sourceDataList.get(i).setName(data_list.get(i).getName());
            sourceDataList.get(i).setUserId(data_list.get(i).getUserId());
            sourceDataList.get(i).setPortraitUri(data_list.get(i).getPortraitUri());
            sourceDataList.get(i).setDisplayName(data_list.get(i).getDisplayName());
        }
        // 根据a-z进行排序源数据
        Collections.sort(sourceDataList, pinyinComparator);
    }

    /**
     * 为ListView填充数据
     */
    private List<Friend> filledData(List<Friend> list) {
        List<Friend> mFriendList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
//            Friend friendModel = new Friend(list.get(i).getUserId(), list.get(i).getName(), list.get(i).getPortraitUri());
            Friend friendModel = new Friend();
            friendModel.setUserId(list.get(i).getUserId());
            friendModel.setName(list.get(i).getName());
            friendModel.setPortraitUri(list.get(i).getPortraitUri());
            //汉字转换成拼音
            String pinyin = null;
            if (!TextUtils.isEmpty(list.get(i).getDisplayName())) {
                pinyin = mCharacterParser.getSpelling(list.get(i).getDisplayName());
            } else if (!TextUtils.isEmpty(list.get(i).getName())) {
                pinyin = mCharacterParser.getSpelling(list.get(i).getName());
            } else {
                UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(list.get(i).getUserId());
                if (userInfo != null) {
                    pinyin = mCharacterParser.getSpelling(userInfo.getName());
                }
            }
            String sortString;
            if (!TextUtils.isEmpty(pinyin)) {
                sortString = pinyin.substring(0, 1).toUpperCase();
            } else {
                sortString = "#";
            }

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                friendModel.setLetters(sortString);
            } else {
                friendModel.setLetters("#");
            }

            mFriendList.add(friendModel);
        }
        return mFriendList;

    }

    @Override
    protected SelectFriendsPresenter initPresenter() {
        return new SelectFriendsPresenter();
    }

    @Override
    public void onError(String msg, RequestCode requestCode) {

    }

    @Override
    public void getAllUserInfo(AllFriendsInfo friendsInfo) {
        Log.e("111", "getAllUserInfo: "+friendsInfo );
    }
    //用于存储CheckBox选中状态
    public Map<Integer, Boolean> mCBFlag;

    public List<Friend> adapterList;
    class StartDiscussionAdapter extends BaseAdapter implements SectionIndexer {

        private Context context;
        private ArrayList<CheckBox> checkBoxList = new ArrayList<>();

        public StartDiscussionAdapter(Context context, List<Friend> list) {
            this.context = context;
            adapterList = list;
            mCBFlag = new HashMap<>();
            init();
        }

        public void setData(List<Friend> friends) {
            adapterList = friends;
            init();
        }

        void init() {
            for (int i = 0; i < adapterList.size(); i++) {
                mCBFlag.put(i, false);
            }
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<Friend> list) {
            adapterList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return adapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return adapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;
            final Friend friend = adapterList.get(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_start_discussion, parent, false);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.dis_friendname);
                viewHolder.tvLetter = (TextView) convertView.findViewById(R.id.dis_catalog);
                viewHolder.mImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.dis_frienduri);
                viewHolder.isSelect = (CheckBox) convertView.findViewById(R.id.dis_select);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                viewHolder.tvLetter.setVisibility(View.VISIBLE);
                viewHolder.tvLetter.setText(friend.getLetters());
            } else {
                viewHolder.tvLetter.setVisibility(View.GONE);
            }
                viewHolder.isSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCBFlag.put(position, viewHolder.isSelect.isChecked());

                    }
                });
            if (TextUtils.isEmpty(adapterList.get(position).getDisplayName())) {
                viewHolder.tvTitle.setText(adapterList.get(position).getName());
            } else {
                viewHolder.tvTitle.setText(adapterList.get(position).getDisplayName());
            }
            String portrait = friend.getPortraitUri();
            if (TextUtils.isEmpty(portrait)){
                portrait = RongGenerate.generateDefaultAvatar(friend.getName(), friend.getUserId());
            }
            Glide.with(SelectFriendsActivity.this)
                    .load(Uri.parse(portrait))
                    .centerCrop()
                    .crossFade()
                    .into(viewHolder.mImageView);
            return convertView;
        }

        private void updateSelectedSizeView(Map<Integer, Boolean> mCBFlag) {
            if (!isStartPrivateChat && mCBFlag != null) {
                int size = 0;
                for (int i = 0; i < mCBFlag.size(); i++) {
                    if (mCBFlag.get(i)) {
                        size++;
                    }
                }
                if (size == 0) {
//                    mHeadRightText.setText("确定");
//                    mSelectedFriendsLinearLayout.setVisibility(View.GONE);
                } else {
//                    mHeadRightText.setText("确定(" + size + ")");
                    List<Friend> selectedList = new ArrayList<>();
                    for (int i = 0; i < sourceDataList.size(); i++) {
                        if (mCBFlag.get(i)) {
                            selectedList.add(sourceDataList.get(i));
                        }
                    }
//                    mSelectedFriendsLinearLayout.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public Object[] getSections() {
            return new Object[0];
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         */
        @Override
        public int getPositionForSection(int sectionIndex) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = adapterList.get(i).getLetters();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == sectionIndex) {
                    return i;
                }
            }

            return -1;
        }

        /**
         * 根据ListView的当前位置获取分类的首字母的Char ascii值
         */
        @Override
        public int getSectionForPosition(int position) {
            return adapterList.get(position).getLetters().charAt(0);
        }


        final class ViewHolder {
            /**
             * 首字母
             */
            TextView tvLetter;
            /**
             * 昵称
             */
            TextView tvTitle;
            /**
             * 头像
             */
            SelectableRoundedImageView mImageView;
            /**
             * userid
             */
//            TextView tvUserId;
            /**
             * 是否被选中的checkbox
             */
            CheckBox isSelect;
        }

    }
}
