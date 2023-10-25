package com.example.smile.ui.adapter

import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.util.addOnDebouncedChildClick
import com.chad.library.adapter.base.util.setOnDebouncedItemClick
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.drake.channel.sendEvent
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.app.AppConfig
import com.example.smile.http.NetApi
import com.example.smile.http.NetApi.DeleteMainCommentAPI
import com.example.smile.model.EmptyModel
import com.example.smile.model.JokeCommentChildModel
import com.example.smile.model.JokeCommentModel
import com.example.smile.widget.ext.visibleOrInvisible
import com.example.smile.widget.view.RevealViewLikeComment
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.toast.Toaster
import com.huantansheng.easyphotos.ui.widget.PressedTextView

/** 段子评论列表适配器 */
class JokeCommentAdapter(private val lifecycleOwner: LifecycleOwner) :
    AppAdapter<JokeCommentModel.Comment>(R.layout.item_joke_comment_list) {

    init {
        //设置动画效果
        setItemAnimation(AnimationType.ScaleIn)
        //点击列表范围，发送消息事件，传递默认文本
        setOnDebouncedItemClick { _, _, _ ->
            sendEvent(context.getString(R.string.comment_hint), "input_hint_enter")
        }
        //点击主评论回复，发送消息事件
        addOnDebouncedChildClick(R.id.reply) { _, _, pos ->
            //传递 回复：被回复人昵称
            sendEvent(context.getString(R.string.reply_user, items[pos].commentUser.nickname), "input_hint_enter")
            //传递 被回复评论ID;否(不是回复子评论)
            sendEvent("${items[pos].commentId};${false}", "comment_reply_info")
        }
        addOnDebouncedChildClick(R.id.user_avatar) { _, _, pos ->
            Toaster.show("点击头像 $pos")
        }
        //点击删除主评论
        addOnDebouncedChildClick(R.id.delete) { _, _, pos ->
            lifecycleOwner.scopeNetLife {
                Post<EmptyModel?>(DeleteMainCommentAPI) { param("commentId", items[pos].commentId) }.await()
                //删除成功，更新列表
                Toaster.show(R.string.delete_success)
                removeAt(pos)
            }.catch {
                //请求失败，吐司错误信息
                Toaster.show(it.message)
            }
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: JokeCommentModel.Comment?) {
        if (item != null) {
            //用户头像
            holder.getView<ShapeableImageView>(R.id.user_avatar).apply {
                if (item.commentUser.userAvatar.isNotEmpty()) {
                    Glide.with(context).load(item.commentUser.userAvatar).placeholder(R.drawable.ic_account).into(this)
                } else {
                    Glide.with(context).load(R.drawable.ic_account).into(this)
                }
            }
            //用户昵称
            holder.getView<TextView>(R.id.user_nickname).text = item.commentUser.nickname
            //评论内容
            holder.getView<TextView>(R.id.user_comment).text = item.content
            //评论时间
            holder.getView<TextView>(R.id.comment_time).text = item.timeStr
            //是否❤
            holder.getView<RevealViewLikeComment>(R.id.reveal_like).isChecked = item.isLike
            //❤数量
            val likeNum = holder.getView<TextView>(R.id.like_num)
            likeNum.text = item.likeNum.toString()
            //显示删除按钮
            holder.getView<PressedTextView>(R.id.delete).visibleOrInvisible(item.commentUser.userId.toString() == AppConfig.userId)
            //👍评论
            holder.getView<RevealViewLikeComment>(R.id.reveal_like).apply {
                setOnClickListener(object : RevealViewLikeComment.OnClickListener {
                    override fun onClick(v: RevealViewLikeComment) {
                        //发起请求，点赞(取消点赞)
                        lifecycleOwner.scopeNetLife {
                            Post<EmptyModel?>(NetApi.JokeCommentLikeOrCancelAPI) {
                                param("commentId", item.commentId)
                                param("status", isChecked)
                            }.await()
                            //请求成功，点赞数+1/-1
                            "${likeNum.text.toString().toInt() + if (isChecked) 1 else -1}".also { likeNum.text = it }
                        }.catch {
                            //请求失败，吐司错误信息，点赞操作回滚
                            Toaster.show(it.message)
                            setChecked(!isChecked, true)
                        }
                    }
                })
            }
            //有子评论，显示子评论列表
            if (item.itemCommentNum > 0) {
                lifecycleOwner.scopeNetLife {
                    val data = Post<List<JokeCommentChildModel>>(NetApi.JokeCommentChildListAPI) {
                        param("commentId", item.commentId)
                    }.await()
                    //设置列表数据适配器，装载数据，传递评论ID和生命周期对象
                    holder.getView<RecyclerView>(R.id.rv).adapter = JokeCommentChildAdapter(data, item.commentId, lifecycleOwner)
                }.catch {
                    //请求失败，吐司错误信息
                    Toaster.show(it.message)
                }
            } else {
                //没有评论，设置适配器数据为空集合，传递评论ID和生命周期对象
                holder.getView<RecyclerView>(R.id.rv).adapter = JokeCommentChildAdapter(listOf(), item.commentId, lifecycleOwner)
            }
        }
    }
}