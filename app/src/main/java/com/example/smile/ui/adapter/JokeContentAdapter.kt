package com.example.smile.ui.adapter

import ando.dialog.core.DialogManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.http.NetApi
import com.example.smile.model.EmptyModel
import com.example.smile.model.JokeContentModel
import com.example.smile.ui.dialog.CustomBottomDialogJokeComment
import com.example.smile.ui.dialog.CustomBottomDialogJokeShare
import com.example.smile.util.decrypt
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.gone
import com.example.smile.widget.ext.invisible
import com.example.smile.widget.ext.visible
import com.example.smile.widget.ext.visibleOrGone
import com.example.smile.widget.ext.visibleOrInvisible
import com.example.smile.widget.view.DrawableTextView
import com.example.smile.widget.view.RevealViewLike
import com.example.smile.widget.view.RevealViewUnlike
import com.example.smile.widget.view.SmartTextView
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.Toaster
import com.huantansheng.easyphotos.ui.widget.PressedImageView

/** 段子内容适配器 */
class JokeContentAdapter(private val activity: FragmentActivity) : AppAdapter<JokeContentModel>(R.layout.item_joke_content) {

    init {
        //设置动画效果
        setItemAnimation(AnimationType.SlideInBottom)
        //item点击事件
        setOnDebouncedItemClick { _, _, position ->
            Toaster.show("我被点击了！ $position")
        }
        //子控件点击事件
        addOnDebouncedChildClick(R.id.omitted) { _, _, position ->
            Toaster.show("$position")
        }
        //点击关注文本，关注用户
        addOnDebouncedChildClick(R.id.concern) { _, view, position ->
            activity.scopeNetLife {
                Post<EmptyModel?>(NetApi.UserFocusOrCancelAPI) {
                    param("status", 1)
                    param("userId", items[position].user.userId)
                }.await()
                //请求成功，显示已关注
                view.invisible()
                recyclerView.layoutManager?.findViewByPosition(position)?.findViewById<ShapeTextView>(R.id.followed)?.visible()
            }.catch {
                //请求失败，吐司错误信息
                Toaster.show(it.message)
            }
        }
        //点击已关注文本，取消关注用户
        addOnDebouncedChildClick(R.id.followed) { _, view, position ->
            activity.scopeNetLife {
                Post<EmptyModel?>(NetApi.UserFocusOrCancelAPI) {
                    param("status", 0)
                    param("userId", items[position].user.userId)
                }.await()
                //请求成功，显示关注
                view.invisible()
                recyclerView.layoutManager?.findViewByPosition(position)?.findViewById<DrawableTextView>(R.id.concern)?.visible()
            }.catch {
                //请求失败，吐司错误信息
                Toaster.show(it.message)
            }
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: JokeContentModel?) {
        if (item != null) {
            //用户头像
            holder.getView<ShapeableImageView>(R.id.user_avatar).run {
                if (item.user.avatar.isNotEmpty()) {
                    Glide.with(context).load(item.user.avatar).placeholder(R.drawable.account).into(this)
                } else {
                    Glide.with(context).load(R.drawable.account).into(this)
                }
            }
            //用户昵称
            holder.getView<TextView>(R.id.user_nickname).text = item.user.nickName
            //用户签名
            holder.getView<TextView>(R.id.user_signature).text = item.user.signature
            //是否热门
            holder.getView<ImageView>(R.id.hot).visibleOrInvisible(item.joke.hot)
            //是否关注
            if (item.info.isAttention) {
                holder.getView<ShapeTextView>(R.id.followed).visible()
                holder.getView<DrawableTextView>(R.id.concern).invisible()
            } else {
                holder.getView<DrawableTextView>(R.id.concern).visible()
                holder.getView<ShapeTextView>(R.id.followed).invisible()
            }
            //段子内容(文本)
            holder.getView<SmartTextView>(R.id.joke_text).text = item.joke.content
            //段子内容(图片)
            if (item.joke.type == 2) {
                holder.getView<RecyclerView>(R.id.joke_picture).apply {
                    //根据是否有图片来显示隐藏列表
                    visibleOrGone(item.joke.imageUrl.split(",").isNotEmpty())
                    adapter = if (isVisible) {
                        //将图片集合传递到图片适配器
                        PhotoAdapter(activity, dataList = item.joke.imageUrl.split(",").map { it.decrypt() })
                    } else {
                        //适配器设置为空
                        null
                    }
                }
            } else {
                //隐藏图片列表
                holder.getView<RecyclerView>(R.id.joke_picture).gone()
            }
            //发布时间
            holder.getView<TextView>(R.id.release_time).text = context.getString(R.string.release_time, item.joke.addTime)
            //是否👍
            holder.getView<RevealViewLike>(R.id.reveal_like).isChecked = item.info.isLike
            //是否👎
            holder.getView<RevealViewUnlike>(R.id.reveal_unlike).isChecked = item.info.isUnlike
            //👍的数量
            val likeNum = holder.getView<TextView>(R.id.like_num)
            likeNum.text = item.info.likeNum.toString()
            //👎的数量
            val unlikeNum = holder.getView<TextView>(R.id.unlike_num)
            unlikeNum.text = item.info.disLikeNum.toString()
            //💬的数量
            holder.getView<TextView>(R.id.comment_num).text = item.info.commentNum.toString()
            //分享数量
            holder.getView<TextView>(R.id.share_num).text = item.info.shareNum.toString()
            //👍操作
            holder.getView<RevealViewLike>(R.id.reveal_like).setOnClickListener(object : RevealViewLike.OnClickListener {
                //喜欢控件点击事件回调
                override fun onClick(v: RevealViewLike) {
                    //发起请求，喜欢(取消喜欢)
                    activity.scopeNetLife {
                        Post<EmptyModel?>(NetApi.JokeLikeOrCancelAPI) {
                            param("id", item.joke.jokesId)
                            param("status", v.isChecked)
                        }.await()
                        //请求成功，点👍数+1/-1
                        "${likeNum.text.toString().toInt() + if (v.isChecked) 1 else -1}".also { likeNum.text = it }
                    }.catch {
                        //请求失败，吐司错误信息，点赞操作回滚
                        Toaster.show(it.message)
                        v.setChecked(!v.isChecked, true)
                    }
                }
            })
            //👎操作
            holder.getView<RevealViewUnlike>(R.id.reveal_unlike).setOnClickListener(object : RevealViewUnlike.OnClickListener {
                //不喜欢控件点击事件回调
                override fun onClick(v: RevealViewUnlike) {
                    //发起请求，不喜欢(取消不喜欢)
                    activity.scopeNetLife {
                        Post<EmptyModel?>(NetApi.JokeUnLikeOrCancelAPI) {
                            param("id", item.joke.jokesId)
                            param("status", v.isChecked)
                        }.await()
                        //请求成功，点👎数+1/-1
                        "${unlikeNum.text.toString().toInt() + if (v.isChecked) 1 else -1}".also { unlikeNum.text = it }
                    }.catch {
                        //请求失败，吐司错误信息，点踩操作回滚
                        Toaster.show(it.message)
                        v.setChecked(!v.isChecked, true)
                    }
                }
            })
            //点击查看评论
            holder.getView<PressedImageView>(R.id.reveal_comment).clickNoRepeat {
                //底部弹窗(BottomDialog)
                val bottomDialog = CustomBottomDialogJokeComment(context, activity, item.joke.jokesId.toString())
                DialogManager.replaceDialog(bottomDialog).setCancelable(true)
                    .setCanceledOnTouchOutside(true).setDimmedBehind(true).show()
            }
            //点击分享段子
            holder.getView<PressedImageView>(R.id.reveal_share).clickNoRepeat {
                //底部弹窗(BottomDialog)
                val bottomDialog = if (item.joke.type == 2) {
                    //图片段子
                    CustomBottomDialogJokeShare(context, activity, item.joke.jokesId.toString(), 1, item.joke.content, item.joke.imageUrl)
                } else {
                    //文本段子
                    CustomBottomDialogJokeShare(context, activity, item.joke.jokesId.toString(), 0, item.joke.content)
                }
                DialogManager.replaceDialog(bottomDialog).setCancelable(true)
                    .setCanceledOnTouchOutside(true).setDimmedBehind(true).show()
            }
        }
    }
}