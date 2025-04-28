package com.apps.kunalfarmah.kpass.ui.components

import PasswordGenerator.availablePasswordLengths
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.apps.kunalfarmah.kpass.R
import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.security.CryptoManager
import kotlinx.coroutines.launch

@Preview
@Composable
fun OptionsMenu(titles: List<String> = listOf(), onClickListener: (title: String) -> Unit = {}){
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            titles.forEach { title ->
                DropdownMenuItem(
                    text = { Text(title) },
                    onClick = {
                        expanded = false
                        onClickListener(title)
                    }
                )
            }
        }
    }
}


@Composable
fun AlphabeticalScrollbar(
    modifier: Modifier = Modifier,
    onLetterClicked: (Char) -> Unit
) {
    Column(
        modifier = modifier
            .padding(start = 10.dp, top = 10.dp, end = 5.dp, bottom = 60.dp)
            .width(22.dp)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        ('A'..'Z').toList().forEach { letter ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        indication = ripple(
                            bounded = true,
                            radius = 10.dp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        onClick = { onLetterClicked(letter) },
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter.toString(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}


@Composable
fun PasswordsList(
    state: LazyListState = rememberLazyListState(),
    passwords: List<PasswordMap>, onItemClick: (data: PasswordMap) -> Unit = {},
    onCopyClick: (data: PasswordMap) -> Unit = {},
    onEditClick: (data: PasswordMap) -> Unit = {},
    onDeleteClick: (data: PasswordMap) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    if (passwords.isEmpty()){
        NoPasswords()
    }
    else {
        Row(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 10.dp),
                state = state
            ) {
                items(items = passwords, key = { it.id }) {
                    PasswordItem(it, onItemClick, onCopyClick, onEditClick, onDeleteClick)
                }
            }
            AlphabeticalScrollbar(modifier = Modifier.align(Alignment.CenterVertically)) { letter ->
                passwords.indexOfFirst {
                    it.websiteName[0].lowercase() == letter.lowercase()
                }.let {
                    if (it != -1) {
                        scope.launch {
                            state.scrollToItem(index = it)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun EnterPassword(onClose: ()-> Unit = {}, onConfirm: (String) -> Unit = {}){
    var password by rememberSaveable {
        mutableStateOf("")
    }
    Column(Modifier
        .fillMaxSize()
        .imePadding()
        .background(Color.Transparent), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.elevatedCardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Please provide a password to lock the exported data",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(20.dp))
                TextField(
                    label = "Password",
                    placeholder = "Please enter your password",
                    onValueChange = { password = it },
                    value = password,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Button(
                        modifier = Modifier.width(100.dp),
                        onClick = { onConfirm(password) }

                    ) {
                        Text("Confirm")
                    }
                    Spacer(Modifier.width(25.dp))
                    Button(
                        modifier = Modifier.width(100.dp),
                        onClick = { onClose() }
                    ){
                        Text("Close")
                    }
                }
            }
        }
    }

}

@Composable
fun PasswordItem(
    password: PasswordMap,
    onItemClick: (data: PasswordMap) -> Unit = {},
    onCopyClick: (data: PasswordMap) -> Unit = {},
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
            Text(modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 15.dp), fontSize = 16.sp, text = password.websiteName, fontWeight = FontWeight.Bold, color = Color.Black)
            Row(modifier = Modifier
                .padding(start = 10.dp, end = 10.dp, bottom = 20.dp)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(modifier =  Modifier
                    .weight(1f)
                    .size(18.dp), imageVector = Icons.Filled.Person, contentDescription = "person", tint = Color.Black)
                Text(modifier = Modifier.weight(6f), text = password.username, maxLines = 2, fontSize = 14.sp)
                Image(modifier = Modifier
                    .weight(1f)
                    .size(18.dp)
                    .clickable {
                        onCopyClick(password)
                    }, painter = painterResource(R.drawable.baseline_content_copy_24), contentDescription = "copy", colorFilter = ColorFilter.tint(Color.Black))
                IconButton(modifier = Modifier
                    .weight(1f)
                    .size(18.dp), onClick = {onEditClick(password)}) {
                    Icon(Icons.Filled.Edit, "edit", tint = Color.Black)
                }
                IconButton(modifier = Modifier
                    .weight(1f)
                    .size(18.dp), onClick = {onDeleteClick(password)}) {
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
                    Button(modifier = Modifier.width(100.dp), onClick = onPositiveClick) {
                        Text(stringResource(R.string.confirm))
                    }
                    Spacer(Modifier.width(25.dp))
                    Button(modifier = Modifier.width(100.dp), onClick = onNegativeClick) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SearchPassword(
    enabled: Boolean = true,
    onSearch: (query: String) -> Unit = {}) {
    var searchOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var query by rememberSaveable {
        mutableStateOf("")
    }
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
        label = {Text("Search for passwords")},
        placeholder = {Text("Search by username or website")},
        value = query,
        onValueChange = {
            searchOpen = true
            query = it
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearch(query)
            focusManager.clearFocus()
        }),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedTextColor = Color.Black,
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
           IconButton(onClick = {
               searchOpen = false
               query = ""
               onSearch("")
               focusManager.clearFocus()
           }) {
               Icon(imageVector = if(searchOpen) Icons.Filled.Close else Icons.Outlined.Search, "search or cancel")
           }
        },
        enabled = enabled
    )
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
fun AddOrEditPasswordDialog(currentItem: PasswordMap? = null, onAddNewPassword: (passwordMap: PasswordMap) -> Unit = {}, onClose: () -> Unit = {}, isEditable: Boolean = false, editing: Boolean = false){
    val name = if(editing) currentItem?.websiteName?:"" else ""
    val url = if(editing) currentItem?.websiteUrl?:"" else ""
    val username = if(editing) currentItem?.username?:"" else ""
    val id = if(editing) currentItem?.id ?: "" else ""
    var pass = ""
    if (editing) {
        currentItem?.let {
            if (it.password.isNotEmpty()) pass = CryptoManager.decrypt(it.password)
        }
    }
    var websiteNameState by rememberSaveable {
        mutableStateOf(name)
    }

    var websiteUrlState by rememberSaveable {
        mutableStateOf(url)
    }

    var usernameState by rememberSaveable {
        mutableStateOf(username)
    }

    var passwordState by rememberSaveable {
        mutableStateOf(pass)
    }

    var readOnly by rememberSaveable {
        mutableStateOf(isEditable)
    }

    val context = LocalContext.current

    var selectedLength by rememberSaveable {
        val initialLength = if (pass.isNotEmpty()) {
            // Find the closest available length to pass.length
            availablePasswordLengths.minByOrNull { kotlin.math.abs(it - pass.length) } ?: 15
        } else {
            // Default to 15 if pass is empty
            15
        }
        mutableIntStateOf(initialLength)
    }

    val trailingIcon = if(isEditable) Icons.Outlined.Edit else null
    val onTrailingIconClick = {
        if(readOnly){
            readOnly = false
        }
    }

    fun onLengthSelected(len: Int) {
        selectedLength = len
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
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
                modifier = Modifier
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = if(editing) "Update your password" else "Add new password",
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

                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Length:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    availablePasswordLengths.forEach {
                        PasswordLengthChip(
                            value = it.toString(),
                            isSelected = selectedLength == it,
                            onSelected = { onLengthSelected(it) })
                    }
                }

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, end = 10.dp), horizontalArrangement = Arrangement.End){
                    Text(modifier = Modifier.clickable{
                        passwordState = PasswordGenerator.generateSecurePassword(selectedLength)
                    }, text = "Generate", textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }


                Spacer(Modifier.height(20.dp))

                Row(modifier = Modifier.padding(bottom = 10.dp)) {
                    Button(
                        onClick = {
                            if(websiteNameState.isEmpty() || usernameState.isEmpty() || passwordState.isEmpty()){
                                if(websiteNameState.isEmpty()){
                                    Toast.makeText(context, "Please enter a website name", Toast.LENGTH_SHORT).show()
                                }
                                else if(usernameState.isEmpty()){
                                    Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
                                }
                                else if(passwordState.isEmpty()){
                                    Toast.makeText(context, "Please enter a password", Toast.LENGTH_SHORT).show()
                                }
                                return@Button
                            }
                            onAddNewPassword(
                                PasswordMap(
                                    id = id.let{
                                        it.ifEmpty { "${System.currentTimeMillis()}_${websiteNameState}_${usernameState}" }
                                    },
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
fun PasswordLengthChip(value: String = "15", isSelected: Boolean = false, onSelected: (Int) -> Unit = {}){
    val interactionSource = remember { MutableInteractionSource() }
    Box(modifier = Modifier
        .width(40.dp)
        .height(25.dp)
        .background(
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            shape = RoundedCornerShape(20.dp)
        )
        .border(
            width = 1.dp,
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondary
        )
        .indication(interactionSource, null)
        .clickable(interactionSource = interactionSource, indication = null) {
            onSelected(value.toInt())
        },
        contentAlignment = Alignment.Center
    ){
        Text(
            text = value,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(0.dp)
        )
    }
}

@Preview
@Composable
fun PasswordDetail(data: PasswordMap? = PasswordMap(), onClose: () -> Unit = {}){
    val password = if(data?.password.isNullOrEmpty()) "" else CryptoManager.decrypt(data.password)
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
                Text("Last Modified: ${data?.getDate()}", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Button(modifier = Modifier.width(100.dp), onClick = { onClose() }) {
                    Text("Close")
                }

            }
        }

    }
}

@Preview
@Composable
fun TextField(
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    value: String = "",
    onValueChange: (String) -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: () -> Unit = {},
    imeAction: ImeAction = ImeAction.Done,
){
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        label = {Text(label)},
        placeholder = {Text(placeholder)},
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedTextColor = Color.Black,
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
    PasswordItem(PasswordMap(websiteName = "test", websiteUrl = "test", username = "test", password = "test"))
}


@Preview(showBackground = true)
@Composable
fun PasswordsListPreview() {
    PasswordsList(passwords = listOf(PasswordMap(websiteUrl = "test1", websiteName =  "test1", username = "test", password = "test"),
        PasswordMap(websiteUrl = "test2", websiteName =  "test2", username = "test", password = "test"),
        PasswordMap(websiteUrl = "test3", websiteName =  "test3", username = "test", password = "test")    )
    )
}



fun Context.copyToClipboard(label: String, text: CharSequence) {
    val clipboardManager = ContextCompat.getSystemService(this, ClipboardManager::class.java)
    val clip = ClipData.newPlainText(label, text)
    clipboardManager?.setPrimaryClip(clip)
    Toast.makeText(this, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
}