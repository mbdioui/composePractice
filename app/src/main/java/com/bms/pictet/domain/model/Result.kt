package com.bms.pictet.domain.model

sealed class Result<out T> {
    data class Loading<T>(val data: T? = null) : Result<T>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val message: String, val data: T? = null) : Result<T>()
}

fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success
fun <T> Result<T>.isError(): Boolean = this is Result.Error

fun <T> Result<T>.data(): T? = when (this) {
    is Result.Loading -> data
    is Result.Success -> data
    is Result.Error -> data
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (String) -> Unit): Result<T> {
    if (this is Result.Error) action(message)
    return this
}