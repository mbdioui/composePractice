package com.bms.pictet.domain.model

data class Launch(
    val id: String,
    val flightNumber: Int,
    val name: String,
    val dateUtc: String,
    val upcoming: Boolean,
    val success: Boolean?,
    val details: String?,
    val links: LaunchLinks?,
    val rocketId: String,
    val launchpadId: String
) {

    val isSuccessDisplay: String
        get() = when (success) {
            true -> "✅ Success"
            false -> "❌ Failed"
            null -> "⏳ Upcoming"
        }
}

data class LaunchLinks(
    val patchSmall: String?,
    val patchLarge: String?,
    val webcast: String?,
    val article: String?,
    val wikipedia: String?
)
