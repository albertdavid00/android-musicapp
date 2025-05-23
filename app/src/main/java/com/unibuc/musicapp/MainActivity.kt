package com.unibuc.musicapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unibuc.musicapp.navigation.MusicNavigation
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.feed.logoutAndRedirect
import com.unibuc.musicapp.screens.login.LoginViewModel
import com.unibuc.musicapp.ui.theme.MusicappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicappTheme(darkTheme = false) {

                MusicApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MusicApp(loginViewModel: LoginViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val showBackButton = shouldShowBackButton(currentRoute)
            val showLogoutButton = shouldShowLogoutButton(currentRoute)
            val showMessagesButton = shouldShowMessagesButton(currentRoute)
            val showContactsButton = shouldShowContactsButton(currentRoute)
            val title = getTitle(currentRoute)
            if (currentRoute !in listOf(MusicScreens.LoginScreen.name, MusicScreens.RegisterScreen.name)) {
                TopAppBar(
                    title = {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (showBackButton) {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onPrimary)
                                    }
                                }
                            }
                            Box(modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = title, color = Color.White, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                            }
                            Box(modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                // Invisible spacer for balance
                                if (showBackButton)
                                    Spacer(modifier = Modifier.size(48.dp))
                                if (showLogoutButton) {
                                    IconButton(onClick = { logoutAndRedirect(navController, loginViewModel) }) {
                                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colors.onPrimary)
                                    }
                                }
                                if (showMessagesButton) {
                                    IconButton(onClick = { navController.navigate(MusicScreens.MessagesScreen.name) }) {
                                        Icon(imageVector = Icons.Default.Message, contentDescription = "Messages", tint = MaterialTheme.colors.onPrimary)
                                    }
                                }
                                if (showContactsButton) {
                                    IconButton(onClick = { navController.navigate(MusicScreens.ContactsScreen.name) }) {
                                        Icon(imageVector = Icons.Default.Contacts, contentDescription = "Contacts", tint = MaterialTheme.colors.onPrimary)
                                    }
                                }
                            }
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primary,
                )
            }
        },
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute !in listOf(
                    MusicScreens.LoginScreen.name,
                    MusicScreens.RegisterScreen.name,
                    MusicScreens.ProfileScreen.routeWithParameters("{userId}"),
                    MusicScreens.ChatScreen.routeWithParameters("{userId}")
                )) {
                BottomNavigation(backgroundColor = MaterialTheme.colors.primary) {

                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.Home,
                            contentDescription = "Feed",
                            tint = if (currentRoute == MusicScreens.FeedScreen.name)
                                MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                        )},
                        selected = currentRoute == MusicScreens.FeedScreen.name,
                        onClick = { navController.navigate(MusicScreens.FeedScreen.name) }
                    )

                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.MusicNote,
                            contentDescription = "Matching",
                            tint = if (currentRoute == MusicScreens.MatchingScreen.name)
                                MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface) },
                        selected = currentRoute == MusicScreens.MatchingScreen.name,
                        onClick = { navController.navigate( MusicScreens.MatchingScreen.name) }
                    )

                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.AddCircle,
                            contentDescription = "Upload",
                            tint = if (currentRoute == MusicScreens.CreatePostScreen.name)
                                MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                        )},
                        selected = currentRoute == MusicScreens.CreatePostScreen.name,
                        onClick = { navController.navigate(MusicScreens.CreatePostScreen.name) }
                    )

                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = if (currentRoute == MusicScreens.UsersScreen.name)
                                MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                        )},
                        selected = currentRoute == MusicScreens.UsersScreen.name,
                        onClick = { navController.navigate(MusicScreens.UsersScreen.name) }
                    )

                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = if (currentRoute == MusicScreens.ProfileScreen.name)
                                MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface) },
                        selected = currentRoute == MusicScreens.ProfileScreen.name,
                        onClick = { navController.navigate( MusicScreens.ProfileScreen.name) }
                    )

                    // Add more BottomNavigationItem as needed
                }
            }
        }
    ) {
                MusicNavigation(navController)
    }
}



fun shouldShowBackButton(currentRoute: String?): Boolean {
    return currentRoute in listOf(
        MusicScreens.UsersScreen.routeWithParameters("{followParam}", "{userId}"),
        MusicScreens.ProfileScreen.routeWithParameters("{userId}"),
        MusicScreens.ChatScreen.routeWithParameters("{userId}")
    )
}

fun shouldShowLogoutButton(currentRoute: String?): Boolean {
    return currentRoute in listOf(
        MusicScreens.ProfileScreen.name
    )
}

fun shouldShowMessagesButton(currentRoute: String?): Boolean {
    return currentRoute in listOf(
        MusicScreens.MatchingScreen.name
    )
}

fun shouldShowContactsButton(currentRoute: String?): Boolean {
    return currentRoute in listOf(
        MusicScreens.FeedScreen.name, MusicScreens.UsersScreen.name
    )
}

fun getTitle(currentRoute: String?): String {
    if (currentRoute in listOf(MusicScreens.CreatePostScreen.name)) {
        return "Create Post"
    }
    return "MuSync"
}
//@Composable
//fun MusicHome( viewModel: UsersViewModel = hiltViewModel()) {
//    Users(viewModel)
//}
//@Composable
//fun Users( viewModel: UsersViewModel) {
//    Text(text = "Hello world!")
//    val users = viewModel.data.value.data?.toMutableList()
//    if(viewModel.data.value.loading == true) {
//        CircularProgressIndicator()
//    } else {
//        Log.d("SIZE", "USERS: ${users?.size}")
//    }
//}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
}