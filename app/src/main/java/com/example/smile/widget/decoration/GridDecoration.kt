package com.example.smile.widget.decoration

import android.graphics.Rect
import android.view.View
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.layout.util.SmartUtil

/**
 * Created by chintz on 2018/5/9.
 *
 * 简单的 RecyclerView 网格均分
 */
class GridDecoration(paddingDp: Int, spaceDp: Int, @IntRange(from = 2) spanCount: Int) : RecyclerView.ItemDecoration() {

    private val padding: Int
    private val space: Int
    private val spanCount: Int

    init {
        padding = SmartUtil.dp2px(paddingDp.toFloat())
        space = SmartUtil.dp2px(spaceDp.toFloat())
        this.spanCount = spanCount
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val pos = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter
        val itemCount = adapter?.itemCount ?: 0
        val isLeft = pos % spanCount == 0
        val isRight = (pos + 1) % spanCount == 0
        val isTop = pos < spanCount
        val isBottom = pos > itemCount - spanCount - 1
        outRect.left = if (isLeft) padding else space / 2
        outRect.right = if (isRight) padding else space / 2
        outRect.top = if (isTop) padding else space / 2
        outRect.bottom = if (isBottom) padding else space / 2
    }
}