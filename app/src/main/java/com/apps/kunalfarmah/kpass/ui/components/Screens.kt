package com.apps.kunalfarmah.kpass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.datastore.preferences.core.stringPreferencesKey
import com.apps.kunalfarmah.kpass.constant.Constants
import com.apps.kunalfarmah.kpass.security.CryptoManager
import com.apps.kunalfarmah.kpass.security.CryptoManager.context
import com.apps.kunalfarmah.kpass.security.dataStore

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