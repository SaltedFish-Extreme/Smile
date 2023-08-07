package com.example.smile.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 七牛云token数据模型 */
@Serializable
data class QiNiuYunTokenModel(
    @SerialName("token")
    var token: String = ""
)