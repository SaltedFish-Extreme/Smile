package com.example.smile.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 段子信息数据模型 */
@Serializable
data class JokeContentModel(
    @SerialName("info") var info: Info = Info(), @SerialName("joke") var joke: Joke = Joke(), @SerialName("user") var user: User = User()
) {
    /** 段子社交信息 */
    @Serializable
    data class Info(
        @SerialName("commentNum") var commentNum: Int = 0,
        @SerialName("disLikeNum") var disLikeNum: Int = 0,
        @SerialName("isAttention") var isAttention: Boolean = false,
        @SerialName("isLike") var isLike: Boolean = false,
        @SerialName("isUnlike") var isUnlike: Boolean = false,
        @SerialName("likeNum") var likeNum: Int = 0,
        @SerialName("shareNum") var shareNum: Int = 0
    )

    /** 段子内容 */
    @Serializable
    data class Joke(
        @SerialName("addTime") var addTime: String = "",
        @SerialName("audit_msg") var auditMsg: String = "",
        @SerialName("content") var content: String = "",
        @SerialName("hot") var hot: Boolean = false,
        @SerialName("imageSize") var imageSize: String = "",
        @SerialName("imageUrl") var imageUrl: String = "",
        @SerialName("jokesId") var jokesId: Int = 0,
        @SerialName("latitudeLongitude") var latitudeLongitude: String = "",
        @SerialName("showAddress") var showAddress: String = "",
        @SerialName("thumbUrl") var thumbUrl: String = "",
        @SerialName("type") var type: Int = 0,
        @SerialName("userId") var userId: Int = 0,
        @SerialName("videoSize") var videoSize: String = "",
        @SerialName("videoTime") var videoTime: Int = 0,
        @SerialName("videoUrl") var videoUrl: String = ""
    )

    /** 所属用户 */
    @Serializable
    data class User(
        @SerialName("avatar") var avatar: String = "",
        @SerialName("nickName") var nickName: String = "",
        @SerialName("signature") var signature: String = "",
        @SerialName("userId") var userId: Int = 0
    )
}