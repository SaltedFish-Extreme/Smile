package com.example.smile.model


import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 空数据模型(返回数据可为空)
 *
 * @property data 返回数据
 */
@Serializable
data class EmptyModel(
    @SerialName("data")
    @Contextual
    var data: Any? = null
)