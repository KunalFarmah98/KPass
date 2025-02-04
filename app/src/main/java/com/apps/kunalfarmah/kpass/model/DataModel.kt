package com.apps.kunalfarmah.kpass.model

sealed class DataModel<T> {
    data class Success<T>(val data: T) : DataModel<T>()
    data class Error<T>(val message: String) : DataModel<T>()
    class Loading<T> : DataModel<T>()
}