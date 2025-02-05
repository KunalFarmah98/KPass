package com.apps.kunalfarmah.kpass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.kunalfarmah.kpass.db.PasswordMap
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
fun HomeScreen(modifier: Modifier, viewModel: PasswordViewModel) {
    val passwords by viewModel.passwords.collectAsStateWithLifecycle()
    val currentItem by viewModel.currentItem.collectAsStateWithLifecycle()

    var openAddOrEditPasswordDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var openConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var openPasswordDetailsDialog  by rememberSaveable {
        mutableStateOf(false)
    }

    val isDialogOpen by remember {
        derivedStateOf {
            openAddOrEditPasswordDialog || openConfirmationDialog || openPasswordDetailsDialog
        }
    }

    LaunchedEffect(true) {
        viewModel.getAllPasswords()
        viewModel.dialogController.collect { dialogState ->
            when (dialogState) {
                is DialogModel.AddDialog -> {
                    openAddOrEditPasswordDialog =dialogState.data
                }
                is DialogModel.EditDialog ->{
                    openAddOrEditPasswordDialog = dialogState.data
                }
                is DialogModel.ConfirmationDialog -> {
                    openConfirmationDialog = dialogState.data
                }
                is DialogModel.DetailsDialog -> {
                    openPasswordDetailsDialog = dialogState.data
                }
            }
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
                PasswordsList(
                    passwords = (passwords as DataModel.Success).data,
                    onEditClick = { data: PasswordMap ->
                        if (!isDialogOpen)
                            viewModel.openPasswordDialog(data)
                    },
                    onDeleteClick = { data: PasswordMap ->
                        if (!isDialogOpen)
                            viewModel.openConfirmationDialog(data)
                    },
                    onItemClick = { data: PasswordMap ->
                        if (!isDialogOpen)
                            viewModel.openPasswordDetailDialog(data)
                    }
                )
            }
        }
    }

    if (openAddOrEditPasswordDialog) {
        // this will open the dialog with the current data if it exists or prompt to save password
        AddOrEditPasswordDialog(
            currentItem = currentItem,
            onAddNewPassword = { data: PasswordMap ->
                viewModel.insertOrUpdatePassword(
                    data.websiteName,
                    data.websiteUrl,
                    data.username,
                    data.password
                )
                viewModel.closePasswordDialog()
            },
            onClose = {
                viewModel.closePasswordDialog()
            }
        )
    } else if (openConfirmationDialog) {
        ConfirmationDialog(
            title = "Delete Password",
            body = "Are you sure you want to delete this password?\nIt can not be recovered again",
            onPositiveClick = {
                if (currentItem != null) {
                    viewModel.deletePassword(currentItem!!.username)
                    viewModel.closeConfirmationDialog()
                }
            },
            onNegativeClick = {
                viewModel.closeConfirmationDialog()
            }
        )
    } else if (openPasswordDetailsDialog) {
        PasswordDetail(currentItem, onClose = {
            viewModel.closePasswordDetailDialog()
        })
    }
}