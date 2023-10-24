package com.example.smile.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 登录返回用户信息数据模型 */
@Serializable
data class LoginUserInfoModel(
    @SerialName("token") var token: String = "",
    @SerialName("type") var type: String = "",
    @SerialName("userInfo") var userInfo: UserInfoModel.User = UserInfoModel.User()
)