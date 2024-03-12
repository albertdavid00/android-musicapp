package com.unibuc.musicapp.screens.users

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.unibuc.musicapp.components.ImageDialog
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.login.LoginViewModel

@Composable
fun UsersScreen(navController: NavController,
                followParam: String? = null,
                userId: Long? = null,
                viewModel: UsersViewModel = hiltViewModel(),
                loginViewModel: LoginViewModel = hiltViewModel()) {
    Surface(modifier = Modifier.fillMaxSize()) {
        val users by viewModel.users.observeAsState(initial = emptyList())
        LaunchedEffect(Unit) {
            if (followParam != null && userId != null) {
                viewModel.loadUsers(userId, followParam)
            }
        }
        val isLoading by viewModel.isLoading.observeAsState(false)
        var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
        val filteredUsers = if (searchQuery.text.isEmpty()) {
            users
        } else {
            users.filter {
                it.firstName.contains(searchQuery.text, ignoreCase = true) ||
                        it.lastName.contains(searchQuery.text, ignoreCase = true)
            }
        }
        Column {
            if (followParam != null) { // UserFollow Screen
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                ){
                    Text(text = followParam.lowercase().replaceFirstChar {it.titlecase() },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.padding(end = 8.dp),
                        fontStyle = FontStyle.Italic
                    )
                }
                SearchBar(query = searchQuery,
                        onQueryChanged = { newValue -> searchQuery = newValue}) { }
                UserList(navController, users = filteredUsers, loginViewModel)
            } else {    // UserSearch screen
                SearchBar(searchQuery, onQueryChanged = { newValue -> searchQuery = newValue}) {
                    viewModel.searchUsers(searchQuery.text)
                }
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(120.dp),
                            strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth * 2
                        )
                    }
                } else {
                    UserList(navController, users = users, loginViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(query: TextFieldValue, onQueryChanged: (TextFieldValue) -> Unit, onSearchTriggered: () -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        trailingIcon = {
            IconButton(onClick = {
                onSearchTriggered()
                keyboardController?.hide()
            }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search Icon"
                )
            }
        },
        label = { Text("Search") },
        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.White,
            cursorColor = Color.Black,
            leadingIconColor = MaterialTheme.colors.onSurface,
            focusedIndicatorColor = MaterialTheme.colors.primary,
            unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
            focusedLabelColor = MaterialTheme.colors.primary
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearchTriggered()
            keyboardController?.hide()
        })
    )
}

@Composable
fun UserList(navController: NavController, users: List<UserDto>, loginViewModel: LoginViewModel) {
    LazyColumn(modifier = Modifier.padding(bottom = 56.dp)) {
        items(users) { user ->
            UserItem(user = user) {
                if (user.id != loginViewModel.getUserId()) {
                    navController.navigate(MusicScreens.ProfileScreen.routeWithParameters(user.id.toString()))
                } else {
                    navController.navigate(MusicScreens.ProfileScreen.name)
                }
            }
        }
    }
}

@Composable
fun UserItem(user: UserDto, onItemClick: () -> Unit) {
    var showEnlargedProfilePicture by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onItemClick() },
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(user.profilePictureUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color.Gray, CircleShape)
                    .clickable { showEnlargedProfilePicture = true },
                contentScale = ContentScale.Crop
            )
            if (showEnlargedProfilePicture) {
                ImageDialog(user.profilePictureUrl) { showEnlargedProfilePicture = false }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.firstName + " " + user.lastName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Row {
                    Text(
                        text = user.age.toString() + " yo",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        style = TextStyle(fontStyle = FontStyle.Italic)
                    )
                }
            }
        }
    }
}

