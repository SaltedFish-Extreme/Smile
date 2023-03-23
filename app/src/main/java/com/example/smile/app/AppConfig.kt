package com.example.smile.app

import com.drake.serialize.serialize.serialLazy
import com.example.smile.BuildConfig

/**
 * author : Android 轮子哥 github :
 * https://github.com/getActivity/AndroidProject-Kotlin
 * time : 2019/09/02 desc : App 配置管理类
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
}