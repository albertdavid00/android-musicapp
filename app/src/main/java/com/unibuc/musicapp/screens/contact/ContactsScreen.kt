package com.unibuc.musicapp.screens.contact

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.login.LoginViewModel
import com.unibuc.musicapp.screens.users.UserItem

@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    if (!loginViewModel.isLoggedIn()) {
        navController.navigate(MusicScreens.LoginScreen.name)
    } else {
        val users by viewModel.users.observeAsState(initial = emptyList())
        LaunchedEffect(Unit) {
            viewModel.loadContacts()
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier= Modifier.fillMaxWidth()) {
                Column(modifier= Modifier.fillMaxWidth().padding(10.dp)){
                    Text(text = "Contacts", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Divider()
                }
                ContactsList(navController = navController, users = users, loginViewModel = loginViewModel)
            }
        }
    }
}

@Composable
fun ContactsList(navController: NavController, users: List<UserDto>, loginViewModel: LoginViewModel) {
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.padding(bottom = 56.dp), state = listState) {
        items(users) { user ->
            UserItem(user = user) {
                navController.navigate(MusicScreens.ChatScreen.routeWithParameters(user.id.toString()))
            }
        }
    }
}