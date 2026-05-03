package com.bms.pictet.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RocketDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("description")
    val description: String?,

    @SerializedName("flickr_images")
    val flickrImages: List<String>?
)
