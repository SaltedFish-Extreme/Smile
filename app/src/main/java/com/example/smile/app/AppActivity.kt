package com.example.smile.app

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smile.R
import com.gyf.immersionbar.ktx.immersionBar

/**
 * Created by 咸鱼至尊 on 2021/12/9
 *
 * desc: Activity基类
 */
open class AppActivity : AppCompatActivity() {

    init {
        //应用主题切换
        when (AppApplication.context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                AppConfig.DarkTheme = true
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                AppConfig.DarkTheme = false
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                AppConfig.DarkTheme = false
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //状态栏沉浸
        when (AppConfig.DarkTheme) {
            false -> immersionBar {
                navigationBarColor(R.color.white_smoke)
                statusBarDarkFont(true, 0.2f)
                navigationBarDarkIcon(true, 0.2f)
            }

            true -> immersionBar {}
        }
        //强制页面竖屏显示
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
        //转场动画效果
        overridePendingTransition(R.anim.right_in_activity, R.anim.right_out_activity)
    }

    override fun onDestroy() {
        super.onDestroy()
        //转场动画效果
        overridePendingTransition(R.anim.left_in_activity, R.anim.left_out_activity)
    }
}