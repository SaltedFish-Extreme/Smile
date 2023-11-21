package com.example.smile.ui.adapter

import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.drake.channel.receiveEventLive
import com.drake.channel.sendEvent
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.app.AppConfig
import com.example.smile.http.NetApi
import com.example.smile.model.EmptyModel
import com.example.smile.model.JokeCommentChildModel
import com.example.smile.widget.ext.visibleOrInvisible
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.toast.Toaster
import com.huantansheng.easyphotos.ui.widget.PressedTextView

/** 段子评论子列表适配器 */
class JokeCommentChildAdapter(dataList: List<JokeCommentChildModel>, commentId: Int, lifecycleOwner: LifecycleOwner) :
    AppAdapter<JokeCommentChildModel>(R.layout.item_joke_comment_child_list, dataList) {

    init {
        //设置动画效果
        setItemAnimation(AnimationType.ScaleIn)
        //点击列表范围，发送消息事件，传递默认文本
        setOnDebouncedItemClick { _, _, _ ->
            sendEvent(context.getString(R.string.comment_hint), context.getString(R.string.channel_tag_input_hint_enter))
        }
        //点击子评论回复，发送消息事件
        addOnDebouncedChildClick(R.id.reply) { _, _, pos ->
            //传递 回复：被回复人昵称
            sendEvent(
                context.getString(R.string.reply_user, items[pos].commentUser.nickname),
                context.getString(R.string.channel_tag_input_hint_enter)
            )
            //传递 被回复评论ID;是(回复子评论)
            sendEvent("${items[pos].commentItemId};${true}", context.getString(R.string.channel_tag_comment_reply_info))
        }
        addOnDebouncedChildClick(R.id.user_avatar) { _, _, pos ->
            Toaster.show("点击头像 $pos")
        }
        //点击删除子评论
        addOnDebouncedChildClick(R.id.delete) { _, _, pos ->
            lifecycleOwner.scopeNetLife {
                Post<EmptyModel?>(NetApi.DeleteChildCommentAPI) { param("commentId", items[pos].commentItemId) }.await()
                //删除成功，更新列表
                Toaster.show(R.string.delete_success)
                removeAt(pos)
            }.catch {
                //请求失败，吐司错误信息
                Toaster.show(it.message)
            }
        }
        //接收消息事件，更新子评论列表添加数据(需和strings.xml中定义的一致)
        lifecycleOwner.receiveEventLive<JokeCommentChildModel>("channel_tag_reply_child_comment") {
            if (it.commentParentId == commentId) {
                this@JokeCommentChildAdapter.add(0, it)
            }
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: JokeCommentChildModel?) {
        if (item != null) {
            //用户头像
            holder.getView<ShapeableImageView>(R.id.user_avatar).apply {
                if (item.commentUser.userAvatar.isNotEmpty()) {
                    Glide.with(context).load(item.commentUser.userAvatar).placeholder(R.drawable.account).into(this)
                } else {
                    Glide.with(context).load(R.drawable.account).into(this)
                }
            }
            //用户昵称
            holder.getView<TextView>(R.id.user_nickname).text =
                if (!item.isReplyChild) {
                    //如果不是回复子评论的则直接显示该评论用户昵称
                    item.commentUser.nickname
                } else {
                    //否则显示 评论用户昵称->被评论用户昵称
                    context.getString(R.string.child_user, item.commentUser.nickname, item.commentedNickname)
                }
            //评论内容
            holder.getView<TextView>(R.id.user_comment).text = item.content
            //评论时间
            holder.getView<TextView>(R.id.comment_time).text = item.timeStr
            //显示删除按钮
            holder.getView<PressedTextView>(R.id.delete).visibleOrInvisible(item.commentUser.userId.toString() == AppConfig.userId)
        }
    }
}