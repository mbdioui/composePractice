package com.bms.pictet.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LaunchDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("flight_number")
    val flightNumber: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("date_utc")
    val dateUtc: String,

    @SerializedName("upcoming")
    val upcoming: Boolean,

    @SerializedName("success")
    val success: Boolean?,

    @SerializedName("details")
    val details: String?,

    @SerializedName("links")
    val links: LinksDto?,

    @SerializedName("rocket")
    val rocketId: String,

    @SerializedName("launchpad")
    val launchpadId: String
)
