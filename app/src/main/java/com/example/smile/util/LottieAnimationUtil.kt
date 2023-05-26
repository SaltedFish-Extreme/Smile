package com.example.smile.util

import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.example.smile.R
import com.google.android.material.bottomnavigation.BottomNavigationView

/** Lottie动画工具类 */
object LottieAnimationUtil {
    /** Lottie动画枚举类 */
    enum class LottieAnimation(val value: Int) {
        HOME(R.raw.home), COMPASS(R.raw.tools), ADD(R.raw.release), MESSAGE(R.raw.message), PROFILE(R.raw.profile)
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