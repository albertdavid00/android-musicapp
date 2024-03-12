package com.unibuc.musicapp.screens.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unibuc.musicapp.dto.RegisterDto
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class RegisterScreenViewModel @Inject constructor(
    private val api: MusicApi,
): ViewModel()  {

    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering

    fun register(registerDto: RegisterDto, toLogin: () -> Unit) {
        viewModelScope.launch {
            try{
                _isRegistering.value = true
                Log.d("Register", "Register " + registerDto.email)
                val email = registerDto.email.toRequestBody("text/plain".toMediaTypeOrNull())
                val password = registerDto.password.toRequestBody("text/plain".toMediaTypeOrNull())
                val lastName = registerDto.lastName.toRequestBody("text/plain".toMediaTypeOrNull())
                val firstName = registerDto.firstName.toRequestBody("text/plain".toMediaTypeOrNull())
                val age = registerDto.age.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val imagePart = registerDto.imagePart
                Log.d("Register", imagePart.headers.toString())
                api.register(email, password, lastName, firstName, age, imagePart)
                toLogin()
            } catch (e: HttpException) {
                Log.d("Register", e.response()?.errorBody()?.string()!!)
            } catch (e: Exception) {
                Log.d("Register", e.message.toString())
            } finally {
                _isRegistering.value = false
            }
        }
    }
}