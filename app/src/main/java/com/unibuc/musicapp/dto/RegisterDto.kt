package com.unibuc.musicapp.dto

import com.unibuc.musicapp.utils.Instrument
import com.unibuc.musicapp.utils.Role
import okhttp3.MultipartBody

data class RegisterDto(val email: String,
                       val password: String,
                       val lastName: String,
                       val firstName: String,
                       val selectedRole: Role,
                       val age: Int,
                       val imagePart: MultipartBody.Part,
                       val instruments: List<Instrument>)
