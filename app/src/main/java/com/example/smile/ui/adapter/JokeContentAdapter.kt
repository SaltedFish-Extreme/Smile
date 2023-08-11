package com.example.smile.ui.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.util.addOnDebouncedChildClick
import com.chad.library.adapter.base.util.setOnDebouncedItemClick
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.model.JokeContentModel
import com.example.smile.util.decrypt
import com.example.smile.util.vibration
import com.example.smile.widget.ext.gone
import com.example.smile.widget.ext.screenWidth
import com.example.smile.widget.ext.visible
import com.example.smile.widget.ext.visibleOrGone
import com.example.smile.widget.ext.visibleOrInvisible
import com.example.smile.widget.view.DrawableTextView
import com.example.smile.widget.view.RevealViewDislike
import com.example.smile.widget.view.RevealViewLike
import com.example.smile.widget.view.SmartTextView
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.Toaster

/** 段子内容适配器 */
class JokeContentAdapter(private val fragment: Fragment? = null, private val activity: FragmentActivity? = null) :
    AppAdapter<JokeContentModel>(R.layout.item_joke_content) {

    companion object {
        //当前段子位置
        private var location: Int = 0
    }

    /** 弹窗 */
    private val pop by lazy {
        //填充视图
        val view = View.inflate(context, R.layout.item_save_picture, null)
        //初始化
        PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
            //动画效果
            animationStyle = R.style.PopupWindowAnim
            //接收点击外侧事件，点击关闭弹窗
            isOutsideTouchable = true
            View.OnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (isShowing) {
                            dismiss()
                        }
                    }

                    MotionEvent.ACTION_UP -> v.performClick()
                    else -> {}
                }
                true
            }
            //弹窗文本点击事件
            view.findViewById<ShapeTextView>(R.id.save_picture).apply {
                text = context.getString(R.string.copy_joke)
                setOnClickListener {
                    this@JokeContentAdapter.getItem(location)?.let { copyJoke(it.joke.content) }
                    dismiss()
                }
            }
        }
    }

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
        //长按段子内容，显示PopupWindow
        addOnItemChildLongClickListener(R.id.joke_text) { _, view, position ->
            //设置当前段子位置
            location = position
            //获取当前view左上角坐标
            val coordinate = IntArray(2)
            view.getLocationOnScreen(coordinate)
            //显示pop，设置显示位置，段子内容居中偏上位置
            pop.showAtLocation(view, Gravity.NO_GRAVITY, coordinate[0] + context.screenWidth / 3, coordinate[1])
            true
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
                    layoutManager = GridLayoutManager(context, 3)
                    //根据是否有图片来显示隐藏列表
                    visibleOrGone(item.joke.imageUrl.split(",").isNotEmpty())
                    adapter = if (isVisible) {
                        //将图片集合传递到图片适配器
                        PhotoAdapter(fragment, activity, dataList = item.joke.imageUrl.split(",").map { it.decrypt() })
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
            holder.getView<RevealViewDislike>(R.id.reveal_dislike).isChecked = item.info.isUnlike
            //👍的数量
            holder.getView<TextView>(R.id.like_num).text = item.info.likeNum.toString()
            //👎的数量
            holder.getView<TextView>(R.id.dislike_num).text = item.info.disLikeNum.toString()
            //💬的数量
            holder.getView<TextView>(R.id.comment_num).text = item.info.commentNum.toString()
            //分享数量
            holder.getView<TextView>(R.id.share_num).text = item.info.shareNum.toString()
            //👍操作
            holder.getView<RevealViewLike>(R.id.reveal_like).setOnClickListener(object : RevealViewLike.OnClickListener {
                //喜欢控件点击事件回调
                override fun onClick(v: RevealViewLike) {
                    if (v.isChecked) {
                        //喜欢
                        Toaster.show("喜欢！${holder.layoutPosition}")
                        holder.getView<RevealViewDislike>(R.id.reveal_dislike).isChecked = false
                    } else {
                        //取消喜欢
                        Toaster.show("取消喜欢！${holder.layoutPosition}")
                    }
                }
            })
            //👎操作
            holder.getView<RevealViewDislike>(R.id.reveal_dislike).setOnClickListener(object : RevealViewDislike.OnClickListener {
                //不喜欢控件点击事件回调
                override fun onClick(v: RevealViewDislike) {
                    if (v.isChecked) {
                        //不喜欢
                        Toaster.show("不喜欢！${holder.layoutPosition}")
                        holder.getView<RevealViewLike>(R.id.reveal_like).isChecked = false
                    } else {
                        //取消不喜欢
                        Toaster.show("取消不喜欢！${holder.layoutPosition}")
                    }
                }
            })
        }
    }

    /**
     * 复制文本内容
     *
     * @param text 要复制的字符串
     */
    private fun copyJoke(text: String) {
        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("copy_text", text)
        clipboard.setPrimaryClip(clip)
        Toaster.show(context.getString(R.string.copy_succeed))
        //顺便震动一下
        context.vibration()
    }
}