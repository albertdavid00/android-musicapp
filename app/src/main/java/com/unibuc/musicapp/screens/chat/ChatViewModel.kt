package com.unibuc.musicapp.screens.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.collection.GroupChannelContext
import com.sendbird.android.collection.MessageCollection
import com.sendbird.android.collection.MessageCollectionInitPolicy
import com.sendbird.android.collection.MessageContext
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.MessageCollectionHandler
import com.sendbird.android.handler.MessageCollectionInitHandler
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.android.params.GroupChannelCreateParams
import com.sendbird.android.params.MessageCollectionCreateParams
import com.sendbird.android.params.MessageListParams
import com.sendbird.android.user.User
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.dto.UserProfileDto
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val api: MusicApi,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _userConnected = MutableStateFlow(false)
    val userConnected: StateFlow<Boolean> = _userConnected

    private val _messages = MutableStateFlow<List<BaseMessage>>(emptyList())
    val messages: StateFlow<List<BaseMessage>> = _messages

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _currentChannel = MutableStateFlow<GroupChannel?>(null)
    val currentChannel: StateFlow<GroupChannel?> = _currentChannel

    private val _userInfo = MutableLiveData<UserProfileDto?>()
    val userInfo: LiveData<UserProfileDto?> = _userInfo

    private lateinit var messageCollection: MessageCollection


    fun connectUser(userId: Long) {
        _loading.value = true
        SendbirdChat.connect(userId.toString()) { user, e ->
            if (user != null) {
                if (e != null) {
                    _errorMessage.value = e.message
                    Log.e("ChatScreen", "Error: ${e.message}")

                    // and can be notified through ConnectionHandler.onReconnectSucceeded().
                } else {
                    // Proceed in online mode.
                    _user.value = user
                    _userConnected.value = true
                    Log.d("ChatScreen", "User connected: $user")
                }
            } else {
                _errorMessage.value = e?.message ?: "Unknown error"
            }
            _loading.value = false
        }
    }

    fun createOrGetChannel(userId1: Long, userId2: Long) {
        _loading.value = true
        val sortedUserIds = listOf(userId1.toString(), userId2.toString()).sorted()
        val params = GroupChannelCreateParams().apply {
            userIds = sortedUserIds
            isDistinct = true // Ensure this is a distinct channel between the two users
        }
        Log.d("ChatScreen", "Creating group channel")
        GroupChannel.createChannel(params) { groupChannel, e ->
            if (e != null) {
                _errorMessage.value = e.message
                Log.e("ChatScreen", "Error: ${e.message}")
                _loading.value = false
                return@createChannel
            }
            _currentChannel.value = groupChannel
            _loading.value = false
            if (groupChannel != null) {
                createMessageCollection(groupChannel)
            }
        }
    }
    private fun fetchMessages(channel: GroupChannel) {
        val params = MessageListParams()
        Log.d("ChatScreen", "Fetching messages for channel $channel")
        channel.getMessagesByTimestamp(Long.MAX_VALUE, params) { messages, e ->
            if (e != null) {
                _errorMessage.value = e.message
                Log.e("ChatScreen", "Error: ${e.message}")
                return@getMessagesByTimestamp
            }
            if (messages != null) {
                Log.d("ChatScreen", "Messages fetched: $messages")
                _messages.value = messages
            }
        }
    }

    fun sendMessage(channel: GroupChannel, messageText: String) {
        channel.sendUserMessage(messageText) { message, e ->
            if (e != null) {
                _errorMessage.value = e.message
                Log.e("ChatScreen", "Error: ${e.message}")
                return@sendUserMessage
            }
        }
    }

    fun getUserInfo(userId: Long) {
        viewModelScope.launch {
            try {
                val fetchedInfo = api.getUser(authRepository.getToken()!!, userId)
                Log.d("ChatScreen", fetchedInfo.toString())
                _userInfo.postValue(fetchedInfo)

            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }
    private fun createMessageCollection(groupChannel: GroupChannel) {
        val messageListParams = MessageListParams().apply {
            reverse = false
        }
        Log.d("ChatScreen", "Fetching messages for channel ${groupChannel.url}")

        val collection = SendbirdChat.createMessageCollection(
            MessageCollectionCreateParams(groupChannel, messageListParams).apply {
                startingPoint = System.currentTimeMillis() // Adjust starting point as needed
                messageCollectionHandler = object : MessageCollectionHandler {
                    override fun onMessagesAdded(context: MessageContext, channel: GroupChannel, messages: List<BaseMessage>) {
                        when (context.messagesSendingStatus) {
                            SendingStatus.SUCCEEDED -> {
                                _messages.value = _messages.value + messages
                            }
                            SendingStatus.PENDING -> {
                                // Handle pending messages
                                _messages.value = _messages.value + messages
                            }
                            SendingStatus.FAILED -> {
                            }
                            SendingStatus.CANCELED -> {
                            }
                            SendingStatus.NONE -> {
                            }
                            SendingStatus.SCHEDULED -> {
                            }
                        }
                    }

                    override fun onMessagesUpdated(context: MessageContext, channel: GroupChannel, messages: List<BaseMessage>) {
                        when (context.messagesSendingStatus) {
                            SendingStatus.SUCCEEDED -> {
                                _messages.value = _messages.value.map { existingMessage ->
                                    messages.find { it.messageId == existingMessage.messageId } ?: existingMessage
                                }
                            }
                            SendingStatus.PENDING -> {
                                // Handle pending to failed transition
                                _messages.value = _messages.value.map { existingMessage ->
                                    messages.find { it.messageId == existingMessage.messageId } ?: existingMessage
                                }
                            }
                            SendingStatus.FAILED -> {
                                // Handle failed messages
                                _messages.value = _messages.value.map { existingMessage ->
                                    messages.find { it.messageId == existingMessage.messageId } ?: existingMessage
                                }
                            }
                            SendingStatus.CANCELED -> {
                                // Handle canceled messages
                                _messages.value = _messages.value.filterNot { it.messageId in messages.map { it.messageId } }
                            }

                            else -> {}
                        }
                    }

                    override fun onMessagesDeleted(context: MessageContext, channel: GroupChannel, messages: List<BaseMessage>) {
                        _messages.value = _messages.value.filterNot { it.messageId in messages.map { it.messageId } }
                    }

                    override fun onChannelUpdated(context: GroupChannelContext, channel: GroupChannel) {
                        _currentChannel.value = channel
                    }

                    override fun onChannelDeleted(context: GroupChannelContext, channelUrl: String) {
                        _currentChannel.value = null
                    }

                    override fun onHugeGapDetected() {
                        _messages.value = emptyList()
                        messageCollection.dispose()
                        createMessageCollection(groupChannel)
                    }
                }
            }
        )

        this.messageCollection = collection

        initializeMessageCollection()
    }

    private fun initializeMessageCollection() {
        messageCollection.initialize(
            MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API,
            object : MessageCollectionInitHandler {
                override fun onCacheResult(cachedList: List<BaseMessage>?, e: SendbirdException?) {
                    if (cachedList != null) {
                        _messages.value = cachedList
                    }
                }

                override fun onApiResult(apiResultList: List<BaseMessage>?, e: SendbirdException?) {
                    if (apiResultList != null) {
                        _messages.value = apiResultList
                    }
                }
            }
        )
    }
    override fun onCleared() {
        super.onCleared()
        if (::messageCollection.isInitialized) {
            messageCollection.dispose()
        }
    }

    companion object {
        private const val STARTING_POINT: Long = Long.MAX_VALUE
    }
}