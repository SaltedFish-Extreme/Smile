package com.example.smile.ui.dialog

import ando.dialog.usage.BottomDialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import com.drake.channel.sendEvent
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.ui.activity.ReportActivity
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.visibleOrGone

/**
 * 自定义底部弹出对话框(举报内容)
 *
 * @param context 上下文对象
 * @property jokeId 举报段子id
 * @property userId 举报用户id
 * @property type 举报类型(0:举报段子；1:举报用户)
 * @property position item位置
 */
class CustomBottomDialogReportContent(
    context: Context,
    private val jokeId: String = "",
    private val userId: String = "",
    private val type: Int,
    private val position: Int? = null
) : BottomDialog(context, R.style.AndoLoadingDialog) {

    private val reportPerson: LinearLayout by lazy { findViewById(R.id.report_person) }
    private val reportUser: TextView by lazy { findViewById(R.id.report_user) }
    private val reportJoke: LinearLayout by lazy { findViewById(R.id.report_joke) }
    private val reportPublisher: TextView by lazy { findViewById(R.id.report_publisher) }
    private val reportContent: TextView by lazy { findViewById(R.id.report_content) }
    private val notInterested: TextView by lazy { findViewById(R.id.not_interested) }
    private val cancel: TextView by lazy { findViewById(R.id.cancel) }

    override fun initView() {
        //根据举报类型显示隐藏选项
        reportJoke.visibleOrGone(type == 0)
        reportPerson.visibleOrGone(type == 1)
        //根据有无传递位置显示隐藏选项
        notInterested.visibleOrGone(position != null)
        //举报用户
        reportUser.clickNoRepeat { context.openActivity<ReportActivity>("type" to 1, "id" to userId) }
        //举报用户
        reportPublisher.clickNoRepeat { context.openActivity<ReportActivity>("type" to 1, "id" to userId) }
        //举报段子
        reportContent.clickNoRepeat { context.openActivity<ReportActivity>("type" to 0, "id" to jokeId) }
        //不感兴趣，发送消息事件，传递段子位置
        if (position != null) {
            notInterested.clickNoRepeat { sendEvent(position, context.getString(R.string.channel_tag_not_interested_joke)) }
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

    override fun getLayoutId(): Int = R.layout.layout_dialog_bottom_report_content

}