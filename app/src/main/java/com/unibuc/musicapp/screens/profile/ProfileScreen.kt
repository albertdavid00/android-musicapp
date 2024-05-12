package com.unibuc.musicapp.screens.profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.unibuc.musicapp.components.ImageDialog
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.feed.FeedViewModel
import com.unibuc.musicapp.screens.feed.PostItem
import com.unibuc.musicapp.screens.feed.PostsList
import com.unibuc.musicapp.screens.login.LoginViewModel
import com.unibuc.musicapp.utils.FollowParam

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(navController: NavController,
                  userId: Long? = null,
                  viewModel: ProfileViewModel = hiltViewModel(),
                  loginViewModel: LoginViewModel = hiltViewModel()) {
    val userProfile by viewModel.userInfo.observeAsState()
    val userPosts by viewModel.userPosts.observeAsState()
    if (!loginViewModel.isLoggedIn()) {
        navController.navigate(MusicScreens.LoginScreen.name)
    } else {
        LaunchedEffect(userId) {
                viewModel.loadUserInfo(userId)
                viewModel.loadUserPosts(userId)
        }
        if (userProfile == null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            var bottomBarPadding = 56.dp
            if (userId != null) {
                bottomBarPadding = 0.dp
            }
            var showEnlargedProfilePicture by remember { mutableStateOf(false) }
            Surface(modifier = Modifier.fillMaxSize()) {
                val listState = rememberLazyListState()
                LazyColumn(modifier = Modifier.padding(bottom = bottomBarPadding), state= listState) {
                    item {
                        UserInfo(
                            userId = userId,
                            viewModel = viewModel,
                            userProfile = userProfile,
                            navController = navController,
                            loginViewModel = loginViewModel,
                            showEnlargedProfilePicture = showEnlargedProfilePicture,
                            setShowEnlargedProfilePicture = { showEnlargedProfilePicture = it}
                        )
                    }
                    userPosts?.let {
                        items(userPosts!!) { post ->
                            PostItem(
                                post = post,
                                onSeen = {},
                                navController = navController,
                                loginViewModel = loginViewModel,
                                likeAction = { viewModel.likeAction(it) },
                                addCommentAction = { addCommentDto, id -> viewModel.addComment(addCommentDto, id) },
                                likeCommentAction = { commentDto, feedPostDto -> viewModel.likeCommentAction(commentDto, feedPostDto)},
                                removeCommentAction = { commentDto, feedPostDto -> viewModel.removeComment(commentDto, feedPostDto)},
                                removePostAction = { viewModel.removePost(it)}

                            )
                        }
                        if (userId != null) {
                            item {
                                Box(modifier = Modifier.height(12.dp)) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserInfo(
    userId: Long?,
    userProfile: UserDto?,
    viewModel: ProfileViewModel,
    navController: NavController,
    loginViewModel: LoginViewModel,
    showEnlargedProfilePicture: Boolean,
    setShowEnlargedProfilePicture: (Boolean) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top){
        Row (horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(userProfile!!.profilePictureUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .padding(top = 30.dp, end = 15.dp)
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { setShowEnlargedProfilePicture(true) },
                contentScale = ContentScale.Crop // Adjust scaling to fit or fill as needed
            )
            if (showEnlargedProfilePicture) {
                ImageDialog(userProfile!!.profilePictureUrl) {setShowEnlargedProfilePicture(false)  }
            }
            Column {
                Text(
                    text = userProfile!!.firstName + " " + userProfile!!.lastName,
                    modifier = Modifier
                        .padding(top = 5.dp),
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = userProfile!!.age.toString() + " yo",
                    color = Color.Gray,
                    style = TextStyle(fontSize = 14.sp, fontStyle = FontStyle.Italic)
                )
            }
        }
        Column (
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp, start = 32.dp, end = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,

                    ) {
                    Text(text = "Posts", fontSize = 14.sp)
                    Button(
                        onClick = {  },
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                        Text(text = userProfile!!.posts.toString(),
                            color = Color.Black,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    }
                }

                // Spacer with a weight modifier to take up available space
                Spacer(Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
                    Text(text = "Following", fontSize = 14.sp)
                    Button(
                        onClick = {
                            if (userId != null) {
                                navController.navigate(MusicScreens.UsersScreen.routeWithParameters(FollowParam.FOLLOWING.name, userId.toString()))
                            } else {
                                navController.navigate(MusicScreens.UsersScreen.routeWithParameters(FollowParam.FOLLOWING.name, loginViewModel.getUserId().toString()))
                            }},
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                        Text(
                            text = userProfile!!.following.toString(),
                            color = Color.Black,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(text = "Followers", fontSize = 14.sp)
                    Button(
                        onClick = {
                            if (userId != null) {
                                navController.navigate(MusicScreens.UsersScreen.routeWithParameters(FollowParam.FOLLOWERS.name, userId.toString()))
                            } else {
                                navController.navigate(MusicScreens.UsersScreen.routeWithParameters(FollowParam.FOLLOWERS.name, loginViewModel.getUserId().toString()))
                            }},
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = userProfile!!.followers.toString(),
                            color = Color.Black,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }

            }

            if (userId != null && loginViewModel.getUserId() != userId) {
                Row {
                    FollowButton (isFollowing = userProfile!!.isFollowedByCurrentUser) {
                        viewModel.followUnfollowAction(userId)
                    }
                }
            }
        }
    }
}




@Composable
fun FollowButton(isFollowing: Boolean, onFollowToggle: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isFollowing) Color(0xFFD3D3D3) else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 500), label = ""
    )
    val textColor by animateColorAsState(
        targetValue = if (isFollowing) Color.Black else Color.White,
        animationSpec = tween(durationMillis = 500), label = ""
    )

    Button(
        onClick = onFollowToggle,
        shape = RoundedCornerShape(50),  // Rounded corners
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        modifier = Modifier
            .size(width = 155.dp, height = 40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isFollowing) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Following",
                    modifier = Modifier.padding(end = 1.dp)  // Add some padding to the end of the icon
                )
            }
            Text(
                text = if (isFollowing) "Following" else "Follow",
                style = TextStyle(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 3.dp)
            )

        }
    }
}