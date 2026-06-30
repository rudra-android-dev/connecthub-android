package com.example.connecthub.data.model

import com.google.gson.annotations.SerializedName

data class ImgBbResponse(
    val data: ImgBbData,
    val success: Boolean,
    val status: Int
)

data class ImgBbData(
    @SerializedName("url") val url: String
)
