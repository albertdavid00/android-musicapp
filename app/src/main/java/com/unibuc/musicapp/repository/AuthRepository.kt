package com.unibuc.musicapp.repository

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.auth0.android.jwt.Claim
import com.auth0.android.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.unibuc.musicapp.dto.TokenDto
import com.unibuc.musicapp.utils.Constants.ACCESS_TOKEN
import org.json.JSONObject
import javax.inject.Inject

class AuthRepository @Inject constructor(private val sharedPreferences: SharedPreferences) {
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _token = MutableLiveData<TokenDto?>()
    val token: LiveData<TokenDto?> get() = _token

    init {
        // Initial check or listen for changes in SharedPreferences to update isLoggedIn
        _isLoggedIn.value = !sharedPreferences.getString(ACCESS_TOKEN, null).isNullOrEmpty()
    }

    fun updateToken(token: TokenDto) {
        val accessToken = "Bearer " + token.accessToken
        sharedPreferences.edit().putString(ACCESS_TOKEN, accessToken).apply()
        _isLoggedIn.value = true
        _token.value = token
        sharedPreferences.getString(ACCESS_TOKEN, "No token found.")?.let { Log.d("FORM", it) } // log
    }

    fun deleteToken() {
        sharedPreferences.edit().clear().apply()
        _isLoggedIn.value = false
        _token.value = null
        Log.d("DELETE", "Token cleared from shared preferences.")
    }

    fun getToken(): String? = sharedPreferences.getString(ACCESS_TOKEN, null)

    fun getUserId(): Long {
        val jwt = JWT(getToken()!!.replace("Bearer ", ""))
        return jwt.getClaim("preferred_username").asLong()!!
    }

    fun hasManagerRole(): Boolean {
        val jwt: DecodedJWT = com.auth0.jwt.JWT.decode(getToken()!!.replace("Bearer ", ""))
        val roles = jwt.getClaim("realm_access").asMap()["roles"] as List<String>
        return roles.contains("MANAGER")
    }

    fun hasUserRole(): Boolean {
        val jwt: DecodedJWT = com.auth0.jwt.JWT.decode(getToken()!!.replace("Bearer ", ""))
        val roles = jwt.getClaim("realm_access").asMap()["roles"] as List<String>
        return roles.contains("USER")
    }
}