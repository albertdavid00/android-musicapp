package com.unibuc.musicapp.screens.feed

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.unibuc.musicapp.dto.AddCommentDto
import com.unibuc.musicapp.dto.CommentDto
import com.unibuc.musicapp.dto.FeedPostDto
import com.unibuc.musicapp.dto.MessageContentDto
import com.unibuc.musicapp.dto.ReactionDto
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import com.unibuc.musicapp.utils.Constants
import com.unibuc.musicapp.utils.ReactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val api: MusicApi,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _feedData = MutableLiveData<List<FeedPostDto>>()
    val feedData: LiveData<List<FeedPostDto>> = _feedData

    private val _fetchedData = MutableStateFlow(false)
    val fetchedData: StateFlow<Boolean> = _fetchedData

    fun loadFeedData() {
        viewModelScope.launch {
            try {
                if (_feedData.value == null || _feedData.value!!.isEmpty()) {
                    Log.d("Feed", "Calling Feed api")
                    val fetchedData = api.getFeedPosts(authRepository.getToken()!!)
                    Log.d("Feed", fetchedData.toString())
                    _feedData.postValue(fetchedData)
                    _fetchedData.value = true
                }
            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun markPostAsSeen(postId: Long) {
        viewModelScope.launch {
            try {

                val client = OkHttpClient()
                val messageContent = MessageContentDto(postId = postId, userId = authRepository.getUserId())
                val gson = Gson()
                val jsonString = gson.toJson(messageContent)
                val encodedString = Base64.getEncoder().encodeToString(jsonString.toByteArray())
                val queueMessage = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <QueueMessage>
                        <MessageText>$encodedString</MessageText>
                    </QueueMessage>
                """.trimIndent()

                val mediaType = "application/xml; charset=utf-8".toMediaType()
                val requestBody = queueMessage.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(Constants.AZURE_QUEUE_SAS_URL)
                    .post(requestBody)
                    .header("Content-Type", "application.xml")
                    .build()

                withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        // Handle response
                        response.body?.string()?.let { Log.d("AZURE_QUEUE", it) }
                    }
                }
                val updatedFeedData = _feedData.value!!.map { post ->
                    if (post.id == postId) {
                        post.copy(seen = true)
                    } else {
                        post
                    }
                }
                _feedData.postValue(updatedFeedData)

            } catch (e: HttpException) {
                Log.d("Feed", "Err: " + e.response()?.errorBody()?.string()!!)
            } catch (e: Exception) { // Catching all exceptions
                Log.d("Feed", "Err: " + e.message.toString())
            }
        }
    }

    fun addComment(comment: AddCommentDto, postId: Long) {
        viewModelScope.launch {
            try {
                val response = api.addComment(authRepository.getToken()!!, comment, postId)

                val updatedFeedData = _feedData.value!!.map { post ->
                    if (post.id == postId) {
                        val newCommentsList = listOf(response) + post.comments
                        post.copy(comments = newCommentsList)
                    } else {
                        post
                    }
                }
                _feedData.postValue(updatedFeedData)
                Log.d("Feed", "Add Comment Response: $response")
            } catch (e: HttpException) {
                Log.d("Feed", "Err: " + e.response()?.errorBody()?.string()!!)
            } catch (e: Exception) { // Catching all exceptions
                Log.d("Feed", "Err: " + e.message.toString())
            }
        }
    }

    fun likeAction(feedPost: FeedPostDto) {
        viewModelScope.launch {
            try {
                if (!feedPost.isLiked) {
                    val reactionDto = ReactionDto(reactionType = ReactionType.LIKE)
                    val response = api.addReactionToPost(authRepository.getToken()!!, reactionDto, feedPost.id)
                    Log.d("Feed", "Added reaction to post:$response")
                    val react = feedPost.reactions.find { r -> r.id == response }
                    var newReactionsList = feedPost.reactions
                    if (react == null) {
                        newReactionsList = feedPost.reactions + ReactionDto(id = response, reactionType = ReactionType.LIKE, userId = authRepository.getUserId())
                    }
                    val updatedFeedData = _feedData.value!!.map { post ->
                        if (post.id == feedPost.id) {
                            post.copy(isLiked = !post.isLiked, reactions = newReactionsList)
                        } else {
                            post
                        }
                    }
                    _feedData.postValue(updatedFeedData)
                } else {
                    val reaction = feedPost.reactions.find { reactionDto -> reactionDto.userId == authRepository.getUserId() }
                    if (reaction != null) {
                        api.removeReaction(authRepository.getToken()!!, reaction.id!!)
                    }
                    Log.d("Feed", "Removed reaction ${reaction!!.id} from post.")
                    val newReactionsList = feedPost.reactions.filter { reactionDto -> reactionDto.userId != authRepository.getUserId()}
                    val updatedFeedData = _feedData.value!!.map { post ->
                        if (post.id == feedPost.id) {
                            post.copy(isLiked = !post.isLiked, reactions = newReactionsList)
                        } else {
                            post
                        }
                    }
                    _feedData.postValue(updatedFeedData)
                }
            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }

    fun likeCommentAction(comment: CommentDto, feedPost: FeedPostDto) {
        viewModelScope.launch {
            try {
                if (!comment.isLiked) {
                    val reactionDto = ReactionDto(reactionType = ReactionType.LIKE)
                    val response = api.addReactionToComment(authRepository.getToken()!!, reactionDto, comment.id)
                    Log.d("Feed", "Added reaction to comment:$response")
                    val reactionExists = comment.reactions.any { it.id == response }
                    val newReactionsList = if (!reactionExists) {
                        comment.reactions + ReactionDto(id = response, reactionType = ReactionType.LIKE, userId = authRepository.getUserId())
                    } else {
                        comment.reactions
                    }
                    val updatedFeedData = _feedData.value!!.map { post ->
                        if (post.id == feedPost.id) {
                            post.copy(comments = post.comments.map { comm ->
                                if (comm.id == comment.id) {
                                    comm.copy(reactions = newReactionsList, isLiked = true)
                                } else comm
                            })
                        } else post
                    }
                    _feedData.postValue(updatedFeedData)
                } else {
                    val reaction = comment.reactions.find { reactionDto -> reactionDto.userId == authRepository.getUserId() }
                    if (reaction != null) {
                        api.removeReaction(authRepository.getToken()!!, reaction.id!!)
                    }
                    Log.d("Feed", "Removed reaction ${reaction!!.id} from comment.")
                    val newReactionsList = comment.reactions.filterNot {
                        it.reactionType == ReactionType.LIKE && it.userId == authRepository.getUserId()
                    }
                    val updatedFeedData = _feedData.value!!.map { post ->
                        if (post.id == feedPost.id) {
                            post.copy(comments = post.comments.map { comm ->
                                if (comm.id == comment.id) {
                                    comm.copy(reactions = newReactionsList, isLiked = false)
                                } else {
                                    comm
                                }
                            })
                        } else {
                            post
                        }
                    }
                    _feedData.postValue(updatedFeedData)
                }
            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }

    fun removeComment(comment: CommentDto, feedPost: FeedPostDto) {
        viewModelScope.launch {
            try {
                if (comment.userId == authRepository.getUserId()) {
                    api.removeComment(authRepository.getToken()!!, comment.id)
                    Log.d("Feed", "Removed comment ${comment.id} from post.")
                    val newCommentsList = feedPost.comments.filterNot {
                        it.id == comment.id
                    }
                    val updatedFeedData = _feedData.value!!.map { post ->
                        if (post.id == feedPost.id) {
                            post.copy(comments = newCommentsList)
                        } else {
                            post
                        }
                    }
                    _feedData.postValue(updatedFeedData)
                }
            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }

}