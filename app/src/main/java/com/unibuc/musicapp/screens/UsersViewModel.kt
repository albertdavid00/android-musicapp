package com.unibuc.musicapp.screens

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unibuc.musicapp.data.DataOrException
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(private val repository: MusicRepository): ViewModel(){
    val data: MutableState<DataOrException<List<UserDto>, Boolean, Exception>> =
        mutableStateOf(DataOrException(null, true, Exception("")))

    init {
        getAllUsers()
    }

    private fun getAllUsers() {
        viewModelScope.launch {
            data.value.loading = true
            data.value = repository.getAllUsers()
            if(data.value.data.toString().isNotEmpty()) {
                data.value.loading = false
            }
        }
    }
}