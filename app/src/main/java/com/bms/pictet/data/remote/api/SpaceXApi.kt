package com.bms.pictet.data.remote.api

import com.bms.pictet.data.remote.dto.LaunchDto
import com.bms.pictet.data.remote.dto.RocketDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path


interface SpaceXApi {
    @GET("launches")
    suspend fun getAllLaunches(): Response<List<LaunchDto>>

    @GET("launches/{id}")
    suspend fun getLaunch(@Path("id") id: String): Response<LaunchDto>

    @GET("rockets/{id}")
    suspend fun getRocket(@Path("id") id: String): Response<RocketDto>
}