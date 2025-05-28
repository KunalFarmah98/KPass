package com.apps.kunalfarmah.kpass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.kunalfarmah.kpass.R
import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.model.ConfirmationDialogContent
import com.apps.kunalfarmah.kpass.model.DataModel
import com.apps.kunalfarmah.kpass.model.DialogModel
import com.apps.kunalfarmah.kpass.security.CryptoManager
import com.apps.kunalfarmah.kpass.viewmodel.PasswordViewModel

@Composable

fun MainScreen(modifier: Modifier) {
    val text = remember { mutableStateOf("") }
    val encryptedText = remember { mutableStateOf("") }
    val decryptedText = remember { mutableStateOf("") }

    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            value = text.value,
            onValueChange = { text.value = it },
            label = { Text("Password") })
        Button(modifier = Modifier.padding(20.dp), onClick = {
            encryptedText.value = CryptoManager.encrypt(text.value)
        }) {
            Text("encrypt")
        }
        SelectionContainer() {
            Column {
                Text(modifier = Modifier.padding(20.dp), text = encryptedText.value)
            }
        }

        Button(modifier = Modifier.padding(20.dp), onClick = {
            decryptedText.value = CryptoManager.decrypt(text.value)
        }) {
            Text("decrypt")
        }
        Text(decryptedText.value)
    }

}

