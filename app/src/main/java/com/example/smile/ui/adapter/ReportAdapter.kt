package com.example.smile.ui.adapter

import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.hjq.shape.view.ShapeTextView

/** 举报适配器 */
class ReportAdapter : AppAdapter<String>(R.layout.item_report_block) {

    /** 记录上次选中的选项视图 */
    lateinit var optionView: ShapeTextView

    /** 提供给外部用于获取选项视图是否已初始化 */
    fun isOptionViewInitialised() = ::optionView.isInitialized

    init {
        //动画效果
        setItemAnimation(AnimationType.AlphaIn)
        //item点击事件，修改item边框颜色，设置选中效果
        setOnDebouncedItemClick { _, view, _ ->
            //获取选项视图是否已初始化
            if (isOptionViewInitialised()) {
                //先将上一个选中的view重置回默认颜色
                optionView.shapeDrawableBuilder.apply {
                    strokeColor = context.getColor(R.color.color_navigation_bar_ripple)
                    intoBackground()
                }
            }
            //设置这次选中view的边框颜色
            (view as ShapeTextView).shapeDrawableBuilder.apply {
                strokeColor = context.getColor(R.color.color_concern)
                intoBackground()
                //保存这次选中的选项视图
                optionView = view
            }
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: String?) {
        holder.getView<ShapeTextView>(R.id.report_block_text).text = item
    }

}