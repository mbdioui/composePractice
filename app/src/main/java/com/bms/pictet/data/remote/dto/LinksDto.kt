package com.bms.pictet.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LinksDto(
    @SerializedName("patch")
    val patch: PatchDto?,

    @SerializedName("webcast")
    val webcast: String?,

    @SerializedName("article")
    val article: String?,

    @SerializedName("wikipedia")
    val wikipedia: String?
)
