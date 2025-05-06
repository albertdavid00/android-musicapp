package com.unibuc.musicapp.screens.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.sendbird.android.SendbirdChat
import com.unibuc.musicapp.data.ErrorResponse
import com.unibuc.musicapp.dto.LoginDto
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val api: MusicApi,
                                         private val authRepository: AuthRepository): ViewModel() {
    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _showErrorDialog = MutableStateFlow(false)
    val showErrorDialog: StateFlow<Boolean> = _showErrorDialog

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn.value!!

    }

    fun logout() {
        authRepository.deleteToken()
    }
    fun getUserId(): Long {
        return authRepository.getUserId()
    }
    fun hasUserRole(): Boolean {
        return authRepository.hasUserRole()
    }
    fun hasManagerRole(): Boolean {
        return authRepository.hasManagerRole()
    }
    fun login(email: String, password: String, home: () -> Unit) {
        viewModelScope.launch {
            try{
                _isAuthenticating.value = true
                Log.d("Form", "calling api login")
                val tokenResponse = api.login(LoginDto(email, password))
                authRepository.updateToken(tokenResponse)
                Log.d("Form", tokenResponse.toString())
                connectUserToSendBird(getUserId())
                home()

            } catch (e: HttpException) {
                val statusCode = e.code()
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody)
                val specificErrorMessage = errorMessage ?: "Unknown error"
                _errorMessage.value = "Login Failed:\n$specificErrorMessage"
                _showErrorDialog.value = true
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
                val specificErrorMessage = e.message ?: "Unknown error"
                _errorMessage.value = "Login Failed: $specificErrorMessage"
                _showErrorDialog.value = true
            }
            finally {
                _isAuthenticating.value = false
            }
        }
    }

    private fun parseErrorMessage(json: String?): String? {
        return try {
            json?.let {
                val gson = Gson()
                val errorResponse = gson.fromJson(it, ErrorResponse::class.java)
                errorResponse.message
            }
        } catch (e: Exception) {
            null
        }
    }
    fun dismissErrorDialog() {
        _showErrorDialog.value = false
    }

    private fun connectUserToSendBird(userId: Long) {
        _isAuthenticating.value = true
        SendbirdChat.connect(userId.toString()) { user, e ->
            if (user != null) {
                if (e != null) {
                    _errorMessage.value = e.message.toString()
                    Log.e("Login", "Error: ${e.message}")

                    // and can be notified through ConnectionHandler.onReconnectSucceeded().
                } else {
                    Log.d("Login", "User connected: $user")
                }
            } else {
                _errorMessage.value = e?.message ?: "Unknown error"
            }
            _isAuthenticating.value = false
        }
    }
}