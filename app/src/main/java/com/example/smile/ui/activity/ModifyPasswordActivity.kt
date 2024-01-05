package com.example.smile.ui.activity

import ando.dialog.core.DialogManager
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.softinput.setWindowSoftInput
import com.drake.spannable.movement.ClickableMovementMethod
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.http.NetApi.ModifyPasswordAPI
import com.example.smile.model.EmptyModel
import com.example.smile.ui.dialog.CustomBottomDialogEncounterProblems
import com.example.smile.util.InputTextManager
import com.example.smile.util.logout
import com.example.smile.util.spannableText
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.hideSoftKeyboard
import com.example.smile.widget.ext.loadAnimation
import com.example.smile.widget.view.ClearEditText
import com.example.smile.widget.view.PasswordEditText
import com.example.smile.widget.view.SubmitButton
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import kotlinx.coroutines.delay

/** 修改密码页 */
class ModifyPasswordActivity : AppActivity() {

    private val blankPage: RelativeLayout by lazy { findViewById(R.id.blank_page) }
    private val close: ImageView by lazy { findViewById(R.id.close) }
    private val inputOldPassword: ClearEditText by lazy { findViewById(R.id.input_old_password) }
    private val inputNewPassword: PasswordEditText by lazy { findViewById(R.id.input_new_password) }
    private val inputNewPasswordAgain: PasswordEditText by lazy { findViewById(R.id.input_new_password_again) }
    private val btnModify: SubmitButton by lazy { findViewById(R.id.btn_modify) }
    private val encounterProblems: TextView by lazy { findViewById(R.id.encounter_problems) }
    private val loginProtocolReminder: TextView by lazy { findViewById(R.id.login_protocol_reminder) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_password)
        //点击空白处隐藏输入法并清除输入框焦点
        blankPage.clickNoRepeat {
            hideSoftKeyboard(this)
            inputOldPassword.clearFocus()
            inputNewPassword.clearFocus()
            inputNewPasswordAgain.clearFocus()
        }
        //关闭按钮关闭当前页面
        close.clickNoRepeat { finish() }
        //使软键盘不遮挡输入框(监听所有输入框，使修改按钮悬浮在软键盘上面)
        setWindowSoftInput(float = btnModify)
        //登录协议Spannable文本
        loginProtocolReminder.movementMethod = ClickableMovementMethod.getInstance() // 保证没有点击背景色
        loginProtocolReminder.text = spannableText()
        btnModify.run {
            //联动修改按钮和旧密码/新密码/新密码重复输入框
            InputTextManager.with(this@ModifyPasswordActivity)
                .addView(inputOldPassword).addView(inputNewPassword).addView(inputNewPasswordAgain)
                .setMain(this).build()
            //点击修改按钮
            this.clickNoRepeat {
                //校验旧密码长度
                if (inputOldPassword.text!!.length !in 6..18) {
                    inputOldPassword.startAnimation(loadAnimation(R.anim.shake_anim))
                    showError(2000)
                    Toaster.show(R.string.password_format_error)
                    return@clickNoRepeat
                }
                //校验新密码长度
                if (inputNewPassword.text!!.length !in 6..18) {
                    inputNewPassword.startAnimation(loadAnimation(R.anim.shake_anim))
                    showError(2000)
                    Toaster.show(R.string.password_format_error)
                    return@clickNoRepeat
                }
                //校验两次新密码是否一致
                if (inputNewPassword.text.toString() != inputNewPasswordAgain.text.toString()) {
                    inputNewPassword.startAnimation(loadAnimation(R.anim.shake_anim))
                    inputNewPasswordAgain.startAnimation(loadAnimation(R.anim.shake_anim))
                    showError(2000)
                    Toaster.show(R.string.re_enter_password)
                    return@clickNoRepeat
                }
                scopeNetLife {
                    //延迟一秒，增强用户体验
                    delay(1000)
                    //修改密码
                    Post<EmptyModel?>(ModifyPasswordAPI) {
                        param("old_psw", inputOldPassword.text.toString())
                        param("password", inputNewPassword.text.toString())
                        param("new_psw", inputNewPasswordAgain.text.toString())
                    }.await()
                    //修改成功
                    Toaster.show(R.string.modify_success)
                    showSucceed()
                    //退出登录
                    logout()
                }.catch {
                    //修改失败，显示错误，吐司错误信息
                    showError(2000)
                    Toaster.show(it.message)
                }
            }
        }

        //点击遇到问题显示底部弹窗
        encounterProblems.clickNoRepeat { showBottomDialog() }
    }

    //底部弹窗(BottomDialog)
    private fun showBottomDialog() {
        val bottomDialog = CustomBottomDialogEncounterProblems(this, false)
        DialogManager.replaceDialog(bottomDialog).setCancelable(true)
            .setCanceledOnTouchOutside(true).setDimmedBehind(true).show()
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

    @Suppress("DEPRECATION")
    override fun finish() {
        super.finish()
        //转场动画效果(结束当前Activity时淡出)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}