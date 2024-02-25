package com.unibuc.musicapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.unibuc.musicapp.screens.feed.FeedScreen
import com.unibuc.musicapp.screens.login.LoginScreen
import com.unibuc.musicapp.screens.profile.ProfileScreen
import com.unibuc.musicapp.screens.register.RegisterScreen

@Composable
fun MusicNavigation(navController: NavHostController) {

    NavHost(navController = navController, startDestination = MusicScreens.LoginScreen.name) {
        composable(MusicScreens.LoginScreen.name) {
            LoginScreen(navController)
        }
        composable(MusicScreens.FeedScreen.name) {
            FeedScreen(navController)
        }
        composable(MusicScreens.RegisterScreen.name) {
            RegisterScreen(navController)
        }
        composable(MusicScreens.SearchScreen.name) {
            LoginScreen(navController)
        }
        composable(MusicScreens.FollowScreen.name) {
            LoginScreen(navController)
        }
        composable(MusicScreens.ProfileScreen.name) {
            ProfileScreen(navController)
        }
    }
}