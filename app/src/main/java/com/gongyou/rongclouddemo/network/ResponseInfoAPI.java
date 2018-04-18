package com.gongyou.rongclouddemo.network;




import com.gongyou.rongclouddemo.QuitGroupResponse;
import com.gongyou.rongclouddemo.mvp.bean.ResponseInfo;
import com.gongyou.rongclouddemo.mvp.bean.db.CreateGroupResponse;
import com.gongyou.rongclouddemo.mvp.bean.db.GetGroupMemberResponse;
import com.gongyou.rongclouddemo.mvp.bean.db.GetGroupResponse;
import com.gongyou.rongclouddemo.mvp.bean.db.QiNiuTokenResponse;
import com.gongyou.rongclouddemo.mvp.bean.db.UserRelationshipResponse;
import com.gongyou.rongclouddemo.mvp.bean.first.ListFilterInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.AllFriendsInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.FindInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.LoginInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.MsgViewInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.SendSMSInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.StringInfo;
import com.gongyou.rongclouddemo.mvp.bean.second.UserInfoById;
import com.gongyou.rongclouddemo.mvp.bean.second.VerifyCode;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Administrator on 2018/3/13.
 */

public interface ResponseInfoAPI {

    @POST("user/login")
    Observable<ResponseInfo<LoginInfo>> login(@Body RequestBody body);

    //根据 id 去服务端查询用户信息
    @GET("user/{userid}")
    Observable<ResponseInfo<UserInfoById>> getUserInfoById(@Path("userid") String userid);
    //获取发生过用户关系的列表
    @GET("friendship/all")
    Observable<ResponseInfo<AllFriendsInfo>> getAllUserRelation();

    //获取发生过用户关系的列表
    @GET("friendship/all")
    Observable<UserRelationshipResponse> getAllUserRelationship();
    //获取当前用户所属群组列表
    @GET("user/groups")
    Observable<GetGroupResponse> getGroups();

    //根据群id获取群组成员
    @GET("group/{groupId}/members")
    Observable<GetGroupMemberResponse> getGroupMember(@Path("groupId") String groupId);
    @POST("group/create")
    Observable<CreateGroupResponse> createGroup(@Body RequestBody body);

    //得到七牛的token
    @GET("user/get_image_token")
    Observable<QiNiuTokenResponse> getQiNiuToken();

    //用户自行退出群组
    @POST("group/quit")
    Observable<QuitGroupResponse> quitGroup(@Body RequestBody body);

    //创建者解散群组
    @POST("group/dismiss")
    Observable<QuitGroupResponse> dissmissGroup(@Body RequestBody body);
}
