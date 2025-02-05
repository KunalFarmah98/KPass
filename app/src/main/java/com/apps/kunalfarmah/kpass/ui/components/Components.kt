package com.apps.kunalfarmah.kpass.ui.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apps.kunalfarmah.kpass.R
import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.security.CryptoManager

@Composable
fun PasswordsList(passwords: List<PasswordMap>, onItemClick: (data: PasswordMap) -> Unit = {}) {
    if(passwords.isEmpty()){
        NoPasswords()
    }
    else {
        LazyColumn(
            modifier = Modifier.padding(20.dp),
            contentPadding = PaddingValues(10.dp)
        ) {
            items(items = passwords, key = { it.websiteName + it.username }) {
                PasswordItem(it, onItemClick)
            }
        }
    }
}


@Composable
fun PasswordItem(password: PasswordMap, onItemClick: (data: PasswordMap) -> Unit = {}) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 5.dp)
        .clickable {
            onItemClick(password)
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Text(modifier = Modifier.padding(10.dp), text = password.websiteName)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Person, contentDescription = "person")
                Spacer(Modifier.width(20.dp))
                Text(password.username)
                Spacer(Modifier.width(20.dp))
                Image(painterResource(R.drawable.baseline_visibility_off_24), contentDescription = "view")
            }
        }
    }
}

@Composable
fun NoPasswords(){
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("You haven't stored any passwords yet")
        Text("Start by clicking the + icon")
    }
}

@Preview
@Composable
fun AddPassword(onAddNewPassword: () -> Unit = {}){
    FloatingActionButton(
        shape = CircleShape,
        modifier = Modifier.size(50.dp),
        onClick = onAddNewPassword) {
        Icon(Icons.Filled.Add, contentDescription = "Add")
    }
}

@Preview
@Composable
fun AddOrEditPasswordDialog(currentItem: PasswordMap? = null, onAddNewPassword: (passwordMap: PasswordMap) -> Unit = {}, onClose: () -> Unit = {}, isEditable: Boolean = false){
    var websiteNameState by rememberSaveable {
        mutableStateOf(currentItem?.websiteName ?: "")
    }

    var websiteUrlState by rememberSaveable {
        mutableStateOf(currentItem?.websiteUrl ?: "")
    }

    var usernameState by rememberSaveable {
        mutableStateOf(currentItem?.username ?: "")
    }

    var passwordState by rememberSaveable {
        mutableStateOf(if(!currentItem?.password.isNullOrEmpty()) CryptoManager.decrypt(currentItem!!.password) else "")
    }

    var readOnly by rememberSaveable {
        mutableStateOf(isEditable)
    }

    val trailingIcon = if(isEditable)Icons.Outlined.Edit else null
    val onTrailingIconClick = {
        if(readOnly){
            readOnly = false
        }
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .background(Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.elevatedCardElevation(2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Add New Password",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextField(
                    label = "WebsiteName",
                    placeholder = "Enter website or app's name here",
                    onValueChange = { text ->
                        websiteNameState = text
                    },
                    keyboardType = KeyboardType.Text,
                    value = websiteNameState,
                    readOnly = readOnly,
                    trailingIcon = trailingIcon,
                    onTrailingIconClick = onTrailingIconClick
                )
                TextField(
                    label = "WebsiteUrl",
                    placeholder = "Enter website's url here",
                    onValueChange = { text ->
                        websiteUrlState = text
                    },
                    keyboardType = KeyboardType.Text,
                    value = websiteUrlState,
                    readOnly = readOnly,
                    trailingIcon = trailingIcon,
                    onTrailingIconClick = onTrailingIconClick
                )
                TextField(
                    label = "Username",
                    placeholder = "Enter your username here",
                    onValueChange = { text ->
                        usernameState = text
                    },
                    keyboardType = KeyboardType.Email,
                    value = usernameState,
                    trailingIcon = trailingIcon,
                    onTrailingIconClick = onTrailingIconClick
                )
                TextField(
                    label = "Password",
                    placeholder = "Enter your password here",
                    onValueChange = { text ->
                        passwordState = text
                    },
                    keyboardType = KeyboardType.Password,
                    value = passwordState,
                    trailingIcon = trailingIcon,
                    onTrailingIconClick = onTrailingIconClick
                )
                Spacer(Modifier.height(20.dp))
                Row {
                    Button(
                        onClick = {
                            onAddNewPassword(
                                PasswordMap(
                                    websiteNameState, usernameState,
                                    websiteUrlState, passwordState
                                )
                            )
                        },
                        modifier = Modifier.width(100.dp),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Text("Save")
                    }
                    Spacer(Modifier.width(25.dp))
                    Button(
                        onClick = onClose,
                        modifier = Modifier.width(100.dp),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Text("Cancel")
                    }

                }

            }
        }
    }
}

@Preview
@Composable
fun TextField(label: String = "", placeholder: String = "", value: String = "", onValueChange: (String) -> Unit = {}, keyboardType: KeyboardType = KeyboardType.Text
,readOnly: Boolean = false, trailingIcon: ImageVector? = null, onTrailingIconClick: () -> Unit = {}
){
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        label = {Text(label)},
        placeholder = {Text(placeholder)},
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedLabelColor = Color.White,
            unfocusedBorderColor = Color.White,
            focusedPlaceholderColor = Color.LightGray,
            unfocusedPlaceholderColor = Color.LightGray,
            cursorColor = Color.White,
            errorTextColor = Color.Red,
            errorLabelColor = Color.Red,
            errorBorderColor = Color.Red,
            unfocusedContainerColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
        ),
        trailingIcon = {
            if (trailingIcon != null) {
                IconButton(onClick = onTrailingIconClick)  { Icon(trailingIcon, "trailingIcon")}
            }
        },
        readOnly = readOnly
    )
}



@Preview
@Composable
fun PasswordItemPreview() {
    PasswordItem(PasswordMap("test", "test", "test", "test"))
}


@Preview(showBackground = true)
@Composable
fun PasswordsListPreview() {
    PasswordsList(listOf(PasswordMap("test1", "test1", "test", "test"),
        PasswordMap("test2", "test2", "test", "test"),
        PasswordMap("test3", "test3", "test", "test")
    ))
}