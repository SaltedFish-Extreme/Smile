package com.example.smile.http

import com.drake.net.exception.ResponseException
import com.drake.serialize.intent.openActivity
import com.example.smile.app.AppApplication
import com.example.smile.app.AppConfig
import com.example.smile.ui.activity.LoginActivity
import com.hjq.toast.Toaster
import okhttp3.Interceptor
import okhttp3.Response

/** 客户端token过期拦截器 */
class RefreshTokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        return synchronized(RefreshTokenInterceptor::class.java) {
            // token过期抛出异常, 由全局错误处理器处理, 在其中可以跳转到登陆界面提示用户重新登陆
            if (response.code == 202 && AppConfig.token.isNotBlank()) {
                Toaster.show("登录状态已失效，请重新登录")
                AppApplication.context.openActivity<LoginActivity>()
                chain.proceed(request)
                throw ResponseException(response, "登录状态已失效，请重新登录")
            } else {
                response
            }
        }
    }
}
