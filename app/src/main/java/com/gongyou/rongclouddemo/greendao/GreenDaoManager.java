package com.gongyou.rongclouddemo.greendao;


import com.gongyou.rongclouddemo.MyApp;

/**
 * Author: TFJ
 * Date: 2017/6/5.
 */

public class GreenDaoManager {
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private static volatile GreenDaoManager mInstance = null;

    private GreenDaoManager() {
        if (mInstance == null) {
            DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(MyApp.getContext(), "user.db",null);
            mDaoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
            mDaoSession = mDaoMaster.newSession();
        }
    }

    public static GreenDaoManager getInstance() {
        if (mInstance == null) {
            synchronized (GreenDaoManager.class) {
                if (mInstance == null) {
                    mInstance = new GreenDaoManager();
                }
            }
        }
        return mInstance;
    }

    public DaoMaster getMaster() {
        return mDaoMaster;
    }

    public DaoSession getSession() {
        return mDaoSession;
    }

    public DaoSession getNewSession() {
        mDaoSession = mDaoMaster.newSession();
        return mDaoSession;
    }

    public BlackListDao getBlackListDao(){
        return mDaoSession.getBlackListDao();
    }

    public FriendDao getFriendDao(){
        return mDaoSession.getFriendDao();
    }

    public GroupMemberDao getGroupMemberDao(){
        return mDaoSession.getGroupMemberDao();
    }
    public GroupsDao getGroupDao(){
        return mDaoSession.getGroupsDao();
    }
}
