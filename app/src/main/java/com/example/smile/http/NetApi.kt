package com.example.smile.http

/**
 * Created by 咸鱼至尊 on 2022/1/8
 *
 * desc: 网络请求API类
 */
object NetApi {
    /** 全局根路径 */
    const val BaseURL = "http://tools.cretinzp.com/jokes/"

    /** 根据昵称生成头像API根路径 */
    const val GenerateAvatarAPI = "https://api.multiavatar.com"

    /** 首页推荐列表API路径 */
    const val HomeRecommendAPI = "home/recommend"

    /** 首页纯文列表API路径 */
    const val HomeTextAPI = "home/text"

    /** 首页趣图列表API路径 */
    const val HomePictureAPI = "home/pic"

    /** 首页推荐关注列表API路径 */
    const val HomeRecommendFollowAPI = "home/attention/recommend"

    /** 首页搜索段子API路径 */
    const val HomeSearchJokeAPI = "home/jokes/search"

    /** 首页热搜关键词API路径 */
    const val HomeHotSearchAPI = "helper/hot_search"

    /** 当前用户信息API路径 */
    const val UserInfoAPI = "user/info"

    /** 意见反馈API路径 */
    const val FeedbackAPI = "helper/feedback"

    /** 获取登陆验证码API路径 */
    const val GetLoginCodeAPI = "user/login/get_code"

    /** 验证码登陆API路径 */
    const val CodeLoginAPI = "user/login/code"

    /** 密码登陆API路径 */
    const val PasswordLoginAPI = "user/login/psw"

    /** 获取重置验证码API路径 */
    const val GetResetCodeAPI = "user/psw/reset/get_code"

    /** 重置密码API路径 */
    const val ResetPasswordAPI = "user/psw/reset"

    /** 重置密码API路径 */
    const val ModifyPasswordAPI = "user/psw/change"

    /** 首页推送视频API路径 */
    const val NotificationVideoAPI = "douyin/list"

    /** 段子点赞(取消点赞)API路径 */
    const val JokeLikeOrCancelAPI = "jokes/like"

    /** 段子点踩(取消点踩)API路径 */
    const val JokeUnLikeOrCancelAPI = "jokes/unlike"

    /** 用户关注(取消关注)API路径 */
    const val UserFocusOrCancelAPI = "user/attention"

    /** 段子评论列表API路径 */
    const val JokeCommentListAPI = "jokes/comment/list"

    /** 评论点赞(取消点赞)API路径 */
    const val JokeCommentLikeOrCancelAPI = "jokes/comment/like"

    /** 段子评论子列表API路径 */
    const val JokeCommentChildListAPI = "jokes/comment/list/item"

    /** 评论段子API路径 */
    const val CommentJokeAPI = "jokes/comment"

    /** 评论段子子评论API路径 */
    const val ChildCommentJokeAPI = "jokes/comment/item"

    /** 删除主评论API路径 */
    const val DeleteMainCommentAPI = "jokes/comment/delete"

    /** 删除子评论API路径 */
    const val DeleteChildCommentAPI = "jokes/comment/item/delete"

    /** 获取段子收藏状态API路径 */
    const val JokeCollectStateAPI = "jokes/is_collect"

    /** 段子收藏(取消收藏)API路径 */
    const val JokeCollectOrCancelAPI = "jokes/collect"

    /** 更新用户信息API路径 */
    const val UpdateUserInfoAPI = "user/info/update"

    /** 绑定邀请码API路径 */
    const val BindInvitationCodeAPI = "user/bind/invite"

    /** 注销账户API路径 */
    const val LogoutAccountAPI = "user/close_account"

    /** 举报内容(用户/段子)API路径 */
    const val ReportUserOrJokeAPI = "helper/report"

    /** 获取指定ID段子API路径 */
    const val JokeForIdAPI = "jokes/target"

    /** 获取指定ID段子点赞列表API路径 */
    const val JokeLikeListForIdAPI = "jokes/like/list"
}