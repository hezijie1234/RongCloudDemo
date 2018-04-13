package com.gongyou.rongclouddemo.greendao;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.gongyou.rongclouddemo.MyApp;
import com.gongyou.rongclouddemo.greendaobean.Friend;
import com.gongyou.rongclouddemo.greendaobean.GroupMember;
import com.gongyou.rongclouddemo.greendaobean.Groups;
import com.gongyou.rongclouddemo.mvp.bean.ResponseInfo;
import com.gongyou.rongclouddemo.mvp.bean.db.GetGroupMemberResponse;
import com.gongyou.rongclouddemo.mvp.bean.db.GetGroupResponse;
import com.gongyou.rongclouddemo.mvp.bean.db.UserRelationshipResponse;
import com.gongyou.rongclouddemo.mvp.bean.second.UserInfoById;
import com.gongyou.rongclouddemo.network.ResponseInfoAPI;
import com.gongyou.rongclouddemo.network.Retrofit2Manager;
import com.gongyou.rongclouddemo.utils.Constant;
import com.gongyou.rongclouddemo.utils.LogUtil;
import com.gongyou.rongclouddemo.utils.NetUtils;
import com.gongyou.rongclouddemo.utils.PinyinUtils;
import com.gongyou.rongclouddemo.utils.SpUtils;
import com.gongyou.rongclouddemo.utils.rong.BroadcastManager;
import com.gongyou.rongclouddemo.utils.rong.RongGenerate;

import org.greenrobot.greendao.query.QueryBuilder;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by hezijie on 2018/4/9.
 */

public class DBManager {
    private static DBManager mInstance;
    private boolean mHasFetchedFriends = false;
    private boolean mHasFetchedGroups = false;
    private boolean mHasFetchedGroupMembers = false;
    private  ResponseInfoAPI mResponseInfoAPI;

    private LinkedHashMap<String, UserInfo> mUserInfoCache;
    private List<Groups> groupsList;

