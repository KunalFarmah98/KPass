package com.apps.kunalfarmah.kpass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apps.kunalfarmah.kpass.security.CryptoManager

@Composable
fun PasswordDialog(modifier: Modifier, onSuccess: ()->Unit){
    var password by remember { mutableStateOf("") }
    Card(modifier = modifier.padding(20.dp)) {
        Column(modifier = Modifier.align(Alignment.CenterHorizontally), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Please enter your master password to lock and unlock your passwords")
            Spacer(Modifier.height(5.dp))
            Text("Please remember this password. If it is lost, the stored passwords can not be recovered")
            Spacer(Modifier.height(20.dp))
            TextField(password, onValueChange = {password = it})
            Spacer(Modifier.height(20.dp))
            Button({
                CryptoManager.generateSecretKeyFromPassword(password)
                onSuccess()
            }) {
                Text("Continue")
            }
        }
    }
}