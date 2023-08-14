package com.example.smile.ui.activity

import android.os.Bundle
import com.drake.serialize.intent.bundle
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.widget.ext.clickNoRepeat
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.TitleBar

/** 公告页 */
class AnnouncementActivity : AppActivity() {

    private val titleBar: TitleBar by lazy { findViewById(R.id.titleBar) }

    /** Serialize界面传递参数: title */
    private val title: String by bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //根据传递过来的标题使用对应页面
        when (title) {
            getString(R.string.community_convention_title) -> setContentView(R.layout.activity_community_convention)
            getString(R.string.privacy_policy_title) -> setContentView(R.layout.activity_privacy_policy)
            getString(R.string.services_agreement_title) -> setContentView(R.layout.activity_services_agreement)
        }
        //设置标题
        titleBar.title = title
        //标题栏左侧返回按钮关闭当前页面
        titleBar.leftView.clickNoRepeat { finish() }
    }

    override fun onResume() {
        super.onResume()
        //使标题栏和状态栏不重叠
        immersionBar { titleBar(titleBar) }
    }
}