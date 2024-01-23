package com.example.smile.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.drake.serialize.intent.openActivity
import com.example.smile.R
import com.example.smile.app.AppActivity
import com.example.smile.app.AppConfig.DarkTheme
import per.goweii.swipeback.SwipeBackAbility
import per.goweii.swipeback.SwipeBackDirection

/** 闪屏页 (不接收广播) */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppActivity(false), SwipeBackAbility.Direction {

    private val icon: ImageView by lazy { findViewById(R.id.icon) }
    private val layoutSplash: FrameLayout by lazy { findViewById(R.id.layout_splash) }
    private val alphaAnimation: AlphaAnimation by lazy { AlphaAnimation(0.2F, 1.0F) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (DarkTheme) {
            icon.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher_night, null))
        } else {
            icon.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher_day, null))
        }
        //动画效果
        alphaAnimation.run {
            //动画持续时间
            duration = 1500
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    //执行操作 跳转主页
                    openActivity<MainActivity>()
                    finish()
                }

                override fun onAnimationStart(p0: Animation?) {}
            })
        }
        //加载内容视图动画效果
        layoutSplash.startAnimation(alphaAnimation)
    }

    @Suppress("DEPRECATION")
    override fun finish() {
        super.finish()
        //转场结束动画效果
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /** 当前页禁用侧滑 */
    override fun swipeBackDirection() = SwipeBackDirection.NONE
}