package com.example.smile.ui.activity

import android.os.Bundle
import com.drake.serialize.intent.bundle
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.model.NotificationVideoModel

/** 首页推送视频页 */
class NotificationVideoActivity : AppActivity() {

    /** Serialize界面传递参数: model */
    private val model: NotificationVideoModel by bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_video)
    }
}