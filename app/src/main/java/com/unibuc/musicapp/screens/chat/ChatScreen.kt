package com.unibuc.musicapp.screens.chat

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.sendbird.android.message.BaseMessage
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.login.LoginViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    navController: NavController,
    userId: Long,
    viewModel: ChatViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    if (!loginViewModel.isLoggedIn()) {
        navController.navigate(MusicScreens.LoginScreen.name)
    } else {
        val userConnected by viewModel.userConnected.collectAsState()
        val currentChannel by viewModel.currentChannel.collectAsState()
        val messages by viewModel.messages.collectAsState()
        val errorMessage by viewModel.errorMessage.collectAsState()
        val loading by viewModel.loading.collectAsState()
        var messageInput by remember { mutableStateOf(TextFieldValue()) }
        val userInfo by viewModel.userInfo.observeAsState()
        val listState = rememberLazyListState()
        LaunchedEffect(Unit) {
            viewModel.getUserInfo(userId)
            viewModel.connectUser(loginViewModel.getUserId())
        }

        LaunchedEffect(userConnected) {
            if (userConnected) {
                viewModel.createOrGetChannel(loginViewModel.getUserId(), userId)
            }
        }

        LaunchedEffect(messages.size) {
            if(messages.size - 1 < 0) {
                listState.animateScrollToItem(0)
            }
            else {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .imePadding()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (loading) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 200.dp)) {
                            CircularProgressIndicator()
                        }
                    } else if (errorMessage != null) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(text = errorMessage!!, color = MaterialTheme.colors.error)
                        }
                    } else {
                        val groupedMessages = groupMessagesByDate(messages)

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .padding(bottom = 55.dp, top = 5.dp)
                                .weight(1f)) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if(userInfo != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically){
                                            Image(
                                                painter = rememberAsyncImagePainter(userInfo!!.profilePictureUrl),
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .border(1.5.dp, Color.Gray, CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(text = userInfo!!.firstName + " " + userInfo!!.lastName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                        }

                                    }
                                }
                            }
                            groupedMessages.forEach { (date, messagesForDate) ->
                                item {
                                    DateHeader(date = date)
                                }
                                items(messagesForDate) { message ->
                                    message.sender?.let {
                                        ChatMessageItem(message = message,
                                            isFromCurrentUser = message.sender!!.userId == loginViewModel.getUserId()
                                                .toString()
                                        )
                                    }
                                    if (message.sender == null) { // in case of message from admin (sendbird interface)
                                        Text(text = message.message)
                                    }
                                }
                            }
                            }
                        }
                    }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center) {
                    TextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        modifier = Modifier
                            .width(260.dp)
                            .padding(2.dp),
                        placeholder = { androidx.compose.material3.Text("Write a message...") },
                        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
                    )
                    Button(onClick = {
                        currentChannel?.let {
                            viewModel.sendMessage(it, messageInput.text)
                            messageInput = TextFieldValue()
                        }
                    }) {
                        Text("Send")
                    }
                }
            }
        }
    }
}
@Composable
fun ChatMessageItem(message: BaseMessage, isFromCurrentUser: Boolean) {
    val backgroundColor = if (isFromCurrentUser) MaterialTheme.colors.primary else MaterialTheme.colors.secondary
    val padding =  PaddingValues(start = 8.dp, end = 8.dp)

    Row(
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .padding(8.dp)
                .then(Modifier.padding(padding))
        ) {
            Text(
                text = message.message,
                style = TextStyle(color = Color.White, fontSize = 16.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.createdAt),
                style = TextStyle(color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            )
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date,
            style = TextStyle(
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

fun groupMessagesByDate(messages: List<BaseMessage>): Map<String, List<BaseMessage>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = sdf.format(Date())
    val yesterday = sdf.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))

    val groupedMessages = messages.groupBy {
        when (val messageDate = sdf.format(Date(it.createdAt))) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> messageDate
        }
    }

    return groupedMessages
}