package com.example.smile.ui.activity

import ando.dialog.core.DialogManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.app.AppConfig.UserPersonalInformationModel
import com.example.smile.http.NetApi
import com.example.smile.model.EmptyModel
import com.example.smile.util.InputTextManager
import com.example.smile.util.logout
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.hideSoftKeyboard
import com.example.smile.widget.ext.pressRightClose
import com.example.smile.widget.settingbar.SettingBar
import com.example.smile.widget.view.DrawableTextView
import com.example.smile.widget.view.SubmitButton
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.TitleBar
import com.hjq.shape.view.ShapeEditText
import com.hjq.toast.Toaster
import kotlinx.coroutines.delay

/** 账号安全页 */
class AccountSecurityActivity : AppActivity() {

    private val titleBar: TitleBar by lazy { findViewById(R.id.titleBar) }
    private val userPhone: SettingBar by lazy { findViewById(R.id.user_phone) }
    private val resetPassword: SettingBar by lazy { findViewById(R.id.reset_password) }
    private val modifyPassword: SettingBar by lazy { findViewById(R.id.modify_password) }
    private val logoutAccount: SettingBar by lazy { findViewById(R.id.logout_account) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_security)
        //设置标题
        titleBar.title = getString(R.string.account_security)
        //标题栏左侧返回按钮关闭当前页面
        titleBar.leftView.clickNoRepeat { finish() }
        //显示用户手机号
        userPhone.setRightText(UserPersonalInformationModel.userPhone)
        //跳转重置密码页面
        resetPassword.clickNoRepeat { openActivity<ResetPasswordActivity>("loggedIn" to true) }
        //跳转修改密码页面
        modifyPassword.clickNoRepeat { openActivity<ModifyPasswordActivity>() }
        //打开注销账号弹窗
        logoutAccount.clickNoRepeat { showLogoutAccountDialog() }
    }

    /** 显示注销账号弹窗 */
    private fun showLogoutAccountDialog() {
        DialogManager.with(this, R.style.TransparentBgDialog)
            .useDialog()
            .setContentView(R.layout.layout_dialog_bottom_logout_account) { v: View ->
                val logoutTitle: DrawableTextView = v.findViewById(R.id.logout_title)
                val inputBox: ShapeEditText = v.findViewById(R.id.input_box)
                val btnLogout: SubmitButton = v.findViewById(R.id.btn_logout)
                //按下标题右侧图标关闭弹窗
                logoutTitle.pressRightClose()
                //点击注销按钮
                btnLogout.run {
                    //联动注销按钮和密码输入框
                    InputTextManager.with(this@AccountSecurityActivity).addView(inputBox).setMain(this).build()
                    //注销账户
                    clickNoRepeat {
                        scopeNetLife {
                            //延迟一秒，增强用户体验
                            delay(1000)
                            Post<EmptyModel?>(NetApi.LogoutAccountAPI) { param("psw", inputBox.text.toString()) }.await()
                            hideSoftKeyboard(this@AccountSecurityActivity)
                            //注销成功
                            Toaster.show(R.string.logout_success)
                            showSucceed()
                            //退出登录
                            logout()
                        }.catch {
                            //注销失败，显示错误，吐司错误信息
                            showError(2000)
                            Toaster.show(it.message)
                        }
                    }
                }
            }
            .setCanceledOnTouchOutside(true)
            .show()
    }

    override fun onResume() {
        super.onResume()
        //使标题栏和状态栏不重叠
        immersionBar { titleBar(titleBar) }
    }

    @Suppress("DEPRECATION")
    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        //转场动画效果(启动新Activity时淡入)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}