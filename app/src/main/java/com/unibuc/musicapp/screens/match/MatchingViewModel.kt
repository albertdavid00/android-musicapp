package com.unibuc.musicapp.screens.match

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unibuc.musicapp.dto.LocationDto
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class MatchingViewModel  @Inject constructor(
    private val api: MusicApi,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _userLocation = MutableLiveData<LocationDto?>()
    val userLocation: MutableLiveData<LocationDto?> = _userLocation

    private val _users = MutableLiveData<List<UserDto>>()
    val users: MutableLiveData<List<UserDto>> = _users

    private var _currentUserIndex = MutableLiveData(0)
    val currentUserIndex: LiveData<Int> = _currentUserIndex

    private val _showMatchDialog = MutableStateFlow(false)
    val showMatchDialog = _showMatchDialog.asStateFlow()

    private val _matchedUserName = MutableStateFlow("")
    val matchedUserName = _matchedUserName.asStateFlow()
    fun saveUserLocation(context: Context) {
        viewModelScope.launch {
            try {
                val locationDto = getLocation(context)
                if (locationDto != null) {
                    val response = api.saveUserLocation(authRepository.getToken()!!, locationDto)
                    if (response.isSuccessful) {
                        _userLocation.value = locationDto
                        Log.d("MatchingScreen", "Location: " + _userLocation.value.toString())
                    }
                }
            } catch (e: HttpException) {
                Log.d("MatchingScreen", "Err: " + e.response()?.errorBody()?.string()!!)
            } catch (e: Exception) { // Catching all exceptions
                Log.d("MatchingScreen", "Err: " + e.message.toString())
            }
        }
    }

    @SuppressLint("ServiceCast", "MissingPermission")
    suspend fun getLocation(context: Context): LocationDto? = withContext(Dispatchers.IO) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: return@withContext null

        val geocoder = Geocoder(context, Locale.US)
        val address = geocoder.getAddress(location.latitude, location.longitude)
        Log.d("MatchingScreen", address?.locality.toString())

        if (address != null) {
            LocationDto(location.latitude, location.longitude, address.locality.toString(), address.countryName.toString())
        } else null
    }

    private suspend fun Geocoder.getAddress(
        latitude: Double,
        longitude: Double,
    ): Address? = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCoroutine { cont ->
                    getFromLocation(latitude, longitude, 1) {
                        cont.resume(it.firstOrNull())
                    }
                }
            } else {
                suspendCoroutine { cont ->
                    @Suppress("DEPRECATION")
                    val address = getFromLocation(latitude, longitude, 1)?.firstOrNull()
                    cont.resume(address)
                }
            }
        } catch (e: Exception) {
            Log.d("Exc", e.message.toString())
            null
        }
    }

    fun getUserRecommendations() {
        viewModelScope.launch {
            try {
                val fetchedUsers = api.getRecommendedUsers(authRepository.getToken()!!)
                Log.d("MatchingScreen", fetchedUsers.toString())
                _users.postValue(fetchedUsers)
            } catch (e: HttpException) {
            Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }
    fun nextUser() {
        _currentUserIndex.value?.let {
            if (it < (_users.value?.size ?: 0)) {
                _currentUserIndex.value = it + 1
            }
        }
    }
    fun likeUserAction(user: UserDto) {
        viewModelScope.launch {
            try {
                val response = api.likeUser(authRepository.getToken()!!, user.id)
                if (response.isSuccessful) {
                    if (-1L == response.body()) {
                        Log.d("MatchingScreen", "Not a match yet.")
                    } else {
                        _matchedUserName.value = user.firstName + " " +  user.lastName
                        _showMatchDialog.value = true
                        Log.d("MatchingScreen", "${user.id} and ${authRepository.getUserId()} matched.")
                    }
                }
            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }
    fun dislikeUserAction(userId: Long) {
        viewModelScope.launch {
            try {
                val response = api.dislikeUser(authRepository.getToken()!!, userId)
            } catch (e: HttpException) {
                Log.d("Exc", e.code().toString() + " " + e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                Log.d("Exc", e.message.toString())
            }
        }
    }

    fun dismissMatchDialog() {
        _showMatchDialog.value = false
        _matchedUserName.value = ""
    }



}