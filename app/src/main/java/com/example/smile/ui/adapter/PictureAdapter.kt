package com.example.smile.ui.adapter

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chad.library.adapter.base.animation.ItemAnimator
import com.chad.library.adapter.base.util.setOnDebouncedItemClick
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.drake.net.utils.scopeLife
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.util.PhotoUtils
import com.example.smile.util.vibration
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import com.wgw.photo.preview.PhotoPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

/**
 * 段子图片适配器
 *
 * @param data 图片集合数据源
 * @property lifecycleOwner 生命周期对象
 */
class PictureAdapter(private val lifecycleOwner: Fragment, val data: List<String>) : AppAdapter<String>(R.layout.item_joke_picture, data) {

    init {
        //设置动画
        itemAnimation = CustomAnimation()
        //长按保存图片
        addOnItemChildLongClickListener(R.id.joke_image) { _, _, position ->
            //请求权限
            XXPermissions.with(context).permission(Permission.WRITE_EXTERNAL_STORAGE).request { _, all ->
                if (all) {
                    //保存图片，需在子线程
                    lifecycleOwner.scopeLife(dispatcher = Dispatchers.IO) {
                        val boolean = PhotoUtils.saveFile2Gallery(context, data[position])
                        if (boolean) Toaster.show(R.string.save_succeed) else Toaster.show(R.string.save_failed)
                        //保存完成后取消协程
                        cancel()
                    }
                    //顺便震动一下
                    context.vibration()
                }
            }
            true
        }
        //item点击事件
        setOnDebouncedItemClick { _, view, position ->
            PhotoPreview.with(lifecycleOwner).defaultShowPosition(position).sources(data[position]).build().show(view) // 指定缩略图
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: String?) {
        // 设置item数据
        if (item != null) {
            holder.getView<ShapeableImageView>(R.id.joke_image).apply {
                Glide.with(context).load(item).placeholder(R.drawable.load_picture).transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(this)
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