package com.unibuc.musicapp.screens.post

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.unibuc.musicapp.dto.PostDto
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.login.LoginViewModel
import com.unibuc.musicapp.utils.Constants
import com.unibuc.musicapp.utils.Visibility
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: CreatePostViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    var description by remember { mutableStateOf(TextFieldValue()) }
    var selectedVisibility by remember { mutableStateOf(Visibility.PUBLIC) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val valid = derivedStateOf{
        selectedVisibility.name.isNotEmpty() && videoUri != null
    }.value
    val isUploading by viewModel.isUploading.collectAsState();
    Surface(modifier = Modifier.fillMaxSize()) {
        if (isUploading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                UploadingAnimation()
            }
        } else {
            Column {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Create Post",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.padding(end = 8.dp),
                        fontStyle = FontStyle.Italic
                    )
                    Button(
                        onClick = {
                                  viewModel.uploadPostToAzureAndDB(prepareVideoFile(context, videoUri!!), description.text.trim(), selectedVisibility, context) {
                                      navController.navigate(MusicScreens.FeedScreen.name)
                                  }
                        },
                        enabled = valid,
                    ) {
                        Text(text = "Upload")
                    }
                }
                RadioGroup(selectedVisibility) { visibility ->
                    selectedVisibility = visibility
                }
                TextField(
                    value = description,
                    onValueChange = {
                        if (it.text.length <= 200) {
                            description = it
                        }
                    },
                    label = { androidx.compose.material.Text("Description") },
                    textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        cursorColor = Color.Black,
                        leadingIconColor = MaterialTheme.colors.onSurface,
                        focusedIndicatorColor = MaterialTheme.colors.primary,
                        unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                        focusedLabelColor = MaterialTheme.colors.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(max = 100.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.None),
                    maxLines = 3
                )
                VideoPickerAndRecorder(videoUri) { newUri ->
                    if (newUri != null)
                        videoUri = newUri
                }
            }

        }
    }
}

fun prepareVideoFilePart(context: Context, videoUri: Uri, partName: String): MultipartBody.Part {
    // Get the video's content URI and open an input stream
    val inputStream = context.contentResolver.openInputStream(videoUri)
    val file = File(context.cacheDir, "temp_video") // Create a temporary file for the video
    FileOutputStream(file).use { outputStream ->
        inputStream?.copyTo(outputStream)
    }

    // Create RequestBody instance from file, adjust the MIME type to "video/mp4" or whatever your video format is
    val requestFile = file
        .asRequestBody("video/mp4".toMediaTypeOrNull())
    inputStream?.close()
    // MultipartBody.Part is used to send also the actual file name
    return MultipartBody.Part.createFormData(partName, file.name, requestFile)
}

fun prepareVideoFile(context: Context, videoUri: Uri): File {
    // Get the video's content URI and open an input stream
    val inputStream = context.contentResolver.openInputStream(videoUri)
    val file = File(context.cacheDir, "temp_video") // Create a temporary file for the video

    FileOutputStream(file).use { outputStream ->
        inputStream?.copyTo(outputStream)
    }

    inputStream?.close()
    return file
}

@Composable
fun VideoPickerAndRecorder(videoUri: Uri?, onVideoUriChange: (Uri?) -> Unit) {
    val context = LocalContext.current

    // Launcher for selecting video from the gallery
    val pickVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        onVideoUriChange(uri)
    }
    // Preparing a Uri for recording
    val videoCaptureUri = remember { mutableStateOf<Uri?>(null) }

    var hasCameraPermission by remember { mutableStateOf(false) }

    // Launcher for recording video
    val recordVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success: Boolean ->
        if (success) {
            // Use the pre-prepared Uri if recording was successful
            onVideoUriChange(videoCaptureUri.value)
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            hasCameraPermission = isGranted

            if (isGranted) {
                // Permission was just granted; prepare the URI and start recording
                videoCaptureUri.value = prepareVideoUri(context)
                videoCaptureUri.value?.let { uri ->
                    recordVideoLauncher.launch(uri)
                }
            }
        }
    )

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = { pickVideoLauncher.launch("video/*") }) {
            Text("Select Video")
        }
        Button(onClick = {
            if (hasCameraPermission) {
                // Permission is already granted; you can start recording video
                videoCaptureUri.value = prepareVideoUri(context)
                videoCaptureUri.value?.let { uri ->
                    recordVideoLauncher.launch(uri)
                }
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

        }) {
            Text("Record Video")
        }
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 70.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        if (videoUri == null) {
            Text(text = "No video selected", fontStyle = FontStyle.Italic, color = Color.Gray)
        } else {
            VideoPlayer(
                uri = videoUri,
            )
        }
    }
}


