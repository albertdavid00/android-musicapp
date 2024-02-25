package com.unibuc.musicapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unibuc.musicapp.navigation.MusicNavigation
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.repository.AuthRepository
import com.unibuc.musicapp.screens.login.LoginScreen
import com.unibuc.musicapp.screens.UsersViewModel
import com.unibuc.musicapp.ui.theme.MusicappTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicappTheme {

                MusicApp()
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MusicApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute !in listOf(MusicScreens.LoginScreen.name, MusicScreens.RegisterScreen.name)) {
                BottomNavigation(backgroundColor = Color.Cyan) {

                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.Feed, contentDescription = "Feed") },
                        //label = { Text("Login") },
                        selected = currentRoute == MusicScreens.FeedScreen.name,
                        onClick = { navController.navigate(MusicScreens.FeedScreen.name) }
                    )
                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        //label = { Text("Register") },
                        selected = currentRoute == MusicScreens.ProfileScreen.name,
                        onClick = { navController.navigate( MusicScreens.ProfileScreen
                            .name) }
                    )
                    // Add more BottomNavigationItem as needed
                }
            }
        }
    ) {
                MusicNavigation(navController)
    }
}

@Composable
fun MusicHome( viewModel: UsersViewModel = hiltViewModel()) {
    Users(viewModel)
}
@Composable
fun Users( viewModel: UsersViewModel) {
    Text(text = "Hello world!")
    val users = viewModel.data.value.data?.toMutableList()
    if(viewModel.data.value.loading == true) {
        CircularProgressIndicator()
    } else {
        Log.d("SIZE", "USERS: ${users?.size}")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
}