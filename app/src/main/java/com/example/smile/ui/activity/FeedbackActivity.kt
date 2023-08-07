package com.example.smile.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.softinput.setWindowSoftInput
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.app.AppConfig.uk
import com.example.smile.http.NetApi.QiNiuYunTokenAPI
import com.example.smile.model.QiNiuYunTokenModel
import com.example.smile.ui.adapter.UploadPictureAdapter
import com.example.smile.util.InputTextManager
import com.example.smile.util.KLog
import com.example.smile.widget.ext.albumUploadImage
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.hideSoftKeyboard
import com.example.smile.widget.recycler.WrapRecyclerView
import com.example.smile.widget.view.SubmitButton
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.TitleBar
import com.hjq.shape.view.ShapeEditText
import com.hjq.toast.Toaster
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.qiniu.android.storage.UploadManager

/** 意见反馈页 */
class FeedbackActivity : AppActivity() {

    private val blankPage: LinearLayout by lazy { findViewById(R.id.blank_page) }
    private val titleBar: TitleBar by lazy { findViewById(R.id.titleBar) }
    private val feedbackText: ShapeEditText by lazy { findViewById(R.id.feedback_text) }
    private val feedbackImage: WrapRecyclerView by lazy { findViewById(R.id.feedback_image) }
    private val feedbackContactInfo: ShapeEditText by lazy { findViewById(R.id.feedback_contact_info) }
    private val feedbackBtn: SubmitButton by lazy { findViewById(R.id.feedback_btn) }

    private val photoList by lazy { arrayListOf<Photo>() }

    private val adapter by lazy { UploadPictureAdapter(this, photoList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        //设置标题
        titleBar.title = getString(R.string.feedback)
        //标题栏左侧返回按钮关闭当前页面
        titleBar.leftView.clickNoRepeat { finish() }
        //点击空白处隐藏输入法并清除输入框焦点
        blankPage.clickNoRepeat {
            hideSoftKeyboard(this)
            feedbackText.clearFocus()
            feedbackContactInfo.clearFocus()
        }
        //联动提交按钮和反馈内容/联系方式
        InputTextManager.with(this).addView(feedbackText).addView(feedbackContactInfo).setMain(feedbackBtn).build()
        //使软键盘不遮挡输入框(监听反馈联系方式输入框，使提交按钮悬浮在软键盘上面)
        setWindowSoftInput(float = feedbackBtn, editText = feedbackContactInfo)
        //上传图片列表适配器(横向线性布局)
        feedbackImage.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        feedbackImage.adapter = adapter
        //添加脚布局
        feedbackImage.addFooterView<View>(R.layout.item_add_image)
        //脚布局点击监听，跳转相册图片选择器，上传图片
        feedbackImage.getFooterViews().forEach {
            it?.setOnClickListener {
                albumUploadImage(photoList, adapter, feedbackImage)
            }
        }
        //点击提交按钮，先获取七牛云token，再上传反馈信息
        feedbackBtn.clickNoRepeat {
            scopeNetLife {
                photoList.forEach {
                    val key = uk + "_" + it.name
                    //获取七牛云上传图片所需token
                    val token = Post<QiNiuYunTokenModel>(QiNiuYunTokenAPI) {
                        param("filename", key)
                        param("type", 0)
                    }.await()
                    Toaster.show(key)
                    UploadManager().put(it.path, key, token.token, { key, info, response ->
                        //response 包含 hash、key 等信息，具体字段取决于上传策略的设置
                        if (info.isOK) {
                            KLog.d(getString(R.string.upload_success))
                        } else {
                            //如果失败，这里可以把 info 信息上报自己的服务器，便于后面分析上传错误原因
                            KLog.d(getString(R.string.upload_failed))
                        }
                        KLog.d("$key,\r\n $info,\r\n $response")
                    }, null)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //使标题栏和状态栏不重叠
        immersionBar {
            titleBar(titleBar)
        }
    }

    override fun finish() {
        super.finish()
        //转场动画效果(结束当前Activity时淡出)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}