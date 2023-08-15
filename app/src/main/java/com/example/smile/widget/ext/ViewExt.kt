package com.example.smile.widget.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.AnimRes
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.AppConfig
import com.example.smile.ui.activity.LoginActivity
import com.hjq.toast.Toaster

/** view扩展函数 */

/** 设置view显示 */
fun View.visible() {
    visibility = View.VISIBLE
}

/** 设置view占位隐藏 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/** 根据条件设置view显示隐藏 为true 显示，为false 隐藏 */
fun View.visibleOrGone(flag: Boolean) {
    visibility = if (flag) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

/** 根据条件设置view显示隐藏 为true 显示，为false 隐藏 */
fun View.visibleOrInvisible(flag: Boolean) {
    visibility = if (flag) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

/** 设置view隐藏 */
fun View.gone() {
    visibility = View.GONE
}

/** 将view转为bitmap */
@Deprecated("use View.drawToBitmap()")
fun View.toBitmap(scale: Float = 1f, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap? {
    if (this is ImageView) {
        if (drawable is BitmapDrawable) return (drawable as BitmapDrawable).bitmap
    }
    clearFocus()
    val bitmap = createBitmapSafely(
        (width * scale).toInt(), (height * scale).toInt(), config, 1
    )
    if (bitmap != null) {
        Canvas().run {
            setBitmap(bitmap)
            save()
            drawColor(Color.WHITE)
            scale(scale, scale)
            this@toBitmap.draw(this)
            restore()
            setBitmap(null)
        }
    }
    return bitmap
}

fun createBitmapSafely(width: Int, height: Int, config: Bitmap.Config, retryCount: Int): Bitmap? {
    try {
        return Bitmap.createBitmap(width, height, config)
    } catch (e: OutOfMemoryError) {
        e.printStackTrace()
        if (retryCount > 0) {
            System.gc()
            return createBitmapSafely(width, height, config, retryCount - 1)
        }
        return null
    }
}

var lastClickTime = 0L

/**
 * 防止重复点击事件 默认0.5秒内不可重复点击
 *
 * @param interval 时间间隔 默认0.5秒
 * @param action 执行方法
 */
fun View.clickNoRepeat(interval: Long = 500, action: (view: View) -> Unit) {
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (lastClickTime != 0L && (currentTime - lastClickTime < interval)) {
            return@setOnClickListener
        }
        lastClickTime = currentTime
        action(it)
    }
}

fun Any?.notNull(notNullAction: (value: Any) -> Unit, nullAction1: () -> Unit) {
    if (this != null) {
        notNullAction.invoke(this)
    } else {
        nullAction1.invoke()
    }
}

/**
 * 设置防止重复点击事件
 *
 * @param views 需要设置点击事件的view集合
 * @param interval 时间间隔 默认0.5秒
 * @param onClick 点击触发的方法
 */
fun setOnclickNoRepeat(vararg views: View?, interval: Long = 500, onClick: (View) -> Unit) {
    views.forEach {
        it?.clickNoRepeat(interval = interval) { view ->
            onClick.invoke(view)
        }
    }
}

/**
 * 加载动画
 *
 * @param anim 要加载的动画效果资源
 */
fun Context.loadAnimation(@AnimRes anim: Int): Animation = AnimationUtils.loadAnimation(this, anim)

/**
 * 判断登录状态，未登录吐司提示登录，跳转登录页面，否则执行后续逻辑操作
 *
 * @param invoke 已登录状态要执行的操作
 */
fun Context.judgeLoginOperation(invoke: () -> Unit) {
    if (AppConfig.token.isBlank()) {
        Toaster.show(R.string.please_login)
        openActivity<LoginActivity>()
    } else {
        invoke()
    }
}