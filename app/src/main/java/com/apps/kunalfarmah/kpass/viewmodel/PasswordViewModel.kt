package com.apps.kunalfarmah.kpass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.apps.kunalfarmah.kpass.constant.Constants
import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.model.DataModel
import com.apps.kunalfarmah.kpass.model.DialogModel
import com.apps.kunalfarmah.kpass.repository.PasswordRepository
import com.apps.kunalfarmah.kpass.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PasswordViewModel(private val passwordRepository: PasswordRepository): ViewModel() {

    private val _passwords = MutableStateFlow<DataModel<List<PasswordMap>>>(DataModel.Loading())
    val passwords = _passwords.asStateFlow()

    private val _oldPasswords = MutableStateFlow<DataModel<List<PasswordMap>>>(DataModel.Loading())
    val oldPasswords = _oldPasswords.asStateFlow()

    private val _dialogController = MutableSharedFlow<DialogModel<Boolean>>()
    val dialogController = _dialogController.asSharedFlow()

    private val _currentItem = MutableStateFlow<PasswordMap?>(null)
    val currentItem = _currentItem.asStateFlow()

    private val _enqueuedWork = MutableStateFlow<WorkInfo?>(null)
    val enqueuedWork = _enqueuedWork.asStateFlow()

    /**
     * Gets LiveData of all WorkInfo objects for all enqueued work.
     */
    fun getAllEnqueuedWork(workManager: WorkManager){
        val workQuery = WorkQuery.Builder
            .fromStates(
                listOf(
                    WorkInfo.State.ENQUEUED
                )
            )
            .build()
        viewModelScope.launch {
            workManager.getWorkInfosFlow(workQuery).collectLatest {
                _enqueuedWork.value = (it.firstOrNull())
            }
        }
    }



    fun openAddOrEditPasswordDialog(currentItem: PasswordMap ?= null, isEditing: Boolean = false){
        viewModelScope.launch {
            _currentItem.value = currentItem
            if(isEditing){
                _dialogController.emit(DialogModel.EditDialog(true))
            }
            else{
                _dialogController.emit(DialogModel.AddDialog(true))
            }
        }
    }

    fun closeAddOrEditPasswordDialog(isEditing: Boolean = false){
        viewModelScope.launch {
            if(isEditing){
                _dialogController.emit(DialogModel.EditDialog(false))
            }
            else {
                _dialogController.emit(DialogModel.AddDialog(false))
            }
        }
        _currentItem.value = null
    }

    fun openConfirmationDialog(data: PasswordMap){
        _currentItem.value = data
        viewModelScope.launch {
            _dialogController.emit(DialogModel.ConfirmationDialog(true))
        }
    }

    fun closeConfirmationDialog(){
        viewModelScope.launch {
            _dialogController.emit(DialogModel.ConfirmationDialog(false))
        }
        _currentItem.value = null
    }

    fun openPasswordDetailDialog(data: PasswordMap){
        _currentItem.value = data
        viewModelScope.launch {
            _dialogController.emit(DialogModel.DetailsDialog(true))
        }
    }

    fun closePasswordDetailDialog(){
        viewModelScope.launch {
            _dialogController.emit(DialogModel.DetailsDialog(false))
        }
        _currentItem.value = null
    }


    fun insertOrUpdatePassword(id: String = "", websiteName: String, websiteUrl: String?, username: String, password: String, isUpdate: Boolean = false, onItemAdded: (Int) -> Unit = {}){
        viewModelScope.launch(Dispatchers.IO) {
            passwordRepository.insertOrUpdatePassword(id, websiteName, websiteUrl, username, password, isUpdate)
            val passwords = passwordRepository.getAllPasswords()
            _passwords.value = DataModel.Success(passwords)
            if(!isUpdate){
                onItemAdded(passwords.indexOfFirst { it.id == id })
            }
            else{
                _oldPasswords.value = DataModel.Success(passwordRepository.getAllOldPasswords())
            }
        }
    }

    fun getAllPasswords(){
        viewModelScope.launch(Dispatchers.IO) {
            _passwords.value = DataModel.Success(passwordRepository.getAllPasswords())
        }
    }

    fun getAllOldPasswords(){
        viewModelScope.launch(Dispatchers.IO) {
            val list = passwordRepository.getAllOldPasswords()
            if(list.isNotEmpty()) {
                _oldPasswords.value = DataModel.Success(passwordRepository.getAllOldPasswords())
            }
            else{
                _oldPasswords.value = DataModel.Error("No old passwords found")
            }
        }
    }

    fun deletePassword(id: String){
        viewModelScope.launch(Dispatchers.IO) {
            passwordRepository.deletePassword(id)
            _passwords.value = DataModel.Success(passwordRepository.getAllPasswords())
        }
    }

    fun deleteAllPasswords(){
        viewModelScope.launch(Dispatchers.IO) {
            passwordRepository.deleteAllPasswords()
            _passwords.value = DataModel.Success(listOf())
        }
    }

    fun search(query: String){
        viewModelScope.launch(Dispatchers.IO) {
            if(query.isEmpty()){
                _passwords.value = DataModel.Success(passwordRepository.getAllPasswords())
            }
            else {
                val list = ((passwords.value) as DataModel.Success).data.filter {
                    it.websiteName.contains(query, ignoreCase = true) ||
                            it.websiteUrl?.contains(query, ignoreCase = true) == true ||
                            it.username.contains(query, ignoreCase = true)
                }
                _passwords.value = DataModel.Success(list)
            }
        }
    }

    fun savePassword(password: String, callBack: () -> Unit = {}){
        viewModelScope.launch(Dispatchers.IO) {
            PreferencesManager.setData(Constants.MASTER_PASSWORD, password)
        }.invokeOnCompletion {
            callBack()
        }
    }

    fun getMasterPassword(callBack: (String) -> Unit = {}){
        viewModelScope.launch(Dispatchers.IO) {
            PreferencesManager.getData(Constants.MASTER_PASSWORD).collect{ password ->
                callBack(password)
            }
        }
    }

}