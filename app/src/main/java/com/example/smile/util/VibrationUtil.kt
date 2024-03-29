package com.example.smile.util

import android.content.Context
import android.os.*
import com.example.smile.app.AppConfig.VibrationOrNo

/**
 * Created by 咸鱼至尊 on 2022/1/21
 *
 * desc: 上下文对象扩展函数 手机震动工具类
 */
@Suppress("DEPRECATION")
fun Context.vibration() {
    if (VibrationOrNo) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val vibrator = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator.vibrate(CombinedVibration.createParallel(VibrationEffect.createOneShot(50, -1)))
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(50, -1))
            }

            else -> {
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(50)
            }
        }
    }
}