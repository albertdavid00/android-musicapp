package com.unibuc.musicapp.screens.contact

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
class ContactsViewModel @Inject constructor(
    private val api: MusicApi,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _users = MutableLiveData<List<UserDto>>()
    val users: LiveData<List<UserDto>> = _users

    fun loadContacts() {
        viewModelScope.launch {
            try {
                val fetchedData = api.getAllContacts(authRepository.getToken()!!)
                Log.d("Contacts", fetchedData.toString())
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