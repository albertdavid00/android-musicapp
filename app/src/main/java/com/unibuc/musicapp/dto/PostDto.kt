package com.unibuc.musicapp.dto

import com.unibuc.musicapp.utils.Visibility
import okhttp3.MultipartBody

data class PostDto(
    val description: String,
    val visibility: Visibility,
    val videoUrl: String
)