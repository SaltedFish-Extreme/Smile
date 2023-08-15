package com.example.smile.ui.dialog

import ando.dialog.usage.BottomDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.ui.activity.FeedbackActivity
import com.example.smile.ui.activity.ResetPasswordActivity
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.visibleOrGone
import com.hjq.toast.Toaster

/**
 * 自定义底部弹出对话框(遇到问题)
 *
 * @param context 上下文对象
 * @property visible 是否显示 忘记密码
 */
class CustomBottomDialog(context: Context, private val visible: Boolean) :
    BottomDialog(context, R.style.AndoLoadingDialog) {

    private val forgotPassword: TextView by lazy { findViewById(R.id.forgot_password) }
    private val contactCustomerService: TextView by lazy { findViewById(R.id.contact_customer_service) }
    private val wantFeedback: TextView by lazy { findViewById(R.id.want_feedback) }
    private val cancel: TextView by lazy { findViewById(R.id.cancel) }

    override fun initView() {
        //隐藏忘记密码
        forgotPassword.visibleOrGone(visible)
        //跳转重置密码页
        forgotPassword.clickNoRepeat {
            context.openActivity<ResetPasswordActivity>()
            dismiss()
        }
        //联系客服
        contactCustomerService.clickNoRepeat {
            try {
                //QQ跳转到临时会话界面，如果qq号已经是好友了，直接聊天
                val url = context.getString(
                    R.string.customer_service_link,
                    context.getString(R.string.customer_service_number)
                ) //uin是发送过去的qq号码
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                e.printStackTrace()
                Toaster.show(R.string.qq_exist)
            }
            dismiss()
        }
        //跳转反馈页面
        wantFeedback.clickNoRepeat {
            context.openActivity<FeedbackActivity>()
            dismiss()
        }
        //关闭弹窗
        cancel.clickNoRepeat { dismiss() }
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
    }

    override fun initWindow(window: Window) {
        super.initWindow(window)
        window.setBackgroundDrawableResource(R.drawable.rectangle_ando_dialog_bottom)
        window.setWindowAnimations(R.style.AndoBottomDialogAnimation)
    }

    override fun getLayoutId(): Int = R.layout.layout_dialog_bottom
}