package com.example.smile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 首页推送视频数据模型
 *
 * @property commentNum 评论数量
 * @property isAttention 是否关注
 * @property isLike 是否喜欢
 * @property likeNum 点赞数
 * @property shareNum 分享数
 * @property content 段子内容
 * @property jokesId 段子ID
 * @property videoSize 视频尺寸
 * @property videoTime 视频时长
 * @property videoUrl 视频地址
 * @property avatar 用户头像
 * @property nickName 用户昵称
 * @property userId 用户ID
 */
@Parcelize
data class NotificationVideoModel(
    val commentNum: Int,
    val isAttention: Boolean,
    val isLike: Boolean,
    val likeNum: Int,
    val shareNum: Int,
    val content: String,
    val jokesId: Int,
    val videoSize: String,
    val videoTime: Int,
    val videoUrl: String,
    val avatar: String,
    val nickName: String,
    val userId: Int,
) : Parcelable