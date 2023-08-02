package com.example.smile.ui.activity

import ando.dialog.core.DialogManager
import android.content.Intent
import android.os.Bundle
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
import com.example.smile.ui.dialog.CustomBottomDialog
import com.example.smile.util.InputTextManager
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.hideSoftKeyboard
import com.example.smile.widget.ext.loadAnimation
import com.example.smile.widget.view.ClearEditText
import com.example.smile.widget.view.CountdownView
import com.example.smile.widget.view.PasswordEditText
import com.example.smile.widget.view.RegexEditText
import com.example.smile.widget.view.SubmitButton
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster

/** 重置密码页 */
class ResetPasswordActivity : AppActivity() {

    private val blankPage: RelativeLayout by lazy { findViewById(R.id.blank_page) }
    private val close: ImageView by lazy { findViewById(R.id.close) }
    private val inputPhone: ClearEditText by lazy { findViewById(R.id.input_phone) }
    private val inputVerificationCode: RegexEditText by lazy { findViewById(R.id.input_verification_code) }
    private val sendVerificationCode: CountdownView by lazy { findViewById(R.id.send_verification_code) }
    private val inputPassword: PasswordEditText by lazy { findViewById(R.id.input_password) }
    private val inputPasswordAgain: PasswordEditText by lazy { findViewById(R.id.input_password_again) }
    private val btnReset: SubmitButton by lazy { findViewById(R.id.btn_reset) }
    private val toLogin: TextView by lazy { findViewById(R.id.to_login) }
    private val encounterProblems: TextView by lazy { findViewById(R.id.encounter_problems) }
    private val loginProtocolReminder: TextView by lazy { findViewById(R.id.login_protocol_reminder) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        //点击空白处隐藏输入法并清除输入框焦点
        blankPage.clickNoRepeat {
            hideSoftKeyboard(this)
            inputPhone.clearFocus()
            inputVerificationCode.clearFocus()
            inputPassword.clearFocus()
            inputPasswordAgain.clearFocus()
        }
        //关闭按钮关闭当前页面
        close.clickNoRepeat { finish() }
        //监听手机号输入框输入完成事件(使发送验证码可用)
        inputPhone.addTextChangedListener {
            sendVerificationCode.isEnabled =
                !it.isNullOrBlank() && it.startsWith("1") && it.length == 11
        }
        //发送验证码
        sendVerificationCode.clickNoRepeat {
            Toaster.show(R.string.verification_code_sent_success)
            sendVerificationCode.start()
        }
        //登录协议Spannable文本
        loginProtocolReminder.movementMethod = ClickableMovementMethod.getInstance() // 保证没有点击背景色
        loginProtocolReminder.text = getString(R.string.login_protocol_reminder)
            //隐私政策
            .replaceSpanFirst("《(?<=《)[^》]+》".toRegex()) { matchResult ->
                HighlightSpan(resources.getColor(R.color.privacy_policy_reminder_colors, null)) {
                    //跳转公告页，传递标题：隐私政策(去掉前后的《》)
                    openActivity<AnnouncementActivity>(
                        "title" to matchResult.value.removePrefix("《").removeSuffix("》")
                    )
                }
            }
            //用户服务协议
            .replaceSpanLast("《(?<=《)[^》]+》".toRegex()) { matchResult ->
                HighlightSpan(resources.getColor(R.color.service_agreement_reminder_color, null)) {
                    //跳转公告页，传递标题：用户服务协议(去掉前后的《》)
                    openActivity<AnnouncementActivity>(
                        "title" to matchResult.value.removePrefix("《").removeSuffix("》")
                    )
                }
            }
        btnReset.run {
            //联动重置按钮和手机号/验证码/密码/重复密码输入框
            InputTextManager.with(this@ResetPasswordActivity).addView(inputPhone)
                .addView(inputVerificationCode).addView(inputPassword).addView(inputPasswordAgain)
                .setMain(this).build()
            this.clickNoRepeat {
                //校验手机号长度
                if (inputPhone.text!!.length != 11) {
                    inputPhone.startAnimation(loadAnimation(R.anim.shake_anim))
                    showError(2000)
                    Toaster.show(getString(R.string.phone_format_error))
                    return@clickNoRepeat
                }
                //校验验证码长度
                if (inputVerificationCode.text!!.length != 6) {
                    inputVerificationCode.startAnimation(loadAnimation(R.anim.shake_anim))
                    showError(2000)
                    Toaster.show(getString(R.string.verification_code_format_error))
                    return@clickNoRepeat
                }
                //校验密码长度
                if (inputPassword.text!!.length !in 6..18) {
                    inputPassword.startAnimation(loadAnimation(R.anim.shake_anim))
                    showError(2000)
                    Toaster.show(getString(R.string.password_format_error))
                    return@clickNoRepeat
                }
                //校验两次密码是否一致
                if (inputPassword.text.toString() != inputPasswordAgain.text.toString()) {
                    inputPassword.startAnimation(loadAnimation(R.anim.shake_anim))
                    inputPasswordAgain.startAnimation(loadAnimation(R.anim.shake_anim))
                    btnReset.showError(2000)
                    Toaster.show(getString(R.string.re_enter_password))
                    return@clickNoRepeat
                }
                //todo 执行重置密码逻辑
            }
        }
        //点击去登陆文本，跳转登陆页面
        toLogin.clickNoRepeat {
            openActivity<LoginActivity>()
        }
        //点击遇到问题显示底部弹窗
        encounterProblems.clickNoRepeat { showBottomDialog() }
    }

    //底部弹窗(BottomDialog)
    private fun showBottomDialog() {
        val bottomDialog = CustomBottomDialog(this, false)
        DialogManager.replaceDialog(bottomDialog).setCancelable(true)
            .setCanceledOnTouchOutside(true).setDimmedBehind(true).show()
    }

    override fun onResume() {
        super.onResume()
        //使顶部和状态栏不重叠
        immersionBar {
            titleBarMarginTop(close)
        }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        //转场动画效果(启动新Activity时淡入)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun finish() {
        super.finish()
        //转场动画效果(结束当前Activity时淡出)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}