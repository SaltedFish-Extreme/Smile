package com.example.smile.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 用户信息数据模型 */
@Serializable
data class UserInfoModel(
    @SerialName("token") var token: String = "",
    @SerialName("type") var type: String = "",
    @SerialName("userInfo") var userInfo: UserInfo = UserInfo()
) {
    /** 用户信息 */
    @Serializable
    data class UserInfo(
        @SerialName("avatar") var avatar: String = "",
        @SerialName("birthday") var birthday: String = "",
        @SerialName("inviteCode") var inviteCode: String = "",
        @SerialName("invitedCode") var invitedCode: String = "",
        @SerialName("nickname") var nickname: String = "",
        @SerialName("sex") var sex: String = "",
        @SerialName("signature") var signature: String = "",
        @SerialName("userId") var userId: Int = 0,
        @SerialName("userPhone") var userPhone: String = ""
    )
}