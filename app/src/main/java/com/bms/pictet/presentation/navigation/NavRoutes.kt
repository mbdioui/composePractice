package com.bms.pictet.presentation.navigation

object NavRoutes {
    const val LAUNCH_LIST = "launch_list"
    const val LAUNCH_DETAIL = "launch_detail"
    const val LAUNCH_ID_ARG = "launchId"

    const val LAUNCH_DETAIL_ROUTE = "$LAUNCH_DETAIL/{$LAUNCH_ID_ARG}"

    fun launchDetailRoute(launchId: String): String = "$LAUNCH_DETAIL/$launchId"
}
