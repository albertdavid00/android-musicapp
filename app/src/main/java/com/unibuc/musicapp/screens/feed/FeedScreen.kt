package com.unibuc.musicapp.screens.feed

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.unibuc.musicapp.R
import com.unibuc.musicapp.components.VisibilityIcon
import com.unibuc.musicapp.components.formatNumber
import com.unibuc.musicapp.components.timeFromNow
import com.unibuc.musicapp.dto.AddCommentDto
import com.unibuc.musicapp.dto.CommentDto
import com.unibuc.musicapp.dto.FeedPostDto
import com.unibuc.musicapp.dto.GetPostDto
import com.unibuc.musicapp.dto.PostDto
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.login.LoginViewModel
import com.unibuc.musicapp.screens.users.UserItem
import com.unibuc.musicapp.utils.Visibility
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val posts by viewModel.feedData.observeAsState(initial = null)
    val fetchedData by viewModel.fetchedData.collectAsState();
    if (!loginViewModel.isLoggedIn()) {
        navController.navigate(MusicScreens.LoginScreen.name)
    } else {
        LaunchedEffect(Unit) {
            if(posts == null)
                viewModel.loadFeedData()
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            if (fetchedData && posts != null && posts!!.isEmpty()) {
                Column(modifier= Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(text = "No new posts yet.", fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
            }
            posts?.let { PostsList(navController = navController, posts = it, viewModel = viewModel, loginViewModel = loginViewModel) }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PostsList(navController: NavController, viewModel: FeedViewModel, loginViewModel: LoginViewModel, posts: List<FeedPostDto>) {
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.padding(bottom = 56.dp), state= listState) {
        items(posts) { post ->
            PostItem(
                post = post,
                navController = navController,
                loginViewModel = loginViewModel,
                likeAction = { viewModel.likeAction(it) },
                likeCommentAction = { commParam, postParam -> viewModel.likeCommentAction(commParam, postParam) },
                addCommentAction = { addCommentDto, id -> viewModel.addComment(addCommentDto, id) },
                removeCommentAction = { commentDto, feedPostDto ->  viewModel.removeComment(commentDto, feedPostDto)},
                removePostAction = {},
                onSeen = {
                    if (!post.seen) {
                        viewModel.markPostAsSeen(post.id)
                    }
                }
            )
//            PostItem(post = post, viewModel = viewModel, navController = navController, loginViewModel = loginViewModel) {
//                if (!post.seen) {
//                    viewModel.markPostAsSeen(post.id)
//                }
//            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PostItem(
    post: FeedPostDto,
    loginViewModel: LoginViewModel,
    navController: NavController,
    likeAction: (FeedPostDto) -> Unit,
    removePostAction: (FeedPostDto) -> Unit,
    likeCommentAction: (CommentDto, FeedPostDto) -> Unit,
    addCommentAction: (AddCommentDto, Long) -> Unit,
    removeCommentAction: (CommentDto, FeedPostDto) -> Unit,
    onSeen: () -> Unit
) {
    val context = LocalContext.current
    LocalLifecycleOwner.current
    var showDialog by remember { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var isExpandable by remember { mutableStateOf(false) }
    val initiallyOverflowing = remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = true)
        ) {
            Column(
                modifier = Modifier
                    .width(230.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Remove Post?", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.4.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            removePostAction(post)
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                        contentPadding = PaddingValues(
                            horizontal = 4.dp,
                            vertical = 2.dp
                        )
                    ) {
                        Text(text = "Remove", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                        contentPadding = PaddingValues(
                            horizontal = 4.dp,
                            vertical = 2.dp
                        )
                    ) {
                        Text(text = "Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(interactionSource) {
                detectTapGestures(onLongPress = {
                    if (post.userDto.id == loginViewModel.getUserId()) {
                        coroutineScope.launch {
                            val press = PressInteraction.Press(Offset.Zero)
                            interactionSource.emit(press)
                            showDialog = true
                            interactionSource.emit(PressInteraction.Release(press))
                        }
                    }
                })
            }
            ,
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier
            .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp), horizontalAlignment = Alignment.Start) {

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp)
                ) {
                    Row (verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.clickable {
                            navController.navigate(MusicScreens.ProfileScreen.routeWithParameters(post.userDto.id.toString()))
                        }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(post.userDto.profilePictureUrl),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.Gray, CircleShape)
                                .clickable {},
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = post.userDto.lastName + " " + post.userDto.firstName, fontWeight = FontWeight.Bold)
                    }
                    Column(verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
                        // if(post.userDto.id != null && post.userDto.id == loginViewModel.getUserId()) // if we want only the user of the post to see the icon
                        VisibilityIcon(visibilityType = post.visibility)

                        Text(
                            text = timeFromNow(post.creationTime),
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(modifier = Modifier
                    .clickable { if (isExpandable) isDescriptionExpanded = !isDescriptionExpanded }
                    .padding(vertical = if (!isExpandable) 15.dp else 0.dp)) {
                    Text(
                        text = post.description,
                        maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp,
                        style = TextStyle(letterSpacing = (-0.5).sp),
                        onTextLayout = { textLayoutResult: TextLayoutResult ->
                            val isTextOverflowing = textLayoutResult.hasVisualOverflow
                            if (!initiallyOverflowing.value) {
                                initiallyOverflowing.value = isTextOverflowing
                                isExpandable = isTextOverflowing
                            }
                        }
                    )
                    if (isExpandable || isDescriptionExpanded) {
                        Text(
                            text = if (isDescriptionExpanded) "Read less" else "Read more",
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            VideoPlayer(videoUrl = post.videoUrl) {
                onSeen()
            }
            Row (verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ){
                LikeButton(
                    post = post,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                ) {
                    likeAction(post)
                }
                CommentsButton(
                    post,
                    modifier = Modifier.weight(1f),
                    likeCommentAction = likeCommentAction,
                    addCommentAction = addCommentAction,
                    navController = navController,
                    removeCommentAction = removeCommentAction,
                    loginViewModel = loginViewModel
                )
            }
        }
    }

}

@Composable
fun LikeButton(post: FeedPostDto, modifier: Modifier, likeAction: () -> Unit) {

    val icon = if (post.isLiked) Icons.Filled.ThumbUp else Icons.Filled.ThumbUpOffAlt
    Row (modifier = modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(text = formatNumber(post.reactions.size), fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.2.sp)
        Spacer(modifier = Modifier.width(5.dp))
        Icon(
            imageVector = icon,
            contentDescription = "Like",
            modifier = Modifier
                .size(35.dp)
                .clickable {
                    likeAction()
                },
        )
    }
}
@OptIn(ExperimentalComposeUiApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentsButton(
    post: FeedPostDto,
    modifier: Modifier,
    likeCommentAction: (CommentDto, FeedPostDto) -> Unit,
    addCommentAction: (AddCommentDto, Long) -> Unit,
    removeCommentAction: (CommentDto, FeedPostDto) -> Unit,
    navController: NavController,
    loginViewModel: LoginViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    val (commentText, setCommentText) = remember { mutableStateOf("") }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Button(
        onClick = { showDialog = true },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = post.comments.size.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black, letterSpacing = 0.2.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.Filled.Comment,
                contentDescription = "Comments",
                tint = Color.Black,
                modifier = Modifier
                    .size(30.dp)
            )
        }
    }
    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = true)) {
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )  {
                Row(
                    modifier = Modifier
                        .padding(2.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showDialog = false }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                // Scrollable list for comments
                LazyColumn(
                    modifier = Modifier
                        .padding(8.dp)
                        .height(400.dp)) {
                    items(post.comments) { comment ->
                        CommentItem(
                            comment = comment,
                            userProfilePicture = post.userDto.profilePictureUrl,
                            currentUserId = loginViewModel.getUserId(),
                            removeCommentAction = removeCommentAction,
                            post = post,
                            navigateToUserProfile = {
                                showDialog = false
                                navController.navigate(MusicScreens.ProfileScreen.routeWithParameters(comment.userId.toString()))
                            }
                        ) {
                            likeCommentAction(comment, post)
                        }
                    }
                }
                // Text field to add a new comment
                TextField(
                    value = commentText,
                    onValueChange = setCommentText,
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = { Text("Add a comment...") },
                    textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        addCommentAction(AddCommentDto(content = commentText), post.id)
                        setCommentText("")
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    })
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentItem(
    comment: CommentDto,
    userProfilePicture: String,
    currentUserId: Long,
    removeCommentAction: (CommentDto, FeedPostDto) -> Unit,
    post: FeedPostDto,
    navigateToUserProfile: () -> Unit,
    likeAction: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val icon = if (comment.isLiked) Icons.Filled.ThumbUp else Icons.Filled.ThumbUpOffAlt
    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = true)
        ) {
            Column(
                modifier = Modifier
                    .width(230.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Remove Comment?", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.4.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            removeCommentAction(comment, post)
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                        contentPadding = PaddingValues(
                            horizontal = 4.dp,
                            vertical = 2.dp
                        )
                    ) {
                        Text(text = "Remove", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                        contentPadding = PaddingValues(
                            horizontal = 4.dp,
                            vertical = 2.dp
                        )
                    ) {
                        Text(text = "Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
            .indication(interactionSource, LocalIndication.current)
            .padding(8.dp)
            .pointerInput(interactionSource) {
                detectTapGestures(onLongPress = {
                    if (comment.userId == currentUserId) {
                        coroutineScope.launch {
                            val press = PressInteraction.Press(Offset.Zero)
                            interactionSource.emit(press)
                            showDialog = true
                            interactionSource.emit(PressInteraction.Release(press))
                        }
                    }
                })
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp)
        ) {
            Row (verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.clickable {
                    navigateToUserProfile()
                }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(userProfilePicture),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.Gray, CircleShape)
                        .clickable {},
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = comment.username,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primaryVariant,
                    fontSize = 14.sp
                )
            }
            Text(
                text = timeFromNow(comment.creationTime),
                style = MaterialTheme.typography.caption,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Row (verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = comment.content,
                fontSize = 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 4.dp, end = 8.dp),
                style = TextStyle(letterSpacing = (-0.4).sp)
            )
            Column (horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly){
                Text(text = formatNumber(comment.reactions.size), fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.1.sp)
                Icon(
                    imageVector = icon,
                    contentDescription = "Like",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            likeAction()
                        },
                )
            }
        }
    }
}

@Composable
fun VideoPlayer(videoUrl: String, onSeen: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    var userPausedManually by remember { mutableStateOf(false) }
    var isVisibleOnScreen by remember { mutableStateOf(false) }
    var markedAsSeen by remember { mutableStateOf(false) }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = isVisibleOnScreen && !userPausedManually
        }
    }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> exoPlayer.playWhenReady = isVisibleOnScreen && !userPausedManually
                Lifecycle.Event.ON_STOP -> exoPlayer.playWhenReady = false
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            exoPlayer.release()
        }
    }

    // Prepare and play the video
    LaunchedEffect(videoUrl) {
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
        exoPlayer.prepare()
    }
    exoPlayer.addListener(object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // This will trigger whenever play/pause state changes, including programmatic changes
            userPausedManually = !isPlaying && exoPlayer.playbackState == Player.STATE_READY
        }
    })

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier
            .height(475.dp)
            .onGloballyPositioned { layoutCoordinates ->
                val playerBounds = layoutCoordinates.boundsInWindow()
                val screenHeight =
                    with(density) { context.resources.displayMetrics.heightPixels.dp.toPx() }
                val screenWidth =
                    with(density) { context.resources.displayMetrics.widthPixels.dp.toPx() }

                val screenBounds = Rect(0f, 0f, screenWidth, screenHeight)

                val intersection = playerBounds.intersect(screenBounds)
                val isMoreThanHalfVisible =
                    intersection.width * intersection.height > (playerBounds.width * playerBounds.height / 2)

                isVisibleOnScreen = isMoreThanHalfVisible
                exoPlayer.playWhenReady = isVisibleOnScreen && !userPausedManually
                if (isVisibleOnScreen && !userPausedManually) {
                    if (!markedAsSeen) {
                        markedAsSeen = true
                        onSeen()
                    }
                }
            }

    )
}

fun Context.screenHeight(): Int {
    return resources.displayMetrics.heightPixels
}

fun logoutAndRedirect(navController: NavController, loginViewModel: LoginViewModel) {
    loginViewModel.logout()
    navController.navigate(MusicScreens.LoginScreen.name) {
        popUpTo(navController.graph.startDestinationId) {
            inclusive = true
        }
    }
}
