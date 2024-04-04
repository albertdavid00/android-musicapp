package com.unibuc.musicapp.screens.match

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.feed.PostsList
import com.unibuc.musicapp.screens.login.LoginViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MatchingScreen(
    navController: NavController,
    viewModel: MatchingViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    if (!loginViewModel.isLoggedIn()) {
        navController.navigate(MusicScreens.LoginScreen.name)
    } else {
        LaunchedEffect(Unit) {
           //TODO load data
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            Text(text = "Salut boss")
        }
    }

}