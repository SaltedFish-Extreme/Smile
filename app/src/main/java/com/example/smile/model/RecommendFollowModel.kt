package com.example.smile.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 推荐关注数据模型 */
@Serializable
data class RecommendFollowModel(
    @SerialName("avatar") var avatar: String = "",
    @SerialName("fansNum") var fansNum: String = "",
    @SerialName("isAttention") var isAttention: Boolean = false,
    @SerialName("jokesNum") var jokesNum: String = "",
    @SerialName("nickname") var nickname: String = "",
    @SerialName("userId") var userId: Int = 0
)