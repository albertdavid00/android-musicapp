package com.unibuc.musicapp.screens.messages

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
class MessagesViewModel @Inject constructor(
    private val api: MusicApi,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _users = MutableLiveData<List<UserDto>>()
    val users: LiveData<List<UserDto>> = _users

    fun loadMatches() {
        viewModelScope.launch {
            try {
                val fetchedData = api.getMatchedUsers(authRepository.getToken()!!)
                Log.d("Messages", fetchedData.toString())
                _users.postValue(fetchedData)
            }
            catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            }
            catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }
}