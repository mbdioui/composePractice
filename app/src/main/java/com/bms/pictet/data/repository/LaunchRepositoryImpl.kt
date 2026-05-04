package com.bms.pictet.data.repository

import com.bms.pictet.data.local.dao.LaunchDao
import com.bms.pictet.data.local.entity.LaunchEntity
import com.bms.pictet.data.remote.api.SpaceXApi
import com.bms.pictet.data.remote.dto.LaunchDto
import com.bms.pictet.data.remote.dto.LinksDto
import com.bms.pictet.data.remote.dto.RocketDto
import com.bms.pictet.di.ApplicationScope
import com.bms.pictet.domain.model.Launch
import com.bms.pictet.domain.model.LaunchLinks
import com.bms.pictet.domain.model.Result
import com.bms.pictet.domain.model.Rocket
import com.bms.pictet.domain.repository.LaunchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LaunchRepositoryImpl @Inject constructor(
    private val api: SpaceXApi,
    private val launchDao: LaunchDao,
    @ApplicationScope private val appScope: CoroutineScope
) : LaunchRepository {

    /**
     * Offline-First: Returns cached data immediately, refreshes in background
     *
     * Flow behavior:
     * - First launch (empty cache): Emits Loading → Success (after fetch)
     * - Subsequent opens: Emits Success (cached) → Success (fresh after refresh)
     * - Offline with cache: Emits Success (cached), silent refresh fails
     */
    override fun getLaunches(): Flow<Result<List<Launch>>> =
        launchDao.getAllLaunches()
            .map { entities ->
                if (entities.isEmpty()) {
                    // First launch: Show loading while we fetch
                    Result.Loading()
                } else {
                    // Have cached data: Show immediately, refresh in background
                    Result.Success(entities.map { it.toDomain() })
                }
            }
            .onStart {
                // Fire-and-forget background refresh
                appScope.launch { refreshLaunches() }
            }
            .catch { emit(Result.Error(it.message ?: "Unknown error")) }


    /**
     * Force refresh from remote
     * Called by: Pull-to-refresh, first launch (implicitly), retry button
     */
    override suspend fun refreshLaunches(): Result<Unit> = try {
        val responseFromApi = api.getAllLaunches()
        if (responseFromApi.isSuccessful) {
            responseFromApi.body()?.let { launches ->
                // Save to Room → Triggers Flow re-emission with fresh data
                launchDao.insertLaunches(launches.map { it.toEntity() })
                Result.Success(Unit)
            } ?: Result.Error("Empty response")
        } else {
            Result.Error("HTTP ${responseFromApi.code()}: ${responseFromApi.message()}")
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    override suspend fun getLaunch(id: String): Result<Launch> {
        return try {
            // Try cache first
            launchDao.getLaunchById(id)?.let {
                return Result.Success(it.toDomain())
            }
            // Fall back to API, then cache the result
            val response = api.getLaunch(id)
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    launchDao.insertLaunches(listOf(dto.toEntity()))
                    Result.Success(dto.toDomain())
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getRocket(id: String): Result<Rocket> = try {
        val response = api.getRocket(id)
        if (response.isSuccessful) {
            response.body()?.let {
                Result.Success(it.toDomain())
            } ?: Result.Error("Empty response")
        } else {
            Result.Error("HTTP ${response.code()}: ${response.message()}")
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }


    // Mapping functions
    private fun LaunchDto.toDomain(): Launch = Launch(
        id = id,
        flightNumber = flightNumber,
        name = name,
        dateUtc = dateUtc,
        upcoming = upcoming,
        success = success,
        details = details,
        links = links?.toDomain(),
        rocketId = rocketId,
        launchpadId = launchpadId
    )

    private fun LinksDto.toDomain(): LaunchLinks = LaunchLinks(
        patchSmall = patch?.small,
        patchLarge = patch?.large,
        webcast = webcast,
        article = article,
        wikipedia = wikipedia
    )

    private fun LaunchDto.toEntity(): LaunchEntity = LaunchEntity(
        id = id,
        flightNumber = flightNumber,
        name = name,
        dateUtc = dateUtc,
        upcoming = upcoming,
        success = success,
        details = details,
        patchSmall = links?.patch?.small,
        patchLarge = links?.patch?.large,
        webcast = links?.webcast,
        article = links?.article,
        wikipedia = links?.wikipedia,
        rocketId = rocketId,
        launchpadId = launchpadId
    )

    private fun LaunchEntity.toDomain(): Launch = Launch(
        id = id,
        flightNumber = flightNumber,
        name = name,
        dateUtc = dateUtc,
        upcoming = upcoming,
        success = success,
        details = details,
        links = LaunchLinks(
            patchSmall = patchSmall,
            patchLarge = patchLarge,
            webcast = webcast,
            article = article,
            wikipedia = wikipedia
        ),
        rocketId = rocketId,
        launchpadId = launchpadId
    )

    private fun RocketDto.toDomain(): Rocket = Rocket(
        id = id,
        name = name,
        type = type,
        active = active,
        description = description,
        flickrImages = flickrImages
    )
}
