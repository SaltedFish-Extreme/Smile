package com.example.smile.ui.adapter

import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.model.JokeContentModel
import com.example.smile.widget.gone
import com.example.smile.widget.view.DrawableTextView
import com.example.smile.widget.view.RevealHappyView
import com.example.smile.widget.view.RevealUnHappyView
import com.example.smile.widget.view.SmartTextView
import com.example.smile.widget.visible
import com.example.smile.widget.visibleOrGone
import com.example.smile.widget.visibleOrInvisible
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.Toaster

/**
 * 段子内容适配器
 *
 * @property lifecycleOwner 生命周期对象(Activity/Fragment)
 */
class JokeContentAdapter(private val lifecycleOwner: LifecycleOwner) : AppAdapter<JokeContentModel>(R.layout.item_joke_content) {

    init {
        //item点击事件
        setOnItemClickListener { _, _, position ->
            Toaster.show("我被点击了！ $position")
        }
        //子控件点击事件
        this.addOnItemChildClickListener(R.id.omitted) { _, _, position ->
            Toaster.show("$position")
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: JokeContentModel?) {
        if (item != null) {
            //用户头像
            holder.getView<ShapeableImageView>(R.id.user_avatar).run {
                if (item.user.avatar.isNotEmpty()) {
                    Glide.with(context).load(item.user.avatar).placeholder(R.drawable.ic_account).into(this)
                } else {
                    Glide.with(context).load(R.drawable.ic_account).into(this)
                }
            }
            //用户昵称
            holder.getView<TextView>(R.id.user_nickname).text = item.user.nickName
            //用户签名
            holder.getView<TextView>(R.id.user_signature).text = item.user.signature
            //是否热门
            holder.getView<ImageView>(R.id.hot).visibleOrInvisible(item.joke.hot)
            //是否关注
            if (item.info.isAttention) holder.getView<ShapeTextView>(R.id.followed).visible()
            else holder.getView<DrawableTextView>(R.id.concern).visible()
            //段子内容(文本)
            holder.getView<SmartTextView>(R.id.joke_text).text = item.joke.content
            //段子内容(图片)
            if (item.joke.type == 2) {
                holder.getView<RecyclerView>(R.id.joke_picture).apply {
                    //根据是否有图片来显示隐藏列表
                    visibleOrGone(item.joke.imageUrl.split(",").isNotEmpty())
                    if (isVisible) {
                        //将图片集合传递到图片适配器
                        adapter = PictureAdapter(lifecycleOwner, item.joke.imageUrl.split(","))
                    }
                }
            } else {
                //隐藏图片列表
                holder.getView<RecyclerView>(R.id.joke_picture).gone()
            }
            //发布时间
            holder.getView<TextView>(R.id.release_time).text = context.getString(R.string.release_time, item.joke.addTime)
            //是否👍
            holder.getView<RevealHappyView>(R.id.reveal_happy).isChecked = item.info.isLike
            //是否👎
            holder.getView<RevealUnHappyView>(R.id.reveal_unhappy).isChecked = item.info.isUnlike
            //👍的数量
            holder.getView<TextView>(R.id.happy_num).text = item.info.likeNum.toString()
            //👎的数量
            holder.getView<TextView>(R.id.unhappy_num).text = item.info.disLikeNum.toString()
            //💬的数量
            holder.getView<TextView>(R.id.comment_num).text = item.info.commentNum.toString()
            //分享数量
            holder.getView<TextView>(R.id.share_num).text = item.info.shareNum.toString()
        }
    }
}