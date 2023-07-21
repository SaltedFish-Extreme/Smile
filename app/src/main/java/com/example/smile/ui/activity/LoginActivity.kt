package com.example.smile.ui.activity

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.drake.serialize.intent.openActivity
import com.drake.spannable.movement.ClickableMovementMethod
import com.drake.spannable.replaceSpanFirst
import com.drake.spannable.replaceSpanLast
import com.drake.spannable.span.HighlightSpan
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.util.InputTextManager
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.gone
import com.example.smile.widget.ext.hideSoftKeyboard
import com.example.smile.widget.ext.visible
import com.example.smile.widget.view.ClearEditText
import com.example.smile.widget.view.CountdownView
import com.example.smile.widget.view.PasswordEditText
import com.example.smile.widget.view.RegexEditText
import com.example.smile.widget.view.SubmitButton
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster

/** 登录页 */
class LoginActivity : AppActivity() {

    private val blankPage: RelativeLayout by lazy { findViewById(R.id.blank_page) }
    private val close: ImageView by lazy { findViewById(R.id.close) }
    private val loginInfo: TextView by lazy { findViewById(R.id.login_info) }
    private val inputPhone: ClearEditText by lazy { findViewById(R.id.input_phone) }
    private val verificationCodeLogin: RelativeLayout by lazy { findViewById(R.id.verification_code_login) }
    private val inputVerificationCode: RegexEditText by lazy { findViewById(R.id.input_verification_code) }
    private val sendVerificationCode: CountdownView by lazy { findViewById(R.id.send_verification_code) }
    private val inputPassword: PasswordEditText by lazy { findViewById(R.id.input_password) }
    private val btnLogin: SubmitButton by lazy { findViewById(R.id.btn_login) }
    private val loginMethodSwitch: TextView by lazy { findViewById(R.id.login_method_switch) }
    private val encounterProblems: TextView by lazy { findViewById(R.id.encounter_problems) }
    private val loginProtocolReminder: TextView by lazy { findViewById(R.id.login_protocol_reminder) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //使顶部和状态栏不重叠
        immersionBar {
            titleBarMarginTop(close)
        }
        //点击空白处隐藏输入法并清除输入框焦点
        blankPage.clickNoRepeat {
            hideSoftKeyboard(this)
            inputPhone.clearFocus()
            inputVerificationCode.clearFocus()
            inputPassword.clearFocus()
        }
        //关闭按钮关闭当前页面
        close.clickNoRepeat { finish() }
        //监听手机号输入框输入完成事件(使发送验证码可用)
        inputPhone.addTextChangedListener {
            sendVerificationCode.isEnabled = !it.isNullOrBlank() && it.startsWith("1") && it.length == 11
        }
        //发送验证码
        sendVerificationCode.clickNoRepeat {
            Toaster.show(R.string.verification_code_send_hint)
            sendVerificationCode.start()
        }
        //登录协议Spannable文本
        loginProtocolReminder.movementMethod = ClickableMovementMethod.getInstance() // 保证没有点击背景色
        loginProtocolReminder.text = getString(R.string.login_protocol_reminder)
            //隐私政策
            .replaceSpanFirst("《(?<=《)[^》]+》".toRegex()) { matchResult ->
                HighlightSpan(resources.getColor(R.color.privacy_policy_reminder_colors, null)) {
                    //跳转公告页，传递标题：隐私政策(去掉前后的《》)
                    openActivity<AnnouncementActivity>("title" to matchResult.value.removePrefix("《").removeSuffix("》"))
                }
            }
            //用户服务协议
            .replaceSpanLast("《(?<=《)[^》]+》".toRegex()) { matchResult ->
                HighlightSpan(resources.getColor(R.color.service_agreement_reminder_color, null)) {
                    //跳转公告页，传递标题：用户服务协议(去掉前后的《》)
                    openActivity<AnnouncementActivity>("title" to matchResult.value.removePrefix("《").removeSuffix("》"))
                }
            }
        //切换登陆方式
        loginMethodSwitch.clickNoRepeat {
            if (loginInfo.text == getString(R.string.verification_code_login)) {
                loginInfo.text = getString(R.string.password_login)
                loginMethodSwitch.text = getString(R.string.verification_code_login)
                inputVerificationCode.text?.clear()
                inputPassword.visible()
                verificationCodeLogin.gone()
            } else if (loginInfo.text == getString(R.string.password_login)) {
                loginInfo.text = getString(R.string.verification_code_login)
                loginMethodSwitch.text = getString(R.string.password_login)
                inputPassword.text?.clear()
                verificationCodeLogin.visible()
                inputPassword.gone()
            }
        }
        //联动登陆按钮和账号验证码/密码输入框
        btnLogin.let {
            InputTextManager.with(this).addView(inputPhone).addView(inputVerificationCode).setMain(it).build()
            InputTextManager.with(this).addView(inputPhone).addView(inputPassword).setMain(it).build()
            it.clickNoRepeat {
                //登陆按钮显示失败
                btnLogin.showError(2000)
                //账号输入框加载动画效果
                inputPhone.startAnimation(
                    AnimationUtils.loadAnimation(
                        this@LoginActivity, R.anim.shake_anim
                    )
                )
                //账号输入框加载动画效果
                inputVerificationCode.startAnimation(
                    AnimationUtils.loadAnimation(
                        this@LoginActivity, R.anim.shake_anim
                    )
                )
                //账号输入框加载动画效果
                inputPassword.startAnimation(
                    AnimationUtils.loadAnimation(
                        this@LoginActivity, R.anim.shake_anim
                    )
                )
            }
        }
    }
}