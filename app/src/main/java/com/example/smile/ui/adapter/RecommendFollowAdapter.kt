package com.example.smile.ui.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.app.AppConfig
import com.example.smile.model.RecommendFollowModel
import com.example.smile.widget.ext.visibleOrInvisible
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.Toaster

/** 推荐关注适配器 */
class RecommendFollowAdapter : AppAdapter<RecommendFollowModel>() {

    init {
        //设置动画效果
        setItemAnimation(AnimationType.AlphaIn)
        //item点击事件
        setOnDebouncedItemClick { _, _, position ->
            Toaster.show("我被点击了！ $position")
        }
        //子控件点击事件
        addOnDebouncedChildClick(R.id.follow) { _, _, position ->
            Toaster.show("$position")
        }
        addOnDebouncedChildClick(R.id.followed) { _, _, position ->
            Toaster.show("$position")
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: RecommendFollowModel?) {
        if (item != null) {
            //用户头像
            holder.getView<ShapeableImageView>(R.id.user_avatar).run {
                if (item.avatar.isNotEmpty()) {
                    Glide.with(context).load(item.avatar).placeholder(R.drawable.account)
                        .transition(DrawableTransitionOptions.withCrossFade()).into(this)
                } else {
                    Glide.with(context).load(R.drawable.account).into(this)
                }
            }
            //用户昵称
            holder.getView<TextView>(R.id.user_nickname).text = item.nickname
            //发表数
            holder.getView<TextView>(R.id.user_release_num).text = context.getString(R.string.release_num, item.jokesNum)
            //粉丝数
            holder.getView<TextView>(R.id.user_follower_num).text = context.getString(R.string.follower_num, item.fansNum)
            //登录状态是否关注用户(未登录不需要处理)
            if (AppConfig.token.isNotEmpty()) {
                holder.getView<ShapeTextView>(R.id.follow).visibleOrInvisible(!item.isAttention)
                holder.getView<ShapeTextView>(R.id.followed).visibleOrInvisible(item.isAttention)
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): QuickViewHolder {
        //重写方法，多布局对应登录状态
        return if (AppConfig.token.isEmpty()) QuickViewHolder(R.layout.item_recommend_follow_not_login, parent)
        else QuickViewHolder(R.layout.item_recommend_follow_logged_in, parent)
    }
}