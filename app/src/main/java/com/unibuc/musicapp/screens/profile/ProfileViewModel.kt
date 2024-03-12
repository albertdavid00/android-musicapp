package com.unibuc.musicapp.screens.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val api: MusicApi,
                                           private val authRepository: AuthRepository): ViewModel() {

    private val _userInfo = MutableLiveData<UserDto?>()
    val userInfo: LiveData<UserDto?> = _userInfo

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

            }
            catch (e: Exception) {
                Log.d("User", e.message.toString())
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
}