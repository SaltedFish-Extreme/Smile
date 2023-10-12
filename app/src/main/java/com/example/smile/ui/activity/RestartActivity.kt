package com.example.smile.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.hjq.toast.Toaster

/**
 * author : Android 轮子哥
 *
 * github : https://github.com/getActivity/AndroidProject-Kotlin
 *
 * time : 2020/11/29
 *
 * desc : 重启应用 (不接收广播)
 */
class RestartActivity : AppActivity(false) {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, RestartActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        fun restart(context: Context) {
            val intent = Intent(context, SplashActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restart(this)
        finish()
        Toaster.show(R.string.common_crash_hint)
    }
}