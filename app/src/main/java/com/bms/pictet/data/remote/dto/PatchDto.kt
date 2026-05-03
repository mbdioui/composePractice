package com.bms.pictet.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PatchDto (
    @SerializedName("small")
    val small: String?,

    @SerializedName("large")
    val large: String?
)
