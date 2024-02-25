package com.unibuc.musicapp.screens.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.login.LoginViewModel

@Composable
fun FeedScreen(navController: NavController, loginViewModel: LoginViewModel = hiltViewModel()) {

    Column {
        Text(text = "FEED")
        Button(
            onClick = { logoutAndRedirect(navController, loginViewModel) },
            modifier = Modifier
                .padding(3.dp)
                .fillMaxWidth(),
        ) {
            Text(text= "Log Out", modifier = Modifier.padding(5.dp))
        }

    }

}

fun logoutAndRedirect(navController: NavController, loginViewModel: LoginViewModel) {
    loginViewModel.logout()
    navController.navigate(MusicScreens.LoginScreen.name) {
        popUpTo(navController.graph.startDestinationId) {
            inclusive = true
        }
    }
}
