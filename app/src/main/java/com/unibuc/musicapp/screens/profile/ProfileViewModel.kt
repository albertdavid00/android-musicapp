package com.unibuc.musicapp.screens.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unibuc.musicapp.dto.AddCommentDto
import com.unibuc.musicapp.dto.CommentDto
import com.unibuc.musicapp.dto.FeedPostDto
import com.unibuc.musicapp.dto.ReactionDto
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import com.unibuc.musicapp.utils.ReactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val api: MusicApi,
                                           private val authRepository: AuthRepository): ViewModel() {

    private val _userInfo = MutableLiveData<UserDto?>()
    val userInfo: LiveData<UserDto?> = _userInfo

    private val _userPosts = MutableLiveData<List<FeedPostDto>>()
    val userPosts: LiveData<List<FeedPostDto>> = _userPosts

    fun loadUserInfo(userId: Long? = null) {
        viewModelScope.launch {
            try {
                if (userId == null) {
                    val fetchedInfo = api.getCurrentUser(authRepository.getToken()!!)
                    Log.d("User", fetchedInfo.toString())
                    _userInfo.postValue(fetchedInfo)
                }
                else {
                    val fetchedInfo = api.getUser(authRepository.getToken()!!, userId)
                    Log.d("User", fetchedInfo.toString())
                    _userInfo.postValue(fetchedInfo)
                }

            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }

    fun loadUserPosts(userId: Long? = null) {
        viewModelScope.launch {
            try {
                if (userId == null) {
                    val fetchedPosts = api.getUserPosts(authRepository.getToken()!!, authRepository.getUserId())
                    Log.d("User", fetchedPosts.toString())
                    _userPosts.postValue(fetchedPosts)
                } else {
                    val fetchedPosts = api.getUserPosts(authRepository.getToken()!!, userId)
                    Log.d("User", fetchedPosts.toString())
                    _userPosts.postValue(fetchedPosts)
                }
            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }

    fun followUnfollowAction(userId: Long) {
        viewModelScope.launch {
            try {
                if (_userInfo.value?.isFollowedByCurrentUser == true) {
                    api.unfollowUser(authRepository.getToken()!!, userId)
                    val currentUserInfo = _userInfo.value!!
                    val updatedUserInfo = currentUserInfo.copy(followers = currentUserInfo.followers - 1, isFollowedByCurrentUser = false)
                    _userInfo.postValue(updatedUserInfo)
                    Log.d("User", "Unfollow user operation")
                } else if (_userInfo.value?.isFollowedByCurrentUser == false){
                    api.followUser(authRepository.getToken()!!, userId)
                    val currentUserInfo = _userInfo.value!!
                    val updatedUserInfo = currentUserInfo.copy(followers = currentUserInfo.followers + 1, isFollowedByCurrentUser = true)
                    _userInfo.postValue(updatedUserInfo)
                    Log.d("User", "Follow user operation")
                }
            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }

    fun likeAction(feedPost: FeedPostDto) {
        viewModelScope.launch {
            try {
                if (!feedPost.isLiked) {
                    val reactionDto = ReactionDto(reactionType = ReactionType.LIKE)
                    val response = api.addReactionToPost(authRepository.getToken()!!, reactionDto, feedPost.id)
                    Log.d("User", "Added reaction to post:$response")
                    val react = feedPost.reactions.find { r -> r.id == response }
                    var newReactionsList = feedPost.reactions
                    if (react == null) {
                        newReactionsList = feedPost.reactions + ReactionDto(id = response, reactionType = ReactionType.LIKE, userId = authRepository.getUserId())
                    }
                    val updatedPosts = _userPosts.value!!.map { post ->
                        if (post.id == feedPost.id) {
                            post.copy(isLiked = !post.isLiked, reactions = newReactionsList)
                        } else {
                            post
                        }
                    }
                    _userPosts.postValue(updatedPosts)
                } else {
                    val reaction = feedPost.reactions.find { reactionDto -> reactionDto.userId == authRepository.getUserId() }
                    if (reaction != null) {
                        api.removeReaction(authRepository.getToken()!!, reaction.id!!)
                    }
                    Log.d("User", "Removed reaction ${reaction!!.id} from post.")
                    val newReactionsList = feedPost.reactions.filter { reactionDto -> reactionDto.userId != authRepository.getUserId()}
                    val updatedPosts = _userPosts.value!!.map { post ->
                        if (post.id == feedPost.id) {
                            post.copy(isLiked = !post.isLiked, reactions = newReactionsList)
                        } else {
                            post
                        }
                    }
                    _userPosts.postValue(updatedPosts)
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
                    Log.d("User", "Removed comment ${comment.id} from post.")
                    val newCommentsList = feedPost.comments.filterNot {
                        it.id == comment.id
                    }
                    val updatedPosts = _userPosts.value!!.map { post ->
                        if (post.id == feedPost.id) {
                            post.copy(comments = newCommentsList)
                        } else {
                            post
                        }
                    }
                    _userPosts.postValue(updatedPosts)
                }
            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }

    fun removePost(post : FeedPostDto) {
        viewModelScope.launch {
            try {
                if (post.userDto.id == authRepository.getUserId()) {
                    val response = api.removePost(authRepository.getToken()!!, post.id)
                    if (response.isSuccessful) {
                        Log.d("User", "Removed post ${post.id}.")
                        val updatedPosts = _userPosts.value!!.filterNot { it.id == post.id }
                        _userPosts.postValue(updatedPosts)
                    } else {
                        Log.d("User", "Failed to delete post, status code: ${response.code()}")
                    }
                }
            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }

    fun addComment(comment: AddCommentDto, postId: Long) {
        viewModelScope.launch {
            try {
                val response = api.addComment(authRepository.getToken()!!, comment, postId)

                val updatedPosts = _userPosts.value!!.map { post ->
                    if (post.id == postId) {
                        val newCommentsList = listOf(response) + post.comments
                        post.copy(comments = newCommentsList)
                    } else {
                        post
                    }
                }
                _userPosts.postValue(updatedPosts)
                Log.d("User", "Add Comment Response: $response")
            } catch (e: HttpException) {
                Log.d("Exc", "Err: " + e.response()?.errorBody()?.string()!!)
            } catch (e: Exception) { // Catching all exceptions
                Log.d("Exc", "Err: " + e.message.toString())
            }
        }
    }

    fun likeCommentAction(comment: CommentDto, feedPost: FeedPostDto) {
        viewModelScope.launch {
            try {
                if (!comment.isLiked) {
                    val reactionDto = ReactionDto(reactionType = ReactionType.LIKE)
                    val response = api.addReactionToComment(authRepository.getToken()!!, reactionDto, comment.id)
                    Log.d("User", "Added reaction to comment:$response")
                    val reactionExists = comment.reactions.any { it.id == response }
                    val newReactionsList = if (!reactionExists) {
                        comment.reactions + ReactionDto(id = response, reactionType = ReactionType.LIKE, userId = authRepository.getUserId())
                    } else {
                        comment.reactions
                    }
                    val updatedPosts = _userPosts.value!!.map { post ->
                        if (post.id == feedPost.id) {
                            post.copy(comments = post.comments.map { comm ->
                                if (comm.id == comment.id) {
                                    comm.copy(reactions = newReactionsList, isLiked = true)
                                } else comm
                            })
                        } else post
                    }
                    _userPosts.postValue(updatedPosts)
                } else {
                    val reaction = comment.reactions.find { reactionDto -> reactionDto.userId == authRepository.getUserId() }
                    if (reaction != null) {
                        api.removeReaction(authRepository.getToken()!!, reaction.id!!)
                    }
                    Log.d("User", "Removed reaction ${reaction!!.id} from comment.")
                    val newReactionsList = comment.reactions.filterNot {
                        it.reactionType == ReactionType.LIKE && it.userId == authRepository.getUserId()
                    }
                    val updatedPosts = _userPosts.value!!.map { post ->
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
                    _userPosts.postValue(updatedPosts)
                }
            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }
}