@Composable
fun VideoPlayer(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var currentUri by remember { mutableStateOf<Uri?>(null) }
    var isPlaying by remember { mutableStateOf(false) } // State to manage play/pause icon
    var showControls by remember { mutableStateOf(true) } // State to manage controls visibility
    var videoViewInstance: VideoView? by remember { mutableStateOf(null) } // Keep a reference to VideoView

    // Show controls when the video is interacted with, then hide after 1 second
    LaunchedEffect(showControls, isPlaying) {
        if (showControls) {
            delay(1000) // Delay for 1 second
            showControls = false // Hide controls after delay
        }
    }

    Box {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        start()
                        isPlaying = true
                    }
                    setOnCompletionListener {
                        isPlaying = false
                    }
                    videoViewInstance = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .border(BorderStroke(3.dp, Color.Black))
                .clickable {
                    videoViewInstance?.let {
                        if (isPlaying) {
                            it.pause()
                            isPlaying = false
                        } else {
                            it.start()
                            isPlaying = true
                        }
                    }
                    showControls = true
                },
            update = { view ->
                if (currentUri != uri) {
                    // Update the video URI only if it has changed
                    view.setVideoURI(uri)
                    currentUri = uri
                }
            }
        )
        if (showControls) {
            // Play/Pause Button
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .alpha(0.4f)
                    .clickable {
                        videoViewInstance?.let {
                            if (isPlaying) {
                                it.pause()
                                isPlaying = false
                            } else {
                                it.start()
                                isPlaying = true
                            }
                        }
                        showControls = true
                    },
                tint = Color.White
            )

        }
    }
}

fun prepareVideoUri(context: Context): Uri? {
    val videoFile: File = createVideoFile(context) ?: return null
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        videoFile
    )
}

fun createVideoFile(context: Context): File? {
    val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
    return File.createTempFile(
        "VIDEO_${timestamp}_",
        ".mp4",
        storageDir
    )
}

@Composable
fun RadioGroup(selectedVisibility: Visibility, onVisibilitySelected: (Visibility) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Public", fontSize = 14.sp, color= Color.Gray)
            RadioButton(
                modifier = Modifier.size(40.dp),
                selected = selectedVisibility == Visibility.PUBLIC,
                onClick = { onVisibilitySelected(Visibility.PUBLIC) },

                )

        }

        Column(horizontalAlignment = Alignment.CenterHorizontally){
            Text(text ="Followers", fontSize = 14.sp, color= Color.Gray)
            RadioButton(
                modifier = Modifier.size(40.dp),
                selected = selectedVisibility == Visibility.FOLLOWERS,
                onClick = { onVisibilitySelected(Visibility.FOLLOWERS) },
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Private", fontSize = 14.sp, color= Color.Gray)
            RadioButton(
                modifier = Modifier.size(40.dp),
                selected = selectedVisibility == Visibility.PRIVATE,
                onClick = { onVisibilitySelected(Visibility.PRIVATE) },
            )

        }
    }
}

@Composable
fun UploadingAnimation() {
    var dotsCount by remember { mutableStateOf(1) }

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(500) // Adjust the speed of dot animation here
            dotsCount = if (dotsCount < 3) dotsCount + 1 else 1
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Uploading",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = buildString { repeat(dotsCount) { append(".") } },
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
        }
    }
}