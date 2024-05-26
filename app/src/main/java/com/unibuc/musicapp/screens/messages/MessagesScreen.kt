package com.unibuc.musicapp.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.login.LoginViewModel
import com.unibuc.musicapp.screens.users.UserItem

@Composable
fun MessagesScreen(
    navController: NavController,
    viewModel: MessagesViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    if (!loginViewModel.isLoggedIn()) {
        navController.navigate(MusicScreens.LoginScreen.name)
    } else {
        val users by viewModel.users.observeAsState(initial = emptyList())
        LaunchedEffect(Unit) {
            viewModel.loadMatches()
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
              MatchesList(navController = navController, users = users, loginViewModel = loginViewModel)
            }
        }
    }
}

@Composable
fun MatchesList(navController: NavController, users: List<UserDto>, loginViewModel: LoginViewModel) {
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.padding(bottom = 56.dp), state = listState) {
        items(users) { user ->
            UserItem(user = user) {
                // TODO navigate to conversation
                navController.navigate(MusicScreens.ChatScreen.routeWithParameters(user.id.toString()))
            }
        }
    }
}