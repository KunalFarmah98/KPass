package com.apps.kunalfarmah.kpass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apps.kunalfarmah.kpass.constant.Constants
import com.apps.kunalfarmah.kpass.security.CryptoManager

@Composable

fun MainScreen(modifier: Modifier){
    val text = remember { mutableStateOf("") }
    val encryptedText = remember { mutableStateOf("") }
    val decryptedText = remember { mutableStateOf("") }
    var enterPassword by remember { mutableStateOf(false) }
    LaunchedEffect(true) {
        // if no secret key is stored, prompt for password
        if(CryptoManager.keystore.getKey(Constants.KEY_MASTER,null)==null){
            enterPassword = true
        }
    }
    if(enterPassword){
        PasswordDialog(modifier, onSuccess = {
            enterPassword = false
        })
    }
    else {
        Column(
            modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                value = text.value,
                onValueChange = { text.value = it },
                label = { Text("Password") })
            Button(modifier = Modifier.padding(20.dp), onClick = {
                encryptedText.value = CryptoManager.encryptData(text.value)
            }) {
                Text("encrypt")
            }

            Text(modifier = Modifier.padding(20.dp), text = encryptedText.value)

            Button(modifier = Modifier.padding(20.dp), onClick = {
                decryptedText.value = CryptoManager.decryptData(encryptedText.value)
            }) {
                Text("decrypt")
            }
            Text(decryptedText.value)
        }
    }
}