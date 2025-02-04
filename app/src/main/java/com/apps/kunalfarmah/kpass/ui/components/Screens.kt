package com.apps.kunalfarmah.kpass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.model.DataModel
import com.apps.kunalfarmah.kpass.security.CryptoManager
import com.apps.kunalfarmah.kpass.viewmodel.PasswordViewModel

@Composable

fun MainScreen(modifier: Modifier){
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
fun HomeScreen(modifier: Modifier, viewModel: PasswordViewModel){
    val passwords by viewModel.passwords.collectAsStateWithLifecycle()
    val openPassDialog by viewModel.openPasswordDialog.collectAsStateWithLifecycle(false)
    val currentItem by viewModel.currentItem.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        viewModel.getAllPasswords()
    }

    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

        when(passwords){
            is DataModel.Error -> {
                Text(text = "Error")
            }
            is DataModel.Loading -> {
                CircularProgressIndicator()
                }
            is DataModel.Success -> {
                if (openPassDialog) {
                    // this will open the dialog with the current data if it exists or prompt to save password
                    AddOrEditPasswordDialog(
                        currentItem = currentItem,
                        onAddNewPassword = {
                            if(currentItem != null) {
                                viewModel.insertOrUpdatePassword(
                                    currentItem!!.websiteName,
                                    currentItem!!.websiteUrl,
                                    currentItem!!.username,
                                    currentItem!!.password
                                )
                            }
                            viewModel.closePasswordDialog()
                        },
                        onClose = {
                            viewModel.closePasswordDialog()
                        }
                    )
                } else {
                    PasswordsList(passwords = (passwords as DataModel.Success).data, onItemClick = {data: PasswordMap ->
                        viewModel.openPasswordDialog(data)
                    })
                }
            }
        }

    }


}