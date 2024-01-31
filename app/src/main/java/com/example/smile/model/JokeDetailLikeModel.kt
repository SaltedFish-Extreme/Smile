package com.example.smile.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 指定段子点赞列表数据模型 */
@Serializable
data class JokeDetailLikeModel(
    @SerialName("avatar")
    val avatar: String = "",
    @SerialName("nickname")
    val nickname: String = "",
    @SerialName("user_id")
    val userId: Int = 0
)