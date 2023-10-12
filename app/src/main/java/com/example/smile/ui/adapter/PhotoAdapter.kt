package com.example.smile.ui.adapter

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.animation.ItemAnimator
import com.chad.library.adapter.base.util.setOnDebouncedItemClick
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.drake.net.utils.scopeLife
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.util.PhotoUtils
import com.example.smile.util.vibration
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.Toaster
import com.wgw.photo.preview.PhotoPreview
import com.wgw.photo.preview.ShapeTransformType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

/** 图片预览适配器 */
class PhotoAdapter(
    private val fragment: Fragment? = null, private val activity: FragmentActivity? = null, private val dataList: List<String>
) : AppAdapter<String>(R.layout.item_joke_picture, dataList) {

    companion object {
        //当前图片位置
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
            view.findViewById<ShapeTextView>(R.id.save_picture).setOnClickListener {
                savePicture(location)
                dismiss()
            }
        }
    }

    init {
        //设置动画
        itemAnimation = CustomAnimation()
        //设置图片点击事件，打开大图预览
        setOnDebouncedItemClick { _, _, position ->
            if (fragment != null) {
                PhotoPreview.with(fragment).defaultShowPosition(position).sources(dataList)
                    .onLongClickListener { _, customViewRoot, _ ->
                        location = position
                        //长按显示PopupWindow
                        pop.showAtLocation(customViewRoot, Gravity.CENTER, 0, 0)
                        true
                    }.shapeTransformType(ShapeTransformType.ROUND_RECT).shapeCornerRadius(20).build().show { pos ->
                        val viewByPosition: View? = recyclerView.layoutManager?.findViewByPosition(pos)
                        return@show viewByPosition?.findViewById<View>(R.id.joke_image)
                    }// 指定缩略图
            } else if (activity != null) {
                PhotoPreview.with(activity).defaultShowPosition(position).sources(dataList)
                    .onLongClickListener { _, customViewRoot, _ ->
                        location = position
                        //长按显示PopupWindow
                        pop.showAtLocation(customViewRoot, Gravity.CENTER, 0, 0)
                        true
                    }.shapeTransformType(ShapeTransformType.ROUND_RECT).shapeCornerRadius(20).build().show { pos ->
                        val viewByPosition: View? = recyclerView.layoutManager?.findViewByPosition(pos)
                        return@show viewByPosition?.findViewById<View>(R.id.joke_image)
                    }// 指定缩略图
            }
        }
        //长按保存图片
        setOnItemLongClickListener { _, _, position ->
            savePicture(position)
            true
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: String?) {
        holder.getView<ImageView>(R.id.joke_image).apply {
            //Glide显示图片
            Glide.with(context).load(item).placeholder(R.drawable.load_picture)
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                .into(this)
        }
    }

    /**
     * 保存图片
     *
     * @param position 图片位置
     */
    private fun savePicture(position: Int) {
        //请求权限
        XXPermissions.with(context).permission(Permission.WRITE_EXTERNAL_STORAGE).request { _, all ->
            if (all) {
                //保存图片，需在子线程
                (fragment ?: activity)?.scopeLife(dispatcher = Dispatchers.IO) {
                    val boolean = PhotoUtils.saveFile2Gallery(context, dataList[position])
                    if (boolean) Toaster.show(R.string.save_succeed) else Toaster.show(R.string.save_failed)
                    //保存完成后取消协程
                    cancel()
                }
                //顺便震动一下
                context.vibration()
            }
        }
    }

    /** 自定义动画类 */
    inner class CustomAnimation : ItemAnimator {
        override fun animator(view: View): Animator {
            // 创建三个动画
            val alpha: Animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            val scaleY: Animator = ObjectAnimator.ofFloat(view, "scaleY", 1.3f, 1f)
            val scaleX: Animator = ObjectAnimator.ofFloat(view, "scaleX", 1.3f, 1f)
            scaleY.interpolator = DecelerateInterpolator()
            scaleX.interpolator = DecelerateInterpolator()
            // 多个动画组合，可以使用 AnimatorSet 包装
            val animatorSet = AnimatorSet()
            animatorSet.duration = 350
            animatorSet.play(alpha).with(scaleX).with(scaleY)
            return animatorSet
        }
    }
}