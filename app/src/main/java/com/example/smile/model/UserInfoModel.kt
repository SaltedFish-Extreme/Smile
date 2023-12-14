package com.example.smile.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 当前用户信息数据模型
 *
 * @property info 用户资源信息数据
 * @property user 用户个人信息数据
 */
@Serializable
data class UserInfoModel(
    @SerialName("info") var info: Info = Info(), @SerialName("user") var user: User = User()
) {
    @Serializable
    data class Info(
        @SerialName("attentionNum") var attentionNum: Int = 0,
        @SerialName("experienceNum") var experienceNum: Int = 0,
        @SerialName("fansNum") var fansNum: Int = 0,
        @SerialName("likeNum") var likeNum: Int = 0
    )

    @Serializable
    data class User(
        @SerialName("avatar") var avatar: String = "",
        @SerialName("birthday") var birthday: String = "",
        @SerialName("inviteCode") var inviteCode: String = "",
        @SerialName("invitedCode") var invitedCode: String? = "",
        @SerialName("nickname") var nickname: String = "",
        @SerialName("sex") var sex: String = "",
        @SerialName("signature") var signature: String = "",
        @SerialName("userId") var userId: Int = 0,
        @SerialName("userPhone") var userPhone: String = ""
    )
}