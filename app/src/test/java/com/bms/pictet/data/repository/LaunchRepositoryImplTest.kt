package com.bms.pictet.data.repository

import app.cash.turbine.test
import com.bms.pictet.data.local.dao.LaunchDao
import com.bms.pictet.data.local.entity.LaunchEntity
import com.bms.pictet.data.remote.api.SpaceXApi
import com.bms.pictet.data.remote.dto.LaunchDto
import com.bms.pictet.data.remote.dto.LinksDto
import com.bms.pictet.data.remote.dto.PatchDto
import com.bms.pictet.domain.model.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchRepositoryImplTest {

    private val api: SpaceXApi = mockk()
    private val dao: LaunchDao = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private val appScope = CoroutineScope(SupervisorJob() + testDispatcher)

    private lateinit var repository: LaunchRepositoryImpl

    @Before
    fun setup() {
        repository = LaunchRepositoryImpl(
            api = api,
            launchDao = dao,
            appScope = appScope
        )
    }

    @Test
    fun getLaunches_withEmptyCache_emitsLoading() = runTest {
        every { dao.getAllLaunches() } returns flowOf(emptyList())
        coEvery { api.getAllLaunches() } returns Response.success(emptyList())

        repository.getLaunches().test {
            val first = awaitItem()
            assertTrue(first is Result.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshLaunches_fetchesFromApi_andCaches() = runTest {
        val launchDto = LaunchDto(
            id = "launch-1",
            flightNumber = 1,
            name = "FalconSat",
            dateUtc = "2006-03-24T22:30:00.000Z",
            upcoming = false,
            success = false,
            details = "Engine failure",
            links = LinksDto(
                patch = PatchDto(
                    small = "small.png",
                    large = "large.png"
                ),
                webcast = null,
                article = null,
                wikipedia = null
            ),
            rocketId = "falcon-1",
            launchpadId = "kwajalein"
        )
        coEvery { api.getAllLaunches() } returns Response.success(listOf(launchDto))

        val result = repository.refreshLaunches()

        assertTrue(result is Result.Success)
        coVerify(exactly = 1) {
            dao.insertLaunches(
                withArg { entities ->
                    assertTrue(entities.size == 1)
                    val entity: LaunchEntity = entities.first()
                    assertTrue(entity.id == "launch-1")
                    assertTrue(entity.name == "FalconSat")
                }
            )
        }
    }

    @Test
    fun refreshLaunches_whenHttpError_returnsError() = runTest {
        val errorBody = "Not Found".toResponseBody("text/plain".toMediaType())
        coEvery { api.getAllLaunches() } returns Response.error(404, errorBody)

        val result = repository.refreshLaunches()

        assertTrue(result is Result.Error)
    }
}
