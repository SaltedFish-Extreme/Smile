@file:Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")

package com.example.smile.http

import com.drake.net.NetConfig
import com.drake.net.convert.NetConverter
import com.drake.net.exception.ConvertException
import com.drake.net.exception.RequestParamsException
import com.drake.net.exception.ResponseException
import com.drake.net.exception.ServerResponseException
import com.drake.net.request.kType
import com.drake.serialize.intent.openActivity
import com.example.smile.app.AppApplication
import com.example.smile.ui.activity.LoginActivity
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Type
import kotlin.reflect.KType

/**
 * Kotlin JSON解析转换器
 *
 * @property success 成功状态码
 * @property code 状态码标识
 * @property message 错误信息标识
 */
class SerializationConverter(
    val success: String = "0",
    val code: String = "errorCode",
    val message: String = "errorMsg",
) : NetConverter {

    companion object {
        val jsonDecoder = Json {
            ignoreUnknownKeys = true // 数据类可以不用声明Json的所有字段
            coerceInputValues = true // 如果Json字段是Null则使用数据类字段默认值
        }
    }

    override fun <R> onConvert(succeed: Type, response: Response): R? {
        try {
            return NetConverter.onConvert<R>(succeed, response)
        } catch (e: ConvertException) {
            val code = response.code
            when {
                code in 200..299 -> { // 请求成功
                    val bodyString = response.body?.string() ?: return null
                    val kType = response.request.kType ?: throw ConvertException(response, "Request does not contain KType")
                    return try {
                        val json = JSONObject(bodyString) // 获取JSON中后端定义的错误码和错误信息
                        when (val srvCode = json.getString(this.code)) {
                            success -> { // 对比后端自定义错误码
                                json.getString("data").parseBody<R>(kType)
                            }

                            "202" -> {
                                //登录过期，拦截重新登录
                                AppApplication.context.openActivity<LoginActivity>()
                                throw ResponseException(response, "登录状态已失效，请重新登录", tag = srvCode)
                            }

                            else -> { // 错误码匹配失败, 开始写入错误异常
                                val errorMessage = json.optString(message, NetConfig.app.getString(com.drake.net.R.string.no_error_message))
                                throw ResponseException(response, errorMessage, tag = srvCode) // 将业务错误码作为tag传递
                            }
                        }
                    } catch (e: JSONException) { // 固定格式JSON分析失败直接解析JSON
                        bodyString.parseBody<R>(kType)
                    }
                }

                code in 400..499 -> throw RequestParamsException(response, code.toString()) // 请求参数错误
                code >= 500 -> throw ServerResponseException(response, code.toString()) // 服务器异常错误
                else -> throw ConvertException(response)
            }
        }
    }

    fun <R> String.parseBody(succeed: KType): R? {
        return jsonDecoder.decodeFromString(Json.serializersModule.serializer(succeed), this) as R
    }
}