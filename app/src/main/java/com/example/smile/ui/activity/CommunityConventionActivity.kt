package com.example.smile.ui.activity

import android.os.Bundle
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.widget.ext.clickNoRepeat
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.TitleBar

class CommunityConventionActivity : AppActivity() {

    private val titleBar: TitleBar by lazy { findViewById(R.id.titleBar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_convention)
        //使标题栏和状态栏不重叠
        immersionBar {
            titleBar(R.id.titleBar)
        }
        //标题栏左侧返回按钮关闭当前页面
        titleBar.leftView.clickNoRepeat { finish() }
    }
}