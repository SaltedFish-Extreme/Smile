package com.example.smile.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.StringRes
import com.example.smile.R

/**
 * author : Android 轮子哥
 *
 * github : https://github.com/getActivity/AndroidProject-Kotlin
 *
 * time : 2018/12/2
 *
 * desc : 等待加载对话框
 */
@Suppress("ClickableViewAccessibility", "unused")
class WaitDialog {

    class Builder(context: Context, cancel: Boolean = false) : Dialog(context) {

        private val messageView: TextView? by lazy { findViewById(R.id.tv_wait_message) }

        init {
            setContentView(R.layout.dialog_wait)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setWindowAnimations(android.R.style.Animation_Toast)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setCancelable(cancel)
        }

        fun setMessage(@StringRes id: Int): Builder = apply {
            setMessage(context.getString(id))
        }

        fun setMessage(text: CharSequence?): Builder = apply {
            messageView?.text = text
            messageView?.visibility = if (text == null) View.GONE else View.VISIBLE
        }
    }
}