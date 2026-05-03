package com.bms.pictet.domain.model

data class Rocket(
    val id: String,
    val name: String,
    val type: String,
    val active: Boolean,
    val description: String?,
    val flickrImages: List<String>?
)
