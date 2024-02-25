package com.unibuc.musicapp.repository

import android.nfc.Tag
import android.util.Log
import com.unibuc.musicapp.data.DataOrException
import com.unibuc.musicapp.dto.LoginDto
import com.unibuc.musicapp.dto.TokenDto
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.network.MusicApi
import javax.inject.Inject

class MusicRepository @Inject constructor(private val api: MusicApi) {
    private val dataOrException = DataOrException<List<UserDto>, Boolean, Exception>()

    suspend fun getAllUsers(): DataOrException<List<UserDto>, Boolean, Exception> {
        try{
            Log.d("repoUSER", "calling api")
            dataOrException.loading = true
            dataOrException.data = api.getAllUsers()
            Log.d("repoUSER", "calling: ${dataOrException.data.toString()}")
            if(dataOrException.data.toString().isNotEmpty()) {
                dataOrException.loading = false
            }
        } catch (exception: Exception) {
            dataOrException.e = exception
            Log.d("Exc", "getAllUsers: ${dataOrException.e!!.localizedMessage}")
        }
        return dataOrException
    }

}