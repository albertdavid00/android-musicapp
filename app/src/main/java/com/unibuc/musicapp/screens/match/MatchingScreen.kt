package com.unibuc.musicapp.screens.match
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.login.LoginViewModel
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.unibuc.musicapp.components.ImageDialog
import com.unibuc.musicapp.components.timeFromNow
import com.unibuc.musicapp.dto.LocationDto
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.screens.feed.CommentsButton
import com.unibuc.musicapp.screens.feed.LikeButton
import com.unibuc.musicapp.screens.feed.VideoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
        val context = LocalContext.current
        var hasLocationPermission by remember {mutableStateOf(false)}
        val userLocation by viewModel.userLocation.observeAsState()
        val users by viewModel.users.observeAsState()
        val currentIndex = viewModel.currentUserIndex.observeAsState()
        val currentVideoIndex = remember { mutableStateOf(0) }
        val showMatchDialog = viewModel.showMatchDialog.collectAsState()
        val matchedUserName = viewModel.matchedUserName.collectAsState()

        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                hasLocationPermission = isGranted
                Log.d("MatchingScreen", "Asking for permission")
                if (isGranted) {
                    Log.d("MatchingScreen", "Permission granted")
                    viewModel.saveUserLocation(context)
                }
            }
        )
        LaunchedEffect(Unit) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        LaunchedEffect(userLocation) {
            userLocation?.let {
                viewModel.getUserRecommendations()
            }
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            if (hasLocationPermission) {
                users?.let { userList ->
                    if (userList.isNotEmpty() && currentIndex.value != null && currentIndex.value!! < userList.size) {
                        val user = userList[currentIndex.value!!]
                        LaunchedEffect(key1 = user) {
                            currentVideoIndex.value = 0  // Reset video index when user changes
                        }
                        AnimatedVisibility(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 56.dp),
                            visible = true,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            UserCard(user = user, currentVideoIndex = currentVideoIndex,
                                onMatchAction = {
                                    viewModel.likeUserAction(it)

                                    currentVideoIndex.value = 0
                                    Log.d("MatchingScreen", "Video Index: $currentVideoIndex")
                                    viewModel.nextUser()
                                },
                                onDismissAction = {
                                    viewModel.dislikeUserAction(it)
                                    currentVideoIndex.value = 0
                                    Log.d("MatchingScreen", "Video Index: $currentVideoIndex")
                                    viewModel.nextUser()
                            })
                        }
                    } else {
                        Column(modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Text(text = "Nobody new around you, for the moment.",
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                    if (showMatchDialog.value) {
                        Dialog(onDismissRequest = { viewModel.dismissMatchDialog() }) {
                            Column(
                                modifier = Modifier
                                    .width(300.dp)
                                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Congratulations! \uD83C\uDF89",
                                    style = MaterialTheme.typography.h5,
                                    color = MaterialTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "You matched with ${matchedUserName.value}",
                                    style = MaterialTheme.typography.body1
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { viewModel.dismissMatchDialog() }
                                    ) {
                                        Text("OK")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(text = "Permission is required to access the location.")
            }
        }
    }

}
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserCard(user: UserDto, currentVideoIndex: MutableState<Int>, onMatchAction: (UserDto) -> Unit, onDismissAction: (Long) -> Unit) {
    LocalLifecycleOwner.current
    val interactionSource = remember { MutableInteractionSource() }

    val maxIndex = user.postsList.size - 1
    val swipeableState = rememberSwipeableState(initialValue = currentVideoIndex.value)
    var showEnlargedProfilePicture by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    val screenWidthPx = with(density) { screenWidth.roundToPx() }
    // Calculate anchors such that they don't overlap in value for different swipes
    val anchors = remember(currentVideoIndex.value) {
        val default = mapOf(0f to currentVideoIndex.value)  // No swipe
        val swipeRight = if (currentVideoIndex.value > 0) mapOf(screenWidthPx.toFloat() to currentVideoIndex.value - 1) else emptyMap()
        val swipeLeft = if (currentVideoIndex.value < maxIndex) mapOf(-screenWidthPx.toFloat() to currentVideoIndex.value + 1) else emptyMap()
        default + swipeRight + swipeLeft
    }
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
            .indication(interactionSource, LocalIndication.current),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier
            .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp), horizontalAlignment = Alignment.Start) {

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp)
                ) {
                    Row (verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(user.profilePictureUrl),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.Gray, CircleShape)
                                .clickable { showEnlargedProfilePicture = true },
                            contentScale = ContentScale.Crop
                        )
                        if (showEnlargedProfilePicture) {
                            ImageDialog(user.profilePictureUrl) { showEnlargedProfilePicture = false }
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = user.lastName + " " + user.firstName, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = if (user.location.city != null) user.location.city else "" ,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(text = user.instrumentsPlayed.joinToString(separator = " / "),
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold)
            }
            LaunchedEffect(swipeableState.currentValue) {
                currentVideoIndex.value = swipeableState.currentValue
            }
            if (user.postsList.isNotEmpty()) {
                VideoIndicator(
                    totalVideos = user.postsList.size,
                    currentIndex = currentVideoIndex.value,
                    activeColor = MaterialTheme.colors.primary, // Or any specific color you want
                    inactiveColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.padding(top = 8.dp)
                )
                VideoPlayer(videoUrl = user.postsList[currentVideoIndex.value].videoUrl) {}
            }

            Row (verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ){
                // Heart icon button
                IconButton(onClick = { onMatchAction(user)}) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp) // Larger size to accommodate background circle
                            .background(color = Color(0xFFEEECEC), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }

                IconButton(onClick = { onDismissAction(user.id) }) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .background(color = Color(0xFFEEECEC), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(32.dp),
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoIndicator(totalVideos: Int, currentIndex: Int, activeColor: Color, inactiveColor: Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until totalVideos) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(width = 40.dp, height = 4.dp)  // Line size
                    .clip(RoundedCornerShape(50))  // Optional: rounded corners
                    .background(if (i == currentIndex) activeColor else inactiveColor)

            )
        }
    }
}
