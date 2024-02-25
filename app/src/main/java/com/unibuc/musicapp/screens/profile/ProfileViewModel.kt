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
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val api: MusicApi,
                                           private val authRepository: AuthRepository): ViewModel() {

    private val _userInfo = MutableLiveData<UserDto?>()
    val userInfo: LiveData<UserDto?> = _userInfo

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val fetchedInfo = api.getCurrentUser(authRepository.getToken()!!)
                Log.d("User", fetchedInfo.toString())
                _userInfo.postValue(fetchedInfo)
            }
            catch (e: Exception) {
                Log.d("User", e.message.toString())
            }
        }
    }
}