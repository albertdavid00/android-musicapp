package com.unibuc.musicapp.screens.users

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import com.unibuc.musicapp.utils.FollowParam
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class UsersViewModel  @Inject constructor(private val api: MusicApi,
                                          private val authRepository: AuthRepository): ViewModel() {
    private val _users = MutableLiveData<List<UserDto>>()
    val users: LiveData<List<UserDto>> = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadUsers(userId: Long, followParam: String) {
        viewModelScope.launch {
            try {
                if (FollowParam.FOLLOWERS.name == followParam) {
                    val fetchedUsers = api.getUserFollowers(authRepository.getToken()!!, userId)
                    Log.d("Users", fetchedUsers.toString())
                    _users.postValue(fetchedUsers)
                } else if (FollowParam.FOLLOWING.name == followParam) {
                    val fetchedUsers = api.getUserFollowing(authRepository.getToken()!!, userId)
                    Log.d("Users", fetchedUsers.toString())
                    _users.postValue(fetchedUsers)
                }
            }
            catch (e: Exception) {
                Log.d("Users", e.message.toString())
            }
        }
    }

    fun searchUsers(text: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val filteredUsers = api.filterUsers(authRepository.getToken()!!, text)
                Log.d("Users", filteredUsers.toString())
                _users.postValue(filteredUsers)
            }
            catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            }
            catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
            finally {
                _isLoading.value = false
            }
        }
    }
}