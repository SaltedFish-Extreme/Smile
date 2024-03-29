package com.example.smile.ui.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.app.AppConfig.token
import com.example.smile.http.NetApi
import com.example.smile.model.EmptyModel
import com.example.smile.model.RecommendFollowModel
import com.example.smile.ui.activity.LoginActivity
import com.example.smile.widget.ext.invisible
import com.example.smile.widget.ext.visible
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
        addOnDebouncedChildClick(R.id.follow) { _, view, position ->
            //已登录状态
            if (token.isNotEmpty()) {
                //点击关注文本，关注用户
                scopeNet {
                    Post<EmptyModel?>(NetApi.UserFocusOrCancelAPI) {
                        param("status", 1)
                        param("userId", items[position].userId)
                    }.await()
                    Toaster.show(R.string.follow_success)
                    //请求成功，显示已关注
                    view.invisible()
                    recyclerView.layoutManager?.findViewByPosition(position)?.findViewById<ShapeTextView>(R.id.followed)?.visible()
                }.catch {
                    //请求失败，吐司错误信息
                    Toaster.show(it.message)
                }
            } else {
                //未登录状态，跳转登录页面
                Toaster.show(R.string.please_login)
                context.openActivity<LoginActivity>()
            }
        }
        addOnDebouncedChildClick(R.id.followed) { _, view, position ->
            //点击已关注文本，取消关注用户
            scopeNet {
                Post<EmptyModel?>(NetApi.UserFocusOrCancelAPI) {
                    param("status", 0)
                    param("userId", items[position].userId)
                }.await()
                Toaster.show(R.string.follow_cancel)
                //请求成功，显示关注
                view.invisible()
                recyclerView.layoutManager?.findViewByPosition(position)?.findViewById<ShapeTextView>(R.id.follow)?.visible()
            }.catch {
                //请求失败，吐司错误信息
                Toaster.show(it.message)
            }
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
            if (token.isNotEmpty()) {
                holder.getView<ShapeTextView>(R.id.follow).visibleOrInvisible(!item.isAttention)
                holder.getView<ShapeTextView>(R.id.followed).visibleOrInvisible(item.isAttention)
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): QuickViewHolder {
        //重写方法，多布局对应登录状态
        return if (token.isEmpty()) QuickViewHolder(R.layout.item_recommend_follow_not_login_list, parent)
        else QuickViewHolder(R.layout.item_recommend_follow_logged_in_list, parent)
    }
}