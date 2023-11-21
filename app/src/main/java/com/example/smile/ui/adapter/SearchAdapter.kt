package com.example.smile.ui.adapter

import android.graphics.Color
import androidx.core.view.isVisible
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.drake.channel.sendEvent
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.app.AppConfig
import com.example.smile.widget.ext.invisible
import com.example.smile.widget.ext.randomColor
import com.example.smile.widget.ext.visibleOrInvisible
import com.example.smile.widget.view.ScaleImageView
import com.hjq.shape.view.ShapeTextView

/**
 * Created by 咸鱼至尊 on 2023/5/30
 *
 * desc: 搜索适配器(type：0:热门搜素，1:历史搜素)
 */
class SearchAdapter(private val type: Int) : AppAdapter<String>(R.layout.item_search_block) {

    init {
        //动画效果
        setItemAnimation(AnimationType.AlphaIn)
        //item点击事件，发送事件，传递点击项内容
        setOnDebouncedItemClick { _, _, position ->
            sendEvent(items[position], context.getString(R.string.channel_tag_click_search_block))
        }
        //历史搜索
        if (type == 1) {
            //长按事件，显示隐藏删除按钮
            setOnItemLongClickListener { _, view, _ ->
                view.findViewById<ScaleImageView>(R.id.search_block_image).apply {
                    visibleOrInvisible(!isVisible)
                }
                true
            }
            //删除按钮点击事件，删除该item，同步更新存储数据
            addOnDebouncedChildClick(R.id.search_block_image) { _, _, position ->
                removeAt(position)
                AppConfig.SearchHistory = items
            }
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: String?) {
        holder.getView<ShapeTextView>(R.id.search_block_text).apply {
            text = item
            //历史搜索块
            if (type == 1) {
                //动态修改边框默认颜色及按下时颜色
                shapeDrawableBuilder.apply {
                    strokeColor = randomColor()
                    strokePressedColor = if (AppConfig.DarkTheme) Color.WHITE else Color.BLACK
                    intoBackground()
                }
                //默认隐藏删除按钮
                holder.getView<ScaleImageView>(R.id.search_block_image).invisible()
            }
        }
    }
}