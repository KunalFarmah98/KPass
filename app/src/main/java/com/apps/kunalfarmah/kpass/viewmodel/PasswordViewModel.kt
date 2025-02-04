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

    val _passwords = MutableStateFlow<DataModel<List<PasswordMap>>>(DataModel.Loading())
    val passwords = _passwords.asStateFlow()


    fun insertOrUpdatePassword(websiteName: String, websiteUrl: String?, username: String, password: String){
        viewModelScope.launch(Dispatchers.IO) {
            passwordRepository.insertOrUpdatePassword(websiteName, websiteUrl, username, password)
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
        }
    }

}