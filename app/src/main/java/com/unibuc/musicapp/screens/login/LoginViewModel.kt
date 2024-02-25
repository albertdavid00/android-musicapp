package com.unibuc.musicapp.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unibuc.musicapp.dto.LoginDto
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val api: MusicApi,
                                         private val authRepository: AuthRepository): ViewModel() {

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn.value!!

    }

    fun logout() {
        authRepository.deleteToken()
    }

    fun login(email: String, password: String, home: () -> Unit) {
        viewModelScope.launch {
            try{
                Log.d("Form", "calling api login")
                val tokenResponse = api.login(LoginDto(email, password))
                authRepository.updateToken(tokenResponse)
                Log.d("Form", tokenResponse.toString())
                home()
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }

    }
}