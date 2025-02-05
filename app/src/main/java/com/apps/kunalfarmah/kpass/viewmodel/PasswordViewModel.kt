package com.apps.kunalfarmah.kpass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.model.DataModel
import com.apps.kunalfarmah.kpass.model.DialogModel
import com.apps.kunalfarmah.kpass.repository.PasswordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasswordViewModel(private val passwordRepository: PasswordRepository): ViewModel() {

    private val _passwords = MutableStateFlow<DataModel<List<PasswordMap>>>(DataModel.Loading())
    val passwords = _passwords.asStateFlow()

    private val _dialogController = MutableSharedFlow<DialogModel<Boolean>>()
    val dialogController = _dialogController.asSharedFlow()

    private val _currentItem = MutableStateFlow<PasswordMap?>(null)
    val currentItem = _currentItem.asStateFlow()



    fun openAddOrEditPasswordDialog(currentItem: PasswordMap ?= null){
        viewModelScope.launch {
            _dialogController.emit(DialogModel.AddDialog(true))
            _dialogController.emit(DialogModel.DetailsDialog(false))
            _dialogController.emit(DialogModel.ConfirmationDialog(false))
        }
        if(currentItem != null)
            _currentItem.value = currentItem
    }

    fun closeAddOrEditPasswordDialog(){
        viewModelScope.launch {
            _dialogController.emit(DialogModel.AddDialog(false))
        }
        _currentItem.value = null
    }

    fun openConfirmationDialog(data: PasswordMap){
        viewModelScope.launch {
            _dialogController.emit(DialogModel.ConfirmationDialog(true))
        }
        _currentItem.value = data
    }

    fun closeConfirmationDialog(){
        viewModelScope.launch {
            _dialogController.emit(DialogModel.ConfirmationDialog(false))
        }
        _currentItem.value = null
    }

    fun openPasswordDetailDialog(data: PasswordMap){
        viewModelScope.launch {
            _dialogController.emit(DialogModel.DetailsDialog(true))
        }
        _currentItem.value = data
    }

    fun closePasswordDetailDialog(){
        viewModelScope.launch {
            _dialogController.emit(DialogModel.DetailsDialog(false))
        }
        _currentItem.value = null
    }


    fun insertOrUpdatePassword(websiteName: String, websiteUrl: String?, username: String, password: String){
        viewModelScope.launch(Dispatchers.IO) {
            passwordRepository.insertOrUpdatePassword(websiteName, websiteUrl, username, password)
            _passwords.value = DataModel.Success(passwordRepository.getAllPasswords())
        }
    }

    fun getAllPasswords(){
        viewModelScope.launch(Dispatchers.IO) {
            _passwords.value = DataModel.Success(passwordRepository.getAllPasswords())
        }
    }

    fun deletePassword(username: String){
        viewModelScope.launch(Dispatchers.IO) {
            passwordRepository.deletePassword(username)
            _passwords.value = DataModel.Success(passwordRepository.getAllPasswords())
        }
    }

}