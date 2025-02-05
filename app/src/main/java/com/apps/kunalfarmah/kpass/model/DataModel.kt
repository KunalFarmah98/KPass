package com.apps.kunalfarmah.kpass.model

sealed class DataModel<T> {
    data class Success<T>(val data: T) : DataModel<T>()
    data class Error<T>(val message: String) : DataModel<T>()
    class Loading<T> : DataModel<T>()
}

sealed class DialogModel<T> {
    data class AddDialog<T>(val show: T) : DialogModel<T>()
    data class EditDialog<T>(val show: T) : DialogModel<T>()
    data class ConfirmationDialog<T>(val show: T) : DialogModel<T>()
    data class DetailsDialog<T>(val show: T) : DialogModel<T>()
}