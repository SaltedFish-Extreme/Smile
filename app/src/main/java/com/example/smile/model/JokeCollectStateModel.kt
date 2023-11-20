package com.example.smile.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 段子收藏状态数据模型 */
@Serializable
data class JokeCollectStateModel(
    @SerialName("is_collect")
    val isCollect: Boolean = false
)