package com.example.smile.app

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder

/**
 * Created by 咸鱼至尊 on 2023/5/15
 *
 * BaseRecyclerViewAdapterHelper adapter基类
 *
 * 默认实现onCreateViewHolder，使用时需传递数据源泛型和布局资源id参数，数据集合可传可不传，重写onBindViewHolder设置item数据
 */
abstract class AppAdapter<T>(@LayoutRes private val layoutResId: Int, data: List<T> = emptyList()) : BaseQuickAdapter<T, QuickViewHolder>(data) {
    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): QuickViewHolder {
        return QuickViewHolder(layoutResId, parent)
    }
}