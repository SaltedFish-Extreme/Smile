package com.example.smile.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 指定用户信息数据模型 */
@Serializable
data class SpeciUserInfoModel(
    @SerialName("attentionNum") var attentionNum: String = "",
    @SerialName("attentionState") var attentionState: Int = 0,
    @SerialName("avatar") var avatar: String = "",
    @SerialName("collectNum") var collectNum: String = "",
    @SerialName("commentNum") var commentNum: String = "",
    @SerialName("cover") var cover: String = "",
    @SerialName("fansNum") var fansNum: String = "",
    @SerialName("joinTime") var joinTime: String = "",
    @SerialName("jokeLikeNum") var jokeLikeNum: String = "",
    @SerialName("jokesNum") var jokesNum: String = "",
    @SerialName("likeNum") var likeNum: String = "",
    @SerialName("nickname") var nickname: String = "",
    @SerialName("sigbature") var sigbature: String = "",
    @SerialName("userId") var userId: Int = 0
)