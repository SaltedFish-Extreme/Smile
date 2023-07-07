package com.example.smile.ui.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.drake.serialize.intent.openActivity
import com.drake.spannable.movement.ClickableMovementMethod
import com.drake.spannable.replaceSpanFirst
import com.drake.spannable.replaceSpanLast
import com.drake.spannable.span.HighlightSpan
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.widget.ext.clickNoRepeat
import com.gyf.immersionbar.ktx.immersionBar

/** 登录页 */
class LoginActivity : AppActivity() {

    private val close: ImageView by lazy { findViewById(R.id.close) }
    private val loginInfo: TextView by lazy { findViewById(R.id.login_info) }
    private val loginProtocolReminder: TextView by lazy { findViewById(R.id.login_protocol_reminder) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //使顶部和状态栏不重叠
        immersionBar {
            titleBarMarginTop(close)
        }
        close.clickNoRepeat { finish() }
        //登录协议Spannable文本
        loginProtocolReminder.movementMethod = ClickableMovementMethod.getInstance() // 保证没有点击背景色
        loginProtocolReminder.text = getString(R.string.login_protocol_reminder)
            //隐私政策
            .replaceSpanFirst("《(?<=《)[^》]+》".toRegex()) { matchResult ->
                HighlightSpan(resources.getColor(R.color.orange_protocol, null)) {
                    //跳转公告页，传递标题：隐私政策(去掉前后的《》)
                    openActivity<AnnouncementActivity>("title" to matchResult.value.removePrefix("《").removeSuffix("》"))
                }
            }
            //用户服务协议
            .replaceSpanLast("《(?<=《)[^》]+》".toRegex()) { matchResult ->
                HighlightSpan(resources.getColor(R.color.lime_green, null)) {
                    //跳转公告页，传递标题：用户服务协议(去掉前后的《》)
                    openActivity<AnnouncementActivity>("title" to matchResult.value.removePrefix("《").removeSuffix("》"))
                }
            }
    }
}