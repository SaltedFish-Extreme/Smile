package com.example.smile.app

import android.annotation.SuppressLint
import android.content.Intent
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
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
        //转场动画效果
        overridePendingTransition(R.anim.bottom_in, R.anim.bottom_silent)
    }

    override fun finish() {
        super.finish()
        //转场动画效果
        overridePendingTransition(R.anim.bottom_silent, R.anim.bottom_out)
    }
}