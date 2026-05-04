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

    val formattedDate: String
        get() = try {
            val (year, month, day) = dateUtc.split("T")[0].split("-")
            "$day ${MONTHS[month.toInt() - 1]} $year"
        } catch (e: Exception) {
            dateUtc
        }

    val isSuccessDisplay: String
        get() = when (success) {
            true -> "Success"
            false -> "Failed"
            null -> "Upcoming"
        }

    companion object {
        private val MONTHS = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
    }
}

data class LaunchLinks(
    val patchSmall: String?,
    val patchLarge: String?,
    val webcast: String?,
    val article: String?,
    val wikipedia: String?
)
