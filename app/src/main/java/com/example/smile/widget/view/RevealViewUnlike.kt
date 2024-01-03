package com.example.smile.widget.view

import android.content.Context
import android.util.AttributeSet
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.AppConfig.token
import com.example.smile.ui.activity.LoginActivity
import com.example.smile.util.vibration
import com.example.smile.widget.ext.clickNoRepeat
import com.hjq.toast.Toaster
import per.goweii.reveallayout.RevealLayout

/**
 * Created by 咸鱼至尊 on 2023/5/10
 *
 * desc: 揭示视图(不喜欢)
 */
class RevealViewUnlike @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RevealLayout(context, attrs, defStyleAttr) {

    private lateinit var mOnClickListener: OnClickListener

    override fun initAttr(attrs: AttributeSet) {
        super.initAttr(attrs)
        setCheckWithExpand(true)
        setUncheckWithExpand(false)
        setAnimDuration(400)
        setAllowRevert(false)
    }

    override fun getCheckedLayoutId() = R.layout.view_reveal_unlike_checked

    override fun getUncheckedLayoutId() = R.layout.view_reveal_unlike_unchecked

    fun setOnClickListener(onClickListener: OnClickListener) {
        mOnClickListener = onClickListener
        clickNoRepeat {
            if (token.isNotEmpty()) {
                //登陆过直接走点击事件回调
                mOnClickListener.onClick(this@RevealViewUnlike)
                context.vibration() //震动一下
            } else {
                //否则弹吐司并且不能选中
                Toaster.show(R.string.please_login)
                isChecked = false
                context.openActivity<LoginActivity>()
            }
        }
    }

    interface OnClickListener {
        fun onClick(v: RevealViewUnlike)
    }
}