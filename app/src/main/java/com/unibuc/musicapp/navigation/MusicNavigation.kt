package com.unibuc.musicapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.unibuc.musicapp.screens.chat.ChatScreen
import com.unibuc.musicapp.screens.feed.FeedScreen
import com.unibuc.musicapp.screens.login.LoginScreen
import com.unibuc.musicapp.screens.match.MatchingScreen
import com.unibuc.musicapp.screens.messages.MessagesScreen
import com.unibuc.musicapp.screens.post.CreatePostScreen
import com.unibuc.musicapp.screens.profile.ProfileScreen
import com.unibuc.musicapp.screens.register.RegisterScreen
import com.unibuc.musicapp.screens.users.UsersScreen

@RequiresApi(Build.VERSION_CODES.O)
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
        composable(MusicScreens.CreatePostScreen.name) {
            CreatePostScreen(navController)
        }
        composable(
            route = MusicScreens.UsersScreen.routeWithParameters("{followParam}", "{userId}")
        ) { backStackEntry ->
            val param1 = backStackEntry.arguments?.getString("followParam")
            val param2 = backStackEntry.arguments?.getString("userId")!!
            UsersScreen(navController = navController, followParam = param1, userId = param2.toLongOrNull())
        }
        composable(MusicScreens.UsersScreen.name) {
            UsersScreen(navController = navController)
        }
        composable(MusicScreens.ProfileScreen.name) {
            ProfileScreen(navController)
        }
        composable(
            route = MusicScreens.ProfileScreen.routeWithParameters("{userId}")
        ) { backStackEntry ->
            val param = backStackEntry.arguments?.getString("userId")!!
            ProfileScreen(navController = navController, userId = param.toLongOrNull())
        }
        composable(MusicScreens.MatchingScreen.name) {
            MatchingScreen(navController)
        }
        composable(MusicScreens.MessagesScreen.name) { 
            MessagesScreen(navController = navController)
        }
        composable(
            route = MusicScreens.ChatScreen.routeWithParameters("{userId}")
        ) { backStackEntry ->
            val param = backStackEntry.arguments?.getString("userId")!!
            ChatScreen(navController = navController, userId = param.toLong())
        }
    }
}