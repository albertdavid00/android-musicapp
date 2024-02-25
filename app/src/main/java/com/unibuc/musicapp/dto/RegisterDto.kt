package com.unibuc.musicapp.dto

import okhttp3.MultipartBody

data class RegisterDto(val email: String,
    val password: String,
    val lastName: String,
    val firstName: String,
    val age: Int,
    val imagePart: MultipartBody.Part)
