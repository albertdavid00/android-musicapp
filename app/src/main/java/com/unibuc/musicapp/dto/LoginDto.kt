package com.unibuc.musicapp.dto

data class LoginDto(
    val email: String,
    val password: String,
    val grantType: String = "password")
