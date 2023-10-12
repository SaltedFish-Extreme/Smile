package com.example.smile.app

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smile.R
import com.example.smile.ui.receiver.NetworkConnectChangedReceiver
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import com.hjq.toast.style.BlackToastStyle
import com.hjq.toast.style.WhiteToastStyle

/**
 * Created by 咸鱼至尊 on 2021/12/9
 *
 * desc: Activity基类 目前只用来监听网络状态变化|状态栏沉浸|转场动画效果
 *
 * @property receive 是否接收广播(默认接收)
 */
open class AppActivity(private val receive: Boolean = true) : AppCompatActivity() {

    //延迟初始化网络状态变化监听器
    private val mNetworkChangeListener by lazy { NetworkConnectChangedReceiver() }

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
                //日间主题修改吐司黑色样式
                Toaster.setStyle(BlackToastStyle())
            }

            true -> immersionBar {
                //夜间主题修改吐司白色样式
                Toaster.setStyle(WhiteToastStyle())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //创建意图过滤器对象
        val filter = IntentFilter()
        //添加网络状态变化意图
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        //在程序恢复时动态注册广播接收器，确保只有处于栈顶的activity可以接收广播
        if (receive) registerReceiver(mNetworkChangeListener, filter)
    }

    override fun onPause() {
        //同理，在程序暂停时销毁动态注册的广播接收器(当程序处于后台时不会接收广播)
        if (receive) unregisterReceiver(mNetworkChangeListener)
        super.onPause()
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