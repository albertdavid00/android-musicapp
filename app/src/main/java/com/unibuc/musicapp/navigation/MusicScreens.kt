package com.unibuc.musicapp.navigation

enum class MusicScreens {
    LoginScreen,
    RegisterScreen,
    FeedScreen,
    ProfileScreen,
    SearchScreen,
    FollowScreen;

    companion object {
        fun fromRoute(route: String?): MusicScreens
                = when (route?.substringBefore("/")) {
            FeedScreen.name -> FeedScreen
            ProfileScreen.name -> ProfileScreen
            LoginScreen.name -> LoginScreen
            RegisterScreen.name -> RegisterScreen
            FollowScreen.name -> FollowScreen


            null -> FeedScreen
            else -> throw IllegalArgumentException("Route $route is not recognised!")
        }
    }
}