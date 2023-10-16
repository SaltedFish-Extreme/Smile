package com.example.smile.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 段子评论子数据模型 */
@Serializable
data class JokeCommentChildModel(
    @SerialName("commentItemId")
    val commentItemId: Int = 0,
    @SerialName("commentParentId")
    val commentParentId: Int = 0,
    @SerialName("commentUser")
    val commentUser: CommentUser = CommentUser(),
    @SerialName("commentedNickname")
    val commentedNickname: String = "",
    @SerialName("commentedUserId")
    val commentedUserId: Int = 0,
    @SerialName("content")
    val content: String = "",
    @SerialName("isReplyChild")
    val isReplyChild: Boolean = false,
    @SerialName("jokeId")
    val jokeId: Int = 0,
    @SerialName("timeStr")
    val timeStr: String = ""
) {
    @Serializable
    data class CommentUser(
        @SerialName("nickname")
        val nickname: String = "",
        @SerialName("userAvatar")
        val userAvatar: String = "",
        @SerialName("userId")
        val userId: Int = 0
    )
}