    public DBManager() {
        mUserInfoCache = new LinkedHashMap<>();
        mResponseInfoAPI = Retrofit2Manager.getInstance().create(ResponseInfoAPI.class);
    }
    public static DBManager getInstance() {
        if (mInstance == null) {
            synchronized (DBManager.class) {
                if (mInstance == null) {
                    mInstance = new DBManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 登录时同步好友，群组，群组成员，黑名单数据
     */
    public void getAllUserInfo() {
        if (!NetUtils.isNetworkAvailable(MyApp.getContext())) {
            return;
        }
        fetchFriends();
        fetchGroups();
    }
    /**
     *
     * 同步朋友信息
     */
    private void fetchFriends() {
        mResponseInfoAPI.getAllUserRelationship()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<UserRelationshipResponse>() {
                    @Override
                    public void call(UserRelationshipResponse userRelationshipResponse) {
                        if (userRelationshipResponse != null && userRelationshipResponse.getCode() == 200){
                            List<UserRelationshipResponse.ResultEntity> list = userRelationshipResponse.getResult();
                            if (list != null && list.size() > 0) {
                                Log.e("111", "call:好友信息 " + list );
                                deleteFriends();
                                saveFriends(list);
                            }
                            mHasFetchedFriends = true;
                            checkFetchComplete();
                        }else {
                            mHasFetchedFriends = true;
                            checkFetchComplete();
                        }
                    }


                });


    }

    /**
     * 同步群组信息
     */
    private void fetchGroups() {
        mHasFetchedGroups = false;
        mResponseInfoAPI.getGroups()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<GetGroupResponse>() {
                    @Override
                    public void call(GetGroupResponse getGroupResponse) {
                        if (getGroupResponse != null && getGroupResponse.getCode() == 200) {
                            List<GetGroupResponse.ResultEntity> list = getGroupResponse.getResult();
                            if (list != null && list.size() > 0) {
                                Log.e("111", "call:群组信息 " + list );
                                deleteGroups();
                                saveGroups(list);
                                //同步群组成员信息
                                fetchGroupMembers();
                            } else {
                                mHasFetchedGroupMembers = true;
                            }
                            mHasFetchedGroups = true;
                            checkFetchComplete();
                        } else {
                            mHasFetchedGroups = true;
                            mHasFetchedGroupMembers = true;
                            checkFetchComplete();
                        }
                    }
                });
    }

    private void fetchGroupMembers() {
        mHasFetchedGroupMembers = false;
        Observable.from(getGroups())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<Groups>() {
                    @Override
                    public void call(final Groups groups) {
                        mResponseInfoAPI.getGroupMember(groups.getUserId())
                                .subscribe(new Action1<GetGroupMemberResponse>() {
                                    @Override
                                    public void call(GetGroupMemberResponse getGroupMemberResponse) {
                                        if (getGroupMemberResponse != null && getGroupMemberResponse.getCode() == 200) {
                                            List<GetGroupMemberResponse.ResultEntity> list = getGroupMemberResponse.getResult();
                                            if (null != list && list.size() > 0) {
                                                deleteGroupMembersByGroupId(groups.getUserId());
                                                saveGroupMembers(list, groups.getUserId());
                                            }
                                            mHasFetchedGroupMembers = true;
                                            checkFetchComplete();
                                        } else {
                                            mHasFetchedGroupMembers = true;
                                            checkFetchComplete();
                                        }
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        LogUtil.e(throwable.getLocalizedMessage());
                                        mHasFetchedGroupMembers = true;
                                        checkFetchComplete();
                                    }
                                });
                    }
                });
    }


    /**
     * @param groupId 根据群组成员所属群的id来删除他们
     */
    public synchronized void deleteGroupMembersByGroupId(String groupId) {
        GroupMemberDao groupMemberDao = GreenDaoManager.getInstance().getGroupMemberDao();
        QueryBuilder<GroupMember> groupMemberQueryBuilder = groupMemberDao.queryBuilder();
        List<GroupMember> list = groupMemberQueryBuilder.where(GroupMemberDao.Properties.GroupId.eq(groupId)).list();
        groupMemberDao.deleteInTx(list);
    }

    public synchronized void saveGroupMembers(List<GetGroupMemberResponse.ResultEntity> list, String groupId) {
        if (list != null && list.size() > 0) {
            List<GroupMember> groupMembers = setCreatedToTop(list, groupId);
            if (groupMembers != null && groupMembers.size() > 0) {
                for (GroupMember groupMember : groupMembers) {
                    if (groupMember != null && TextUtils.isEmpty(groupMember.getPortraitUri())) {
                        String portrait = getPortrait(groupMember);
                        groupMember.setPortraitUri(portrait);
                    }
                }
                if (groupMembers.size() > 0) {
                    for (GroupMember groupMember : groupMembers) {
                        saveOrUpdateGroupMember(groupMember);
                    }
                }
            }
        }
    }

    /**
     * @param groupMember 将数据库中值为groupId和UerId的群主成员信息更新。
     */
    public synchronized void saveOrUpdateGroupMember(GroupMember groupMember) {
        if (groupMember != null) {
            String portrait = groupMember.getPortraitUri();
            if (TextUtils.isEmpty(portrait)) {
                portrait = RongGenerate.generateDefaultAvatar(groupMember.getName(), groupMember.getUserId());
                groupMember.setPortraitUri(portrait);
            }
            GroupMemberDao groupMemberDao = GreenDaoManager.getInstance().getGroupMemberDao();
            List<GroupMember> list = groupMemberDao.queryBuilder().list();
            if (null != list && list.size() > 0){
                for(GroupMember groupMember1 : list){
                    if (groupMember1.getGroupId() == groupMember.getGroupId() && groupMember1.getUserId() == groupMember.getUserId()){
                        groupMember.setId(groupMember1.getId());
                        groupMemberDao.insertOrReplace(groupMember);
                    }
                }
            }
        }
    }

    /**
     * @param groupMember
     * @return
     */
    private String getPortrait(GroupMember groupMember) {
        if (groupMember != null) {
            if (TextUtils.isEmpty(groupMember.getPortraitUri())) {
                if (TextUtils.isEmpty(groupMember.getUserId())) {
                    return null;
                } else {
                    UserInfo userInfo = mUserInfoCache.get(groupMember.getUserId());
                    if (userInfo != null) {
                        if (!TextUtils.isEmpty(userInfo.getPortraitUri().toString())) {
                            return userInfo.getPortraitUri().toString();
                        } else {
                            mUserInfoCache.remove(groupMember.getUserId());
                        }
                    }
                    Friend friend = getFriendById(groupMember.getUserId());
                    if (friend != null) {
                        if (!TextUtils.isEmpty(friend.getPortraitUri())) {
                            return friend.getPortraitUri();
                        }
                    }
                    List<GroupMember> groupMemberList = getGroupMembersWithUserId(groupMember.getUserId());
                    if (groupMemberList != null && groupMemberList.size() > 0) {
                        GroupMember member = groupMemberList.get(0);
                        if (!TextUtils.isEmpty(member.getPortraitUri())) {
                            return member.getPortraitUri();
                        }
                    }
                    String portrait = RongGenerate.generateDefaultAvatar(groupMember.getName(), groupMember.getUserId());
                    userInfo = new UserInfo(groupMember.getUserId(), groupMember.getName(), Uri.parse(portrait));
                    mUserInfoCache.put(groupMember.getUserId(), userInfo);
                    return portrait;
                }
            } else {
                return groupMember.getPortraitUri();
            }
        }
        return null;
    }
    public synchronized List<GroupMember> getGroupMembersWithUserId(String userId) {
        if (TextUtils.isEmpty(userId))
            return null;
        GroupMemberDao groupMemberDao = GreenDaoManager.getInstance().getGroupMemberDao();
        List<GroupMember> list = groupMemberDao.queryBuilder().where(GroupMemberDao.Properties.GroupId.eq(userId)).list();
        return list;
    }
    private synchronized Friend getFriendById(String userId) {
        if (!TextUtils.isEmpty(userId)) {
            FriendDao friendDao = GreenDaoManager.getInstance().getFriendDao();
            List<Friend> friends = friendDao.queryBuilder().where(FriendDao.Properties.UserId.eq(userId)).list();
            if (friends != null && friends.size() > 0)
                return friends.get(0);
        }
        return null;
    }

    private synchronized List<GroupMember> setCreatedToTop(List<GetGroupMemberResponse.ResultEntity> groupMember, String groupId) {
        List<GroupMember> newList = new ArrayList<>();
        GroupMember created = null;
        for (GetGroupMemberResponse.ResultEntity group : groupMember) {
            String groupName = null;
            String groupPortraitUri = null;
            Groups groups = getGroupsById(groupId);
            if (groups != null) {
                groupName = groups.getName();
                groupPortraitUri = groups.getPortraitUri();
            }
//            GroupMember newMember = new GroupMember(groupId,
//                    group.getUser().getId(),
//                    group.getUser().getNickname(),
//                    group.getUser().getPortraitUri(),
//                    group.getDisplayName(),
//                    PinyinUtils.getPinyin(group.getUser().getNickname()),
//                    PinyinUtils.getPinyin(group.getDisplayName()),
//                    groupName,
//                    PinyinUtils.getPinyin(groupName),
//                    groupPortraitUri);
            GroupMember newMember = new GroupMember();
            newMember.setGroupId(groupId);
            newMember.setUserId(group.getUser().getId());
            newMember.setName(group.getUser().getNickname());
            newMember.setPortraitUri(group.getUser().getPortraitUri());
            newMember.setDisplayName(group.getDisplayName());
            newMember.setNameSpelling(PinyinUtils.getPinyin(group.getUser().getNickname()));
            newMember.setGroupNameSpelling(PinyinUtils.getPinyin(groupName));
            newMember.setGroupName(groupName);
            newMember.setGroupPortraitUri(groupPortraitUri);
            newMember.setDisplayNameSpelling(PinyinUtils.getPinyin(group.getDisplayName()));
            if (group.getRole() == 0) {
                created = newMember;
            } else {
                newList.add(newMember);
            }
        }
        if (created != null) {
            newList.add(created);
        }
        Collections.reverse(newList);
        return newList;
    }

    private Groups getGroupsById(String groupId) {
        GroupsDao groupDao = GreenDaoManager.getInstance().getGroupDao();
        QueryBuilder<Groups> where = groupDao.queryBuilder().where(GroupsDao.Properties.UserId.eq(groupId));
        if (where.list().size() > 0){
            return where.list().get(0);
        }
        return null;
    }


    private List<Groups> getGroups() {
        GroupsDao groupDao = GreenDaoManager.getInstance().getGroupDao();
        QueryBuilder<Groups> groupsQueryBuilder = groupDao.queryBuilder();
        return groupsQueryBuilder.list();
    }

    private synchronized void saveGroups(List<GetGroupResponse.ResultEntity> list) {
        final GroupsDao groupDao = GreenDaoManager.getInstance().getGroupDao();
        groupsList = new ArrayList<>();
        long i = 0;
        for(GetGroupResponse.ResultEntity entity : list){
            String portrait = entity.getGroup().getPortraitUri();
            if (TextUtils.isEmpty(portrait)) {
                portrait = RongGenerate.generateDefaultAvatar(entity.getGroup().getName(), entity.getGroup().getId());
            }
            Groups group = new Groups();
            group.setId(++i);
            group.setUserId(entity.getGroup().getId());
            group.setName(entity.getGroup().getName());
            group.setPortraitUri(portrait);
            group.setRole(String.valueOf(entity.getRole()));
            groupDao.insert(group);
            groupsList.add(group);
        }

        //提供群主的头像名称等给融云。
        RongIM.setGroupInfoProvider(new RongIM.GroupInfoProvider() {
            @Override
            public Group getGroupInfo(String s) {
                for(Groups groups : groupsList){
                    //对比融云给的用户id和服务器的用户id
                    if (groups.getUserId().equals(s)){
                        String name = groups.getDisplayName();
                        if (TextUtils.isEmpty(name)){
                            name = groups.getName();
                        }
//                        Log.e("111", "getGroupInfo: " + s + groups.getUserId() + "头像：" + groups.getPortraitUri());
                        return new Group(groups.getUserId(),name,Uri.parse(groups.getPortraitUri()));
                    }
                }
                return null;
            }
        }, true);
    }

    private void deleteGroups() {
        GroupsDao groupDao = GreenDaoManager.getInstance().getGroupDao();
        groupDao.deleteAll();
    }

    private void checkFetchComplete() {
        if (mHasFetchedFriends && mHasFetchedGroups && mHasFetchedGroupMembers) {
            BroadcastManager.getInstance(MyApp.getContext()).sendBroadcast(Constant.FETCH_COMPLETE);
            BroadcastManager.getInstance(MyApp.getContext()).sendBroadcast(Constant.UPDATE_FRIEND);
            BroadcastManager.getInstance(MyApp.getContext()).sendBroadcast(Constant.UPDATE_GROUP);
            BroadcastManager.getInstance(MyApp.getContext()).sendBroadcast(Constant.UPDATE_CONVERSATIONS);
        }
    }

    /**
     * 删除所有的朋友
     */
    public synchronized void deleteFriends() {
//        List<Friend> friends = getFriends();
        FriendDao friendDao = GreenDaoManager.getInstance().getFriendDao();
        friendDao.deleteAll();
    }

    /**
     * @param list 保存所有的朋友信息到数据库
     */
    private void saveFriends(final List<UserRelationshipResponse.ResultEntity> list) {
        FriendDao friendDao = GreenDaoManager.getInstance().getFriendDao();
        final List<Friend> friendList = new ArrayList<>();
        long i = 0;
        for(UserRelationshipResponse.ResultEntity entity : list){
            //判断是否是好友
            if (entity.getStatus() == 20){
                final Friend friend = new Friend();
                friend.setId(++i);
                friend.setUserId(entity.getUser().getId());
                friend.setName(entity.getUser().getNickname());
                friend.setPortraitUri(entity.getUser().getPortraitUri());
                friend.setDisplayName(TextUtils.isEmpty(entity.getDisplayName()) ? entity.getUser().getNickname() : entity.getDisplayName());
                friend.setNameSpelling(PinyinUtils.getPinyin(entity.getUser().getNickname()));
                friend.setDisplayNameSpelling(PinyinUtils.getPinyin(TextUtils.isEmpty(entity.getDisplayName()) ? entity.getUser().getNickname() : entity.getDisplayName()));
                if (TextUtils.isEmpty(friend.getPortraitUri())) {
                    friend.setPortraitUri(getPortrait(friend));
                }
                friendList.add(friend);
                friendDao.insert(friend);
//                friendDao.save(friend);
            }
        }

        //提供用户的头像名称等给融云。
        RongIM.setUserInfoProvider(new RongIM.UserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String s) {
                for(Friend friend : friendList){
                    //对比融云给的用户id和服务器的用户id
                    if (friend.getUserId().equals(s)){
                        Log.e("111", "getUserInfo: " + friend.getPortraitUri() );
                        return new UserInfo(friend.getUserId(),friend.getDisplayName(),Uri.parse(friend.getPortraitUri()));
                    }
                }
                return null;
            }
        }, true);

    }

    /**判断主键是否重复*/
    private boolean isEcho(FriendDao mDao,String id){
        QueryBuilder<Friend> qb = mDao.queryBuilder();
        List<Friend> list = qb.where(FriendDao.Properties.Id.eq(id)).list();
        if (list.size() > 0){
            return true;
        }
        return false;

    }



    /**
     * 获取用户头像,头像为空时会生成默认的头像,此默认头像可能已经存在数据库中,不重新生成
     * 先从缓存读,再从数据库读
     */
    private String getPortrait(Friend friend) {
        if (friend != null) {
            if (TextUtils.isEmpty(friend.getPortraitUri().toString())) {
                if (TextUtils.isEmpty(friend.getUserId())) {
                    return null;
                } else {
                    UserInfo userInfo = mUserInfoCache.get(friend.getUserId());
                    if (userInfo != null) {
                        if (!TextUtils.isEmpty(userInfo.getPortraitUri().toString())) {
                            return userInfo.getPortraitUri().toString();
                        } else {
                            mUserInfoCache.remove(friend.getUserId());
                        }
                    }
//                    List<GroupMember> groupMemberList = getGroupMembersWithUserId(friend.getUserId());
//                    if (groupMemberList != null && groupMemberList.size() > 0) {
//                        GroupMember groupMember = groupMemberList.get(0);
//                        if (!TextUtils.isEmpty(groupMember.getPortraitUri().toString()))
//                            return groupMember.getPortraitUri().toString();
//                    }
                    String portrait = RongGenerate.generateDefaultAvatar(friend.getName(), friend.getUserId());
                    //缓存信息kit会使用,备注名存在时需要缓存displayName
                    String name = friend.getName();
                    if (!TextUtils.isEmpty(friend.getDisplayName())) {
                        name = friend.getDisplayName();
                    }
                    userInfo = new UserInfo(friend.getUserId(), name, Uri.parse(portrait));
                    mUserInfoCache.put(friend.getUserId(), userInfo);
                    return portrait;
                }
            } else {
                return friend.getPortraitUri().toString();
            }
        }
        return null;
    }

    /**
     * @return获取到除了自己外的所有朋友
     */
    public synchronized List<Friend> getFriends() {
//        return DataSupport.findAll(Friend.class);
        FriendDao friendDao = GreenDaoManager.getInstance().getFriendDao();
        QueryBuilder<Friend> friendQueryBuilder = friendDao.queryBuilder();
        friendQueryBuilder.where(FriendDao.Properties.UserId.notEq(SpUtils.getString(MyApp.getContext(), Constant.LOGIN_ID, "")));
        return friendQueryBuilder.list();
    }


    private void fetchGroupsError(Throwable throwable) {

    }


}
