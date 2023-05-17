package com.example.smile.ui.adapter

import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.drake.net.utils.scopeLife
import com.example.smile.R
import com.example.smile.app.AppAdapter
import com.example.smile.util.PhotoUtils
import com.example.smile.util.decrypt
import com.example.smile.util.vibration
import com.example.smile.widget.imageload.ShowImagesDialog
import com.google.android.material.imageview.ShapeableImageView
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

/**
 * 段子图片适配器
 *
 * @param data 图片集合数据源
 * @property lifecycleOwner 生命周期对象
 */
class PictureAdapter(private val lifecycleOwner: LifecycleOwner, data: List<String>) : AppAdapter<String>(R.layout.item_joke_picture, data) {

    init {
        //点击图片弹窗显示大图
        this.addOnItemChildClickListener(R.id.joke_image) { _, _, position ->
            //传递 上下文对象 图片路径集合 当前图片位置
            ShowImagesDialog(context, data.map { it.decrypt() }, position).show()
        }
        //长按保存图片
        this.addOnItemChildLongClickListener(R.id.joke_image) { _, _, position ->
            //请求权限
            XXPermissions.with(context).permission(Permission.WRITE_EXTERNAL_STORAGE).request { _, all ->
                if (all) {
                    //保存图片，需在子线程
                    lifecycleOwner.scopeLife(dispatcher = Dispatchers.IO) {
                        val boolean = PhotoUtils.saveFile2Gallery(context, data[position].decrypt())
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
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: String?) {
        // 设置item数据
        if (item != null) {
            holder.getView<ShapeableImageView>(R.id.joke_image).apply {
                Glide.with(context).load(item.decrypt()).placeholder(R.drawable.load_picture).transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(this)
            }
        }
    }
}