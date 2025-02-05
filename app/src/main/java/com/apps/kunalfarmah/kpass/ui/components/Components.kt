package com.apps.kunalfarmah.kpass.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.apps.kunalfarmah.kpass.R
import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.security.CryptoManager

fun Context.copyToClipboard(label: String, text: CharSequence) {
    val clipboardManager = ContextCompat.getSystemService(this, ClipboardManager::class.java)
    val clip = ClipData.newPlainText(label, text)
    clipboardManager?.setPrimaryClip(clip)
    Toast.makeText(this, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
}

@Composable
fun PasswordsList(
    passwords: List<PasswordMap>, onItemClick: (data: PasswordMap) -> Unit = {},
    onEditClick: (data: PasswordMap) -> Unit = {}, onDeleteClick: (data: PasswordMap) -> Unit = {}
) {
    if (passwords.isEmpty()){
        NoPasswords()
    }
    else {
        LazyColumn(
            modifier = Modifier.padding(20.dp)
        ) {
            items(items = passwords, key = { it.websiteName + it.username }) {
                PasswordItem(it, onItemClick, onEditClick, onDeleteClick)
            }
        }
    }
}


@Composable
fun PasswordItem(
    password: PasswordMap,
    onItemClick: (data: PasswordMap) -> Unit = {},
    onEditClick: (data: PasswordMap) -> Unit = {},
    onDeleteClick: (data: PasswordMap) -> Unit = {}
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp)
        .clickable {
            onItemClick(password)
        },
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Text(modifier = Modifier.padding(20.dp), fontSize = 18.sp, text = password.websiteName, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier
                .padding(start = 10.dp, end = 10.dp, bottom = 20.dp)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(modifier =  Modifier
                    .weight(1f)
                    .size(20.dp), imageVector = Icons.Filled.Person, contentDescription = "person", tint = Color.Black)
                Spacer(Modifier.width(20.dp))
                Text(modifier = Modifier.weight(5f), text = password.username, maxLines = 2, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(20.dp))
                Image(modifier = Modifier
                    .weight(1f)
                    .size(20.dp)
                    .clickable {
                        onItemClick(password)
                    }, painter = painterResource(R.drawable.baseline_visibility_24), contentDescription = "view", colorFilter = ColorFilter.tint(Color.Black))
                Spacer(Modifier.width(5.dp))
                IconButton(modifier = Modifier
                    .weight(1f)
                    .size(20.dp), onClick = {onEditClick(password)}) {
                    Icon(Icons.Filled.Edit, "edit", tint = Color.Black)
                }
                Spacer(Modifier.width(5.dp))
                IconButton(modifier = Modifier
                    .weight(1f)
                    .size(20.dp), onClick = {onDeleteClick(password)}) {
                    Icon(Icons.Filled.Delete, "delete", tint = Color.Black)
                }
            }
        }
    }
}

@Preview
@Composable
fun ConfirmationDialog(
    title: String = "title",
    body: String = "body",
    onPositiveClick: () -> Unit = {},
    onNegativeClick: () -> Unit = {}
) {
    Column(Modifier
        .fillMaxSize()
        .background(Color.Transparent), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.elevatedCardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = body,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onPositiveClick) {
                        Text(stringResource(R.string.confirm))
                    }
                    Spacer(Modifier.width(25.dp))
                    Button(onClick = onNegativeClick) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@Composable
fun NoPasswords(){
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.you_haven_t_stored_any_passwords_yet))
        Text(stringResource(R.string.start_by_clicking_the_icon))
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.elevatedCardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
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
                    text = "Save a Password",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
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
                                    websiteName = websiteNameState,
                                    websiteUrl = websiteUrlState,
                                    username = usernameState,
                                    password = passwordState
                                )
                            )
                        },
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                    Spacer(Modifier.width(25.dp))
                    Button(
                        onClick = onClose,
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                }

            }
        }
    }
}

@Preview
@Composable
fun PasswordDetail(data: PasswordMap? = PasswordMap(), onClose: () -> Unit = {}){
    val password = if(data?.password.isNullOrEmpty()) "" else CryptoManager.decrypt(data!!.password)
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.elevatedCardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = data?.websiteName ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = data?.websiteUrl ?: "",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(9f),
                        text = "Username: ${data?.username}",
                        color = Color.Black,
                        textAlign = TextAlign.Start,
                    )
                    IconButton(modifier = Modifier
                        .weight(1f)
                        .size(20.dp), onClick = {
                        context.copyToClipboard("username", data?.username?:"")
                    }) {
                        Image(painterResource(R.drawable.baseline_content_copy_24), contentDescription = "copy", colorFilter = ColorFilter.tint(Color.Black))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(9f),
                        text = "Password: $password",
                        color = Color.Black,
                        textAlign = TextAlign.Start
                    )
                    IconButton(
                        modifier = Modifier
                            .weight(1f)
                            .size(20.dp),
                        onClick = {
                        context.copyToClipboard("password", password)
                    }) {
                        Image(painterResource(R.drawable.baseline_content_copy_24), contentDescription = "copy", colorFilter = ColorFilter.tint(Color.Black))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { onClose() }) {
                    Text("Close")
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
            unfocusedTextColor = Color.Gray,
            focusedTextColor = Color.Black,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = Color.Gray,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedPlaceholderColor = Color.Gray,
            unfocusedPlaceholderColor = Color.Gray,
            cursorColor = Color.Black,
            errorTextColor = Color.Red,
            errorLabelColor = Color.Red,
            errorBorderColor = Color.Red
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