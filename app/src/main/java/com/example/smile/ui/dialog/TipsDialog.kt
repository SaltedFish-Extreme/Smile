package com.example.smile.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.smile.R
import com.example.smile.widget.view.SmartTextView

/**
 * author : Android 轮子哥
 *
 * github : https://github.com/getActivity/AndroidProject-Kotlin
 *
 * time : 2018/12/2
 *
 * desc : 提示对话框
 */
@Suppress("ClickableViewAccessibility", "unused")
class TipsDialog {

    companion object {
        @SuppressLint("NonConstantResourceId")
        const val ICON_FINISH: Int = R.drawable.ic_tips_finish

        @SuppressLint("NonConstantResourceId")
        const val ICON_ERROR: Int = R.drawable.ic_tips_error

        @SuppressLint("NonConstantResourceId")
        const val ICON_WARNING: Int = R.drawable.ic_tips_warning
    }

    class Builder(context: Context) : Dialog(context), Runnable {

        private val messageView: SmartTextView? by lazy { findViewById(R.id.tv_tips_message) }
        private val iconView: ImageView? by lazy { findViewById(R.id.iv_tips_icon) }

        private var duration = 1000

        init {
            setContentView(R.layout.dialog_tips)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setWindowAnimations(android.R.style.Animation_Toast)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setCancelable(false)
        }

        fun setIcon(@DrawableRes id: Int): Builder = apply {
            iconView?.setImageResource(id)
        }

        fun setDuration(duration: Int): Builder = apply {
            this.duration = duration
        }

        fun setMessage(@StringRes id: Int): Builder = apply {
            setMessage(context.getString(id))
        }

        fun setMessage(text: CharSequence?): Builder = apply {
            messageView?.text = text
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            // 如果显示的图标为空就抛出异常
            requireNotNull(iconView?.drawable) { "The display type must be specified" }
            // 如果内容为空就抛出异常
            require(!TextUtils.isEmpty(messageView?.text.toString())) { "Dialog message not null" }
            super.onCreate(savedInstanceState)
        }

        override fun onStart() {
            super.onStart()
            // 延迟自动关闭
            Handler(Looper.getMainLooper()).postAtTime(this, SystemClock.uptimeMillis() + if (duration < 0) 0 else duration)
        }

        override fun run() {
            if (!isShowing) {
                return
            }
            dismiss()
        }
    }
}