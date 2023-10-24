package com.example.smile.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 段子评论数据模型 */
@Serializable
data class JokeCommentModel(
    @SerialName("comments")
    val comments: List<Comment> = listOf(),
    @SerialName("count")
    val count: Int = 0
) {
    @Serializable
    data class Comment(
        @SerialName("commentId")
        val commentId: Int = 0,
        @SerialName("commentUser")
        val commentUser: JokeCommentChildModel.CommentUser = JokeCommentChildModel.CommentUser(),
        @SerialName("content")
        val content: String = "",
        @SerialName("isLike")
        val isLike: Boolean = false,
        @SerialName("itemCommentList")
        val itemCommentList: List<JokeCommentChildModel> = listOf(),
        @SerialName("itemCommentNum")
        val itemCommentNum: Int = 0,
        @SerialName("jokeId")
        val jokeId: Int = 0,
        @SerialName("jokeOwnerUserId")
        val jokeOwnerUserId: Int = 0,
        @SerialName("likeNum")
        val likeNum: Int = 0,
        @SerialName("timeStr")
        val timeStr: String = ""
    )
}