@Composable
fun HomeScreen(modifier: Modifier, viewModel: PasswordViewModel, shouldUpdatePasswords: Boolean ?= false, isUpdatePasswordDialogOpen: Boolean = false, setFabState: (state: Boolean) -> Unit = {}) {
    val passwords by viewModel.passwords.collectAsStateWithLifecycle()
    val oldPasswords by viewModel.oldPasswords.collectAsStateWithLifecycle()
    val currentItem by viewModel.currentItem.collectAsStateWithLifecycle()

    var openAddPasswordDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var openEditPasswordDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var openConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var openPasswordDetailsDialog  by rememberSaveable {
        mutableStateOf(false)
    }
    var updateOldPasswords by rememberSaveable {
        mutableStateOf(shouldUpdatePasswords == true)
    }

    var openUpdatePasswordDialog by rememberSaveable {
        mutableStateOf(isUpdatePasswordDialogOpen)
    }

    var addedItemIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }

    var confirmationDialogContent by rememberSaveable {
        mutableStateOf<ConfirmationDialogContent?>(null)
    }

    val context = LocalContext.current

    val listState = rememberLazyListState()

    val isDialogOpen by remember {
        derivedStateOf {
            openAddPasswordDialog || openEditPasswordDialog || openConfirmationDialog || openPasswordDetailsDialog || openUpdatePasswordDialog
        }
    }

    LaunchedEffect(true) {
        viewModel.getAllPasswords()
        viewModel.dialogController.collect { dialogState ->
            when (dialogState) {
                is DialogModel.AddDialog -> {
                    openAddPasswordDialog = dialogState.show
                }
                is DialogModel.EditDialog ->{
                    openEditPasswordDialog = dialogState.show
                }
                is DialogModel.ConfirmationDialog -> {
                    openConfirmationDialog = dialogState.show
                    confirmationDialogContent = dialogState.content
                }
                is DialogModel.DetailsDialog -> {
                    openPasswordDetailsDialog = dialogState.show
                }
            }
        }
    }

    LaunchedEffect(shouldUpdatePasswords) {
        if(shouldUpdatePasswords == true){
            updateOldPasswords = true
        }
    }

    LaunchedEffect(updateOldPasswords) {
        if(!updateOldPasswords){
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(isUpdatePasswordDialogOpen) {
        openUpdatePasswordDialog = isUpdatePasswordDialogOpen
    }

    LaunchedEffect(isDialogOpen, updateOldPasswords) {
        setFabState(!isDialogOpen && updateOldPasswords != true)
    }

    LaunchedEffect(addedItemIndex){
        if(addedItemIndex != -1) {
            listState.animateScrollToItem(addedItemIndex)
        }
    }



    when (passwords) {
        is DataModel.Error -> {
            Column(
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Error")
            }
        }

        is DataModel.Loading -> {
            Column(
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(Modifier.size(50.dp))
            }
        }

        is DataModel.Success -> {
            Column(
                modifier
                    .fillMaxSize()
                    .blur(if (isDialogOpen) 2.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if(!updateOldPasswords) {
                    SearchPassword(onSearch = { query ->
                        viewModel.search(query)
                    }, enabled = !isDialogOpen)
                }
                else{
                    Button(modifier = Modifier.padding(top = 10.dp), onClick = {
                        viewModel.getAllPasswords()
                        updateOldPasswords = false
                    }) {
                        Text("Confirm Updating Old Passwords")
                    }
                }
                PasswordsList(
                    state = listState,
                    passwords = if(updateOldPasswords) (oldPasswords as DataModel.Success).data else (passwords as DataModel.Success).data,
                    arePasswordsExpired = updateOldPasswords,
                    onCopyClick = { data: PasswordMap ->
                        context.copyToClipboard(label = "password", CryptoManager.decrypt(data.password))
                    },
                    onEditClick = { data: PasswordMap ->
                        if (!isDialogOpen && !openUpdatePasswordDialog)
                            viewModel.openAddOrEditPasswordDialog(data, true)
                    },
                    onDeleteClick = { data: PasswordMap ->
                        if (!isDialogOpen && !openUpdatePasswordDialog)
                            viewModel.openConfirmationDialog(data)
                    },
                    onItemClick = { data: PasswordMap ->
                        if (!isDialogOpen && !openUpdatePasswordDialog)
                            viewModel.openPasswordDetailDialog(data)
                    },
                    onIgnoreClick = { data: PasswordMap ->
                        viewModel.openConfirmationDialog(
                            data,
                            ConfirmationDialogContent(
                                context.getString(R.string.stop_receiving_alerts_for_this_password),
                                context.getString(R.string.ignore_body),
                                onPositiveClick = {
                                    viewModel.ignorePassword(data)
                                    viewModel.closeConfirmationDialog()
                                },
                                onNegativeClick = {
                                    viewModel.closeConfirmationDialog()
                                }
                            )
                        )
                    }
                )
            }
        }
    }

    if (openAddPasswordDialog) {
        // close all open dialogs before opening add password
        viewModel.closeConfirmationDialog()
        viewModel.closePasswordDetailDialog()
        viewModel.closePasswordDetailDialog()
        viewModel.closeAddOrEditPasswordDialog(true)
        AddOrEditPasswordDialog(
            currentItem = null,
            onAddNewPassword = { data: PasswordMap ->
                viewModel.insertOrUpdatePassword(
                    id = data.id,
                    websiteUrl = data.websiteUrl,
                    websiteName = data.websiteName,
                    username = data.username,
                    password = data.password,
                    isIgnored = data.isIgnored,
                    isUpdate = false
                ){
                    addedItemIndex = it
                }
                viewModel.closeAddOrEditPasswordDialog()
            },
            onClose = {
                viewModel.closeAddOrEditPasswordDialog()
            },
            editing = false
        )
    }
    else if(openEditPasswordDialog){
        AddOrEditPasswordDialog(
            currentItem = currentItem,
            onAddNewPassword = { data: PasswordMap ->
                viewModel.insertOrUpdatePassword(
                    id = data.id,
                    websiteUrl = data.websiteUrl,
                    websiteName = data.websiteName,
                    username = data.username,
                    password = data.password,
                    isIgnored = data.isIgnored,
                    isUpdate = true
                )
                viewModel.closeAddOrEditPasswordDialog(true)
            },
            onClose = {
                viewModel.closeAddOrEditPasswordDialog(true)
            },
            editing = true
        )
    }
    else if (openConfirmationDialog) {
        ConfirmationDialog(
            title = confirmationDialogContent?.title ?: "Delete Password",
            body = confirmationDialogContent?.body ?: "Are you sure you want to delete this password?\nIt can not be recovered again",
            onPositiveClick = confirmationDialogContent?.onPositiveClick ?: {
                if (currentItem != null) {
                    viewModel.deletePassword(currentItem!!.id)
                    viewModel.closeConfirmationDialog()
                }
            },
            onNegativeClick = confirmationDialogContent?.onNegativeClick ?: {
                viewModel.closeConfirmationDialog()
            }
        )
    } else if (openPasswordDetailsDialog) {
        PasswordDetail(currentItem, onClose = {
            viewModel.closePasswordDetailDialog()
        })
    }
}