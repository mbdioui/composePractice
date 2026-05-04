package com.bms.pictet.domain.usecase

import com.bms.pictet.domain.model.Launch
import com.bms.pictet.domain.model.Result
import com.bms.pictet.domain.model.Rocket
import com.bms.pictet.domain.repository.LaunchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLaunchesUseCase @Inject constructor(
    private val repository: LaunchRepository
) {
    operator fun invoke(): Flow<Result<List<Launch>>> =
        repository.getLaunches()

}

class RefreshLaunchUseCase @Inject constructor(
    private val repository: LaunchRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshLaunches()
    }
}

// domain/usecase/GetLaunchDetailsUseCase.kt
class GetLaunchDetailsUseCase @Inject constructor(
    private val repository: LaunchRepository
) {
    suspend operator fun invoke(id: String): Result<Launch> =
        repository.getLaunch(id)
}

class GetRocketDetailsUseCase @Inject constructor(
    private val repository: LaunchRepository
) {
    suspend operator fun invoke(id: String): Result<Rocket> =
        repository.getRocket(id)
}

// domain/usecase/FilterLaunchesUseCase.kt
class FilterLaunchesUseCase @Inject constructor() {
    operator fun invoke(
        launches: List<Launch>,
        query: String,
        status: LaunchFilterStatus
    ): List<Launch> {
        return launches.filter { launch ->
            val matchesQuery = query.isBlank() ||
                    launch.name.contains(query, ignoreCase = true)

            val matchesStatus = when (status) {
                LaunchFilterStatus.ALL -> true
                LaunchFilterStatus.SUCCESS -> launch.success == true
                LaunchFilterStatus.FAILED -> launch.success == false
                LaunchFilterStatus.UPCOMING -> launch.upcoming
            }

            matchesQuery && matchesStatus
        }
    }
}

enum class LaunchFilterStatus(val displayName: String) {
    ALL("All"),
    SUCCESS("Success"),
    FAILED("Failed"),
    UPCOMING("Upcoming")
}