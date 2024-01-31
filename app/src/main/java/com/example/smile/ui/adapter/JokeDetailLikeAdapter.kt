package com.example.smile.ui.adapter

import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.model.JokeDetailLikeModel
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.toast.Toaster

/** 段子详情点赞列表适配器 */
class JokeDetailLikeAdapter : AppAdapter<JokeDetailLikeModel>(R.layout.item_joke_detail_like_list) {

    init {
        //动画效果
        setItemAnimation(AnimationType.AlphaIn)
        //item点击事件
        setOnDebouncedItemClick { _, _, position ->
            Toaster.show(position)
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: JokeDetailLikeModel?) {
        if (item != null) {
            //用户头像
            holder.getView<ShapeableImageView>(R.id.user_avatar).run {
                if (item.avatar.isNotEmpty()) {
                    Glide.with(context).load(item.avatar).placeholder(R.drawable.account).into(this)
                } else {
                    Glide.with(context).load(R.drawable.account).into(this)
                }
            }
            //用户昵称
            holder.getView<TextView>(R.id.user_nickname).text = item.nickname
        }
    }

}