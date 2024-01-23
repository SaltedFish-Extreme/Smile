package com.example.smile.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.serialize.intent.bundle
import com.drake.softinput.hideSoftInput
import com.drake.softinput.setWindowSoftInput
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.http.NetApi.ReportUserOrJokeAPI
import com.example.smile.model.EmptyModel
import com.example.smile.ui.adapter.ReportAdapter
import com.example.smile.util.InputTextManager
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.view.SubmitButton
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.TitleBar
import com.hjq.shape.view.ShapeEditText
import com.hjq.toast.Toaster
import kotlinx.coroutines.delay

/** 举报页 */
class ReportActivity : AppActivity() {

    private val titleBar: TitleBar by lazy { findViewById(R.id.titleBar) }
    private val rv: RecyclerView by lazy { findViewById(R.id.rv) }
    private val inputReport: ShapeEditText by lazy { findViewById(R.id.input_report) }
    private val btnReport: SubmitButton by lazy { findViewById(R.id.btn_report) }

    /** 举报适配器 */
    private val adapter: ReportAdapter by lazy { ReportAdapter() }

    /** 举报类型列表(用户) */
    private val reportTypeUser by lazy { resources.getStringArray(R.array.report_type_user).toList() }

    /** 举报类型列表(段子) */
    private val reportTypeJoke by lazy { resources.getStringArray(R.array.report_type_joke).toList() }

    /** Serialize界面传递参数: type(0:段子/1:用户) */
    private val type: Int by bundle()

    /** Serialize界面传递参数: id(user/joke) */
    private val id: String by bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        //设置标题
        titleBar.title = getString(R.string.report)
        //标题栏左侧返回按钮关闭当前页面
        titleBar.leftView.clickNoRepeat { finish() }
        //设置适配器数据
        if (type == 0) {
            adapter.submitList(reportTypeJoke)
        } else if (type == 1) {
            adapter.submitList(reportTypeUser)
        }
        //设置适配器
        rv.adapter = adapter
        //联动举报按钮和举报内容输入框
        InputTextManager.with(this).addView(inputReport).setMain(btnReport).build()
        //使软键盘不遮挡输入框(监听举报内容输入框，使举报按钮悬浮在软键盘上面)
        setWindowSoftInput(float = btnReport, editText = inputReport)
        //点击举报按钮
        btnReport.clickNoRepeat {
            if (inputReport.text.isNullOrBlank()) {
                Toaster.show(R.string.input_report_content)
                btnReport.showError(1500)
                return@clickNoRepeat
            }
            //获取选项视图是否已初始化
            if (adapter.isOptionViewInitialised()) {
                scopeNetLife {
                    //延迟一秒，增强用户体验
                    delay(1000)
                    //举报用户/段子内容
                    Post<EmptyModel?>(ReportUserOrJokeAPI) {
                        param("content", inputReport.text.toString())
                        param("report_tips", adapter.optionView.text.toString())
                        param("target_id", id)
                        param("type", type.toString())
                    }.await()
                    //隐藏输入法
                    hideSoftInput()
                    //举报按钮显示成功，延迟一秒，关闭页面
                    Toaster.show(R.string.report_success)
                    btnReport.showSucceed()
                    delay(1000)
                    finish()
                }.catch {
                    //出现问题，吐司提示，举报按钮显示错误
                    Toaster.show(R.string.report_failed)
                    btnReport.showError(1500)
                }
            } else {
                //未初始化选项视图则说明未选择举报类型
                Toaster.show(R.string.select_report_type)
                btnReport.showError(1500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //使标题栏和状态栏不重叠
        immersionBar { titleBar(titleBar) }
    }
}