package com.example.smile.model


import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmptyModel(
    @SerialName("data")
    @Contextual
    var data: Any? = null
)