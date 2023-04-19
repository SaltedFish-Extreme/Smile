package com.example.smile.app

import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.drake.serialize.serialize.serialLazy
import com.example.smile.BuildConfig
import com.example.smile.R
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * author : Android 轮子哥
 *
 * github : https://github.com/getActivity/AndroidProject-Kotlin
 *
 * time : 2019/09/02
 *
 * desc : App 配置管理类
 */
object AppConfig {

    /** 是否夜间主题 */
    var DarkTheme: Boolean by serialLazy(false) //懒加载

    /** 当前是否为调试模式 */
    fun isDebug(): Boolean {
        return BuildConfig.DEBUG
    }

    /** 获取当前应用的包名 */
    fun getPackageName(): String {
        return BuildConfig.APPLICATION_ID
    }

    /** 获取当前应用的版本名 */
    fun getVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    /** 获取当前应用的版本码 */
    fun getVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }

    /** Lottie动画枚举类 */
    enum class LottieAnimation(val value: Int) {
        HOME(R.raw.home), COMPASS(R.raw.compass), ADD(R.raw.release), MESSAGE(R.raw.info), PROFILE(R.raw.profile)
    }

    /** 导航栏动画列表 */
    private val NavigationAnimationList = arrayListOf(
        LottieAnimation.HOME, LottieAnimation.COMPASS, LottieAnimation.ADD, LottieAnimation.MESSAGE, LottieAnimation.PROFILE
    )

    /** 获取 Lottie Drawable */
    fun getLottieDrawable(animation: LottieAnimation, bottomNavigationView: BottomNavigationView): LottieDrawable {
        return LottieDrawable().apply {
            val result = LottieCompositionFactory.fromRawResSync(
                bottomNavigationView.context.applicationContext, animation.value
            )
            callback = bottomNavigationView
            composition = result.value
        }
    }

    /** 获取 Lottie json 文件 */
    fun getLottieAnimationList() = NavigationAnimationList
}