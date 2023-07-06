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
}