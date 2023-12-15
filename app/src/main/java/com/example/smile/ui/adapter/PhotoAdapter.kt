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
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter4.animation.ItemAnimator
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.drake.net.utils.scopeNetLife
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.app.AppConfig.MobileNetLoadingPicturesOrNo
import com.example.smile.app.AppConfig.MobileNetUsing
import com.example.smile.util.PhotoUtils
import com.example.smile.util.vibration
import com.example.smile.widget.ext.clickNoRepeat
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.Toaster
import com.scwang.smart.drawable.ProgressDrawable
import com.wgw.photo.preview.PhotoPreview
import com.wgw.photo.preview.ShapeTransformType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

/** 图片预览适配器 */
class PhotoAdapter(
    private val activity: FragmentActivity, private val dataList: List<String>
) : AppAdapter<String>(R.layout.item_load_picture, dataList) {

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
            //弹窗文本点击事件，保存图片
            view.findViewById<ShapeTextView>(R.id.save_picture).clickNoRepeat {
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
            //使用加载中占位
            PhotoPreview.with(activity).progressDrawable(ProgressDrawable()).delayShowProgressTime(0).defaultShowPosition(position)
                .sources(dataList)
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

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: String?) {
        holder.getView<ImageView>(R.id.joke_image).apply {
            if (!MobileNetUsing || MobileNetLoadingPicturesOrNo) {
                //如果当前使用的是wifi网络或者开启了数据流量加载图片开关，则正常显示图片
                Glide.with(context).load(item).placeholder(R.drawable.ic_loading).error(R.drawable.ic_load_error)
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                    .into(this)
            } else {
                //否则直接显示占位图片，设置宽高默认值，否则会显示大图
                Glide.with(context).load(R.drawable.ic_load_picture).override(400, 300)
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                    .into(this)
            }
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
                activity.scopeNetLife(dispatcher = Dispatchers.IO) {
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