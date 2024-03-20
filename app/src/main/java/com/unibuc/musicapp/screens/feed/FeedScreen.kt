package com.unibuc.musicapp.screens.feed

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.unibuc.musicapp.dto.GetPostDto
import com.unibuc.musicapp.dto.PostDto
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.login.LoginViewModel
import com.unibuc.musicapp.utils.Visibility

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    var markedAsSeen by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "FEED")
            Button(
                onClick = { logoutAndRedirect(navController, loginViewModel) },
                modifier = Modifier
                    .padding(3.dp)
                    .fillMaxWidth(),
            ) {
                Text(text= "Log Out", modifier = Modifier.padding(5.dp))
            }
            PostItem(GetPostDto(7003L,
                "My first post",
                Visibility.PUBLIC,
                "https://unibucmusicappstorage.blob.core.windows.net/musicblobdata/6001_1710753270055_temp_video"
            )) {
                if (!markedAsSeen) {
                    markedAsSeen = true
                    Log.d("Feed", "Marking post as seen")
                    viewModel.markPostAsSeen(7003L)

                } else {
                    Log.d("Feed", "Already marked as seen")
                }
            }

        }



    }

}

@Composable
fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> exoPlayer.playWhenReady = true
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

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier
            .height(200.dp)
    )
}

@Composable
fun PostItem(post: GetPostDto, onSeen: (Long) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    Column(modifier = Modifier
        .padding(8.dp)
        .onGloballyPositioned { layoutCoordinates ->
            val isVisible = layoutCoordinates.positionInRoot().y in 0f..context
                .screenHeight()
                .toFloat()
            if (isVisible) {
                onSeen(post.id)
            }
        }
    ) {
        Text(text = post.description)
        VideoPlayer(videoUrl = post.videoUrl)
    }
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
