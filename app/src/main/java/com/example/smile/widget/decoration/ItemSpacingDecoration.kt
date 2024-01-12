package com.example.smile.widget.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by 咸鱼至尊 on 2024/1/12.
 *
 * RecyclerView item均分
 */
class ItemSpacingDecoration(private val itemSpacing: Int) : RecyclerView.ItemDecoration() {

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)
        // 在此处绘制Item之间的间距，可以使用canvas.drawLine()等方法来绘制
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val itemCount = parent.adapter!!.itemCount
        val position = parent.getChildAdapterPosition(view)

        // 根据RecyclerView的方向计算Item之间的偏移量
        if (parent.layoutManager is LinearLayoutManager) {
            val layoutManager = parent.layoutManager as LinearLayoutManager?
            if (layoutManager!!.orientation == LinearLayoutManager.VERTICAL) {
                if (position != itemCount - 1) {
                    outRect.bottom = itemSpacing / (itemCount - 1)
                }
            } else {
                if (position != itemCount - 1) {
                    outRect.right = itemSpacing / (itemCount - 1)
                }
            }
        }
    }
}