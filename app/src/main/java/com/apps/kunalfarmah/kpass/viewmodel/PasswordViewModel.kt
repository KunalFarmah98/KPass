package com.apps.kunalfarmah.kpass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.model.DataModel
import com.apps.kunalfarmah.kpass.repository.PasswordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasswordViewModel(private val passwordRepository: PasswordRepository): ViewModel() {

    private val _passwords = MutableStateFlow<DataModel<List<PasswordMap>>>(DataModel.Loading())
    val passwords = _passwords.asStateFlow()
    private val _openPasswordDialog = MutableStateFlow(false)
    val openPasswordDialog = _openPasswordDialog.asStateFlow()
    private val _openConfirmationDialog = MutableStateFlow(false)
    val openConfirmationDialog = _openConfirmationDialog.asStateFlow()
    private val _currentItem = MutableStateFlow<PasswordMap?>(null)
    val currentItem = _currentItem.asStateFlow()



    fun openPasswordDialog(currentItem: PasswordMap ?= null){
        _openPasswordDialog.value = true
        if(currentItem != null)
            _currentItem.value = currentItem
    }

    fun closePasswordDialog(){
        _openPasswordDialog.value = false
        _currentItem.value = null
    }

    fun openConfirmationDialog(data: PasswordMap){
        _openConfirmationDialog.value = true
        _currentItem.value = data
    }

    fun closeConfirmationDialog(){
        _openConfirmationDialog.value = false
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