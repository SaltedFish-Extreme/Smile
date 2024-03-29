package com.example.smile.ui.activity

import ando.dialog.core.DialogManager
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.serialize.intent.openActivity
import com.drake.softinput.hideSoftInput
import com.drake.spannable.movement.ClickableMovementMethod
import com.example.smile.R
import com.example.smile.app.ActivityManager
import com.example.smile.app.AppActivity
import com.example.smile.app.AppConfig.token
import com.example.smile.app.AppConfig.userId
import com.example.smile.http.NetApi.CodeLoginAPI
import com.example.smile.http.NetApi.GetLoginCodeAPI
import com.example.smile.http.NetApi.PasswordLoginAPI
import com.example.smile.model.EmptyModel
import com.example.smile.model.LoginUserInfoModel
import com.example.smile.ui.dialog.CustomBottomDialogEncounterProblems
import com.example.smile.util.InputTextManager
import com.example.smile.util.spannableText
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.gone
import com.example.smile.widget.ext.loadAnimation
import com.example.smile.widget.ext.visible
import com.example.smile.widget.view.ClearEditText
import com.example.smile.widget.view.CountdownView
import com.example.smile.widget.view.PasswordEditText
import com.example.smile.widget.view.RegexEditText
import com.example.smile.widget.view.SubmitButton
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import kotlinx.coroutines.delay

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
        //点击空白处隐藏输入法并清除输入框焦点
        blankPage.clickNoRepeat {
            hideSoftInput()
            inputPhone.clearFocus()
            inputVerificationCode.clearFocus()
            inputPassword.clearFocus()
        }
        //关闭按钮关闭当前页面
        close.clickNoRepeat { finish() }
        //监听手机号输入框输入完成事件(使发送验证码可用)
        inputPhone.addTextChangedListener { sendVerificationCode.isEnabled = !it.isNullOrBlank() && it.length == 11 }
        //发送验证码
        sendVerificationCode.clickNoRepeat {
            scopeNetLife {
                //返回数据为null，验证码在小程序查看
                Post<EmptyModel?>(GetLoginCodeAPI) { param("phone", inputPhone.text.toString()) }.await()
                //发送成功，验证码倒计时开始
                Toaster.show(R.string.verification_code_sent_success)
                sendVerificationCode.start()
            }.catch {
                //吐司错误信息
                Toaster.show(it.message)
            }
        }
        //登录协议Spannable文本
        loginProtocolReminder.movementMethod = ClickableMovementMethod.getInstance() // 保证没有点击背景色
        loginProtocolReminder.text = spannableText()
        //切换登陆方式
        loginMethodSwitch.clickNoRepeat {
            if (loginInfo.text == getString(R.string.verification_code_login)) {
                loginInfo.text = getString(R.string.password_login)
                loginMethodSwitch.text = getString(R.string.verification_code_login)
                inputVerificationCode.text?.clear()
                verificationCodeLogin.gone()
                inputPassword.visible()
                //联动登陆按钮和手机号/密码输入框
                InputTextManager.with(this).addView(inputPhone).addView(inputPassword).setMain(btnLogin).build()
            } else if (loginInfo.text == getString(R.string.password_login)) {
                loginInfo.text = getString(R.string.verification_code_login)
                loginMethodSwitch.text = getString(R.string.password_login)
                inputPassword.text?.clear()
                inputPassword.gone()
                verificationCodeLogin.visible()
                //联动登陆按钮和手机号/验证码输入框
                InputTextManager.with(this).addView(inputPhone).addView(inputVerificationCode).setMain(btnLogin).build()
            }
        }
        //点击遇到问题显示底部弹窗
        encounterProblems.clickNoRepeat { showBottomDialog() }
        btnLogin.run {
            //联动登陆按钮和手机号/验证码输入框
            InputTextManager.with(this@LoginActivity).addView(inputPhone).addView(inputVerificationCode).setMain(this).build()
            //点击登录按钮
            clickNoRepeat {
                //校验手机号长度
                if (inputPhone.text!!.length != 11) {
                    inputPhone.startAnimation(loadAnimation(R.anim.shake_anim))
                    showError(2000)
                    Toaster.show(R.string.phone_format_error)
                    return@clickNoRepeat
                }
                if (loginInfo.text == getString(R.string.verification_code_login)) {
                    //校验验证码长度
                    if (inputVerificationCode.text!!.length != 6) {
                        inputVerificationCode.startAnimation(loadAnimation(R.anim.shake_anim))
                        showError(2000)
                        Toaster.show(R.string.verification_code_format_error)
                        return@clickNoRepeat
                    } else {
                        scopeNetLife {
                            //延迟一秒，增强用户体验
                            delay(1000)
                            //验证码登录
                            val data = Post<LoginUserInfoModel>(CodeLoginAPI) {
                                param("phone", inputPhone.text.toString())
                                param("code", inputVerificationCode.text.toString())
                            }.await()
                            loginSuccess(data)
                        }.catch {
                            //登录失败，显示错误，吐司错误信息
                            showError(2000)
                            Toaster.show(it.message)
                        }
                    }
                } else if (loginInfo.text == getString(R.string.password_login)) {
                    //校验密码长度
                    if (inputPassword.text!!.length !in 6..18) {
                        inputPassword.startAnimation(loadAnimation(R.anim.shake_anim))
                        showError(2000)
                        Toaster.show(R.string.password_format_error)
                        return@clickNoRepeat
                    } else {
                        scopeNetLife {
                            //延迟一秒，增强用户体验
                            delay(1000)
                            //密码登录
                            val data = Post<LoginUserInfoModel>(PasswordLoginAPI) {
                                param("phone", inputPhone.text.toString())
                                param("psw", inputPassword.text.toString())
                            }.await()
                            loginSuccess(data)
                        }.catch {
                            //登录失败，显示错误，吐司错误信息
                            showError(2000)
                            Toaster.show(it.message)
                        }
                    }
                }
            }
        }
    }

    //底部弹窗(BottomDialog)
    private fun showBottomDialog() {
        val bottomDialog = CustomBottomDialogEncounterProblems(this, true)
        DialogManager.replaceDialog(bottomDialog).setCancelable(true).setCanceledOnTouchOutside(true).setDimmedBehind(true).show()
    }

    /**
     * 登录成功执行逻辑
     *
     * @param data 用户信息数据
     */
    private suspend fun SubmitButton.loginSuccess(data: LoginUserInfoModel) {
        //隐藏软键盘
        hideSoftInput()
        //登录按钮显示成功
        Toaster.show(R.string.login_succeed)
        showSucceed()
        //设置全局token
        token = data.token
        //保存当前登录用户ID
        userId = data.userInfo.userId.toString()
        //销毁除当前页面外的所有Activity
        ActivityManager.getInstance().finishAllActivities(this@LoginActivity::class.java)
        //延迟一秒
        delay(1000)
        //跳转主页
        openActivity<MainActivity>()
        //关闭页面
        finish()
    }

    override fun onResume() {
        super.onResume()
        //使顶部和状态栏不重叠
        immersionBar { titleBarMarginTop(close) }
    }

    @Suppress("DEPRECATION")
    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        //转场动画效果(启动新Activity时淡入)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}