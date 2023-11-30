package com.example.smile.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.softinput.setWindowSoftInput
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.app.AppConfig
import com.example.smile.http.NetApi.FeedbackAPI
import com.example.smile.model.EmptyModel
import com.example.smile.ui.adapter.UploadPictureAdapter
import com.example.smile.util.InputTextManager
import com.example.smile.util.albumUploadImage
import com.example.smile.widget.ext.clickNoRepeat
import com.example.smile.widget.ext.hideSoftKeyboard
import com.example.smile.widget.recycler.WrapRecyclerView
import com.example.smile.widget.view.SubmitButton
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.TitleBar
import com.hjq.shape.view.ShapeEditText
import com.hjq.toast.Toaster
import com.huantansheng.easyphotos.models.album.entity.Photo
import kotlinx.coroutines.delay

/** 意见反馈页 */
class FeedbackActivity : AppActivity() {

    private val blankPage: LinearLayout by lazy { findViewById(R.id.blank_page) }
    private val titleBar: TitleBar by lazy { findViewById(R.id.titleBar) }
    private val feedbackText: ShapeEditText by lazy { findViewById(R.id.feedback_text) }
    private val feedbackImage: WrapRecyclerView by lazy { findViewById(R.id.feedback_image) }
    private val feedbackContactInfo: ShapeEditText by lazy { findViewById(R.id.feedback_contact_info) }
    private val feedbackBtn: SubmitButton by lazy { findViewById(R.id.feedback_btn) }

    /** 照片对象集合 */
    private val photoList by lazy { arrayListOf<Photo>() }

    /** 上传图片列表适配器 */
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
        feedbackImage.getFooterViews().forEach { it?.clickNoRepeat { albumUploadImage(photoList, adapter, feedbackImage) } }
        //点击提交按钮，上传反馈信息
        feedbackBtn.clickNoRepeat {
            scopeNetLife {
                //延迟一秒，增强用户体验
                delay(1000)
                Post<EmptyModel?>(FeedbackAPI) {
                    param("brand", Build.BRAND)
                    param("contact", feedbackContactInfo.text.toString())
                    param("content", feedbackText.text.toString())
                    param("pics", photoList.joinToString(",") { it.path })
                    param("product", Build.MODEL)
                    param("release_version", Build.VERSION.RELEASE)
                    param("version_code", AppConfig.getVersionCode())
                    param("version_name", AppConfig.getVersionName())
                }.await()
                //提交按钮显示成功，延迟一秒，关闭页面
                Toaster.show(R.string.submit_success)
                feedbackBtn.showSucceed()
                delay(1000)
                finish()
            }.catch {
                //提交出问题，吐司提示，提交按钮显示错误
                Toaster.show(R.string.submit_failed)
                feedbackBtn.showError(1500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //使标题栏和状态栏不重叠
        immersionBar { titleBar(titleBar) }
    }

    @Suppress("DEPRECATION")
    override fun finish() {
        super.finish()
        //转场动画效果(结束当前Activity时淡出)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}