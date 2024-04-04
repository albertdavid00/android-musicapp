package com.unibuc.musicapp.navigation

enum class MusicScreens {
    LoginScreen,
    RegisterScreen,
    FeedScreen,
    ProfileScreen,
    CreatePostScreen,
    UsersScreen,
    MatchingScreen;

    fun routeWithParameters(param1: String? = null, param2: String? = null): String {
        return when (this) {
            UsersScreen -> {
                when {
                    param1 != null && param2 != null -> "$name/$param1/$param2"
                    param1 != null -> "$name/$param1"
                    else -> name
                }
            }
            ProfileScreen -> {
                when {
                    param1 != null -> "$name/$param1"
                    else -> name
                }
            }
            else -> name
        }
    }

    companion object {
        fun fromRoute(route: String?): MusicScreens
                = when (route?.substringBefore("/")) {
            FeedScreen.name -> FeedScreen
            ProfileScreen.name -> ProfileScreen
            LoginScreen.name -> LoginScreen
            RegisterScreen.name -> RegisterScreen
            UsersScreen.name -> UsersScreen
            CreatePostScreen.name -> CreatePostScreen
            MatchingScreen.name -> MatchingScreen


            null -> FeedScreen
            else -> throw IllegalArgumentException("Route $route is not recognised!")
        }
    }
}