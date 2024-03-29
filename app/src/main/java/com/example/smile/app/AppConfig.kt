package com.example.smile.app

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.drake.serialize.serialize.annotation.SerializeConfig
import com.drake.serialize.serialize.serialLazy
import com.example.smile.BuildConfig
import com.example.smile.model.UserInfoModel
import java.util.UUID

/**
 * author : Android 轮子哥
 *
 * github : https://github.com/getActivity/AndroidProject-Kotlin
 *
 * time : 2019/09/02
 *
 * desc : App 配置管理类
 */
@SerializeConfig(mmapID = "app_config")
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

    /** 获取设备序列号 */
    private fun getSerial(context: Context): String {
        return Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    /** 获取UUID */
    fun getUUID(): String {
        return UUID.randomUUID().toString()
    }

    //region 段子乐API接口相关参数
    /** 段子乐开放API接口调用凭证 */
    const val project_token = "AA86D41D05804918A54E1F3202BDC8D4"

    /** 用户登录成功后返回的token */
    var token by serialLazy("")

    /** 设备唯一ID */
    val uk by serialLazy(getSerial(AppApplication.context))

    /** 渠道来源 */
    const val channel = "cretin_open_api"

    /** app信息 */
    val app by serialLazy("${getVersionName()};${getVersionCode()};${Build.VERSION.RELEASE}")

    /** 设备信息 */
    val device by serialLazy("${Build.BRAND};${Build.MODEL}")

    /** AES解析密钥 */
    const val AesParsingKey = "cretinzp**273846"
    //endregion

    /** 搜索热词 全局变量 每次打开app重新初始化赋值 */
    val SearchHot: MutableList<String> by lazy { (mutableListOf()) }

    /** 搜索记录 永久保存磁盘，app删除或者赋值为null时清除 */
    var SearchHistory: List<String> by serialLazy(arrayListOf())

    /** 当前登录用户ID */
    var userId: String by serialLazy("")

    /** 是否开启震动 */
    var VibrationOrNo: Boolean by serialLazy(true) //懒加载

    /** 是否开启数据流量加载图片 */
    var MobileNetLoadingPicturesOrNo: Boolean by serialLazy(true) //懒加载

    /** 当前是否正在使用数据流量 */
    var MobileNetUsing: Boolean = false

    /** 用户个人信息数据模型 只用来在用户个人信息页面展示，不作为永久变量保存磁盘 */
    var UserPersonalInformationModel = UserInfoModel.User()
}