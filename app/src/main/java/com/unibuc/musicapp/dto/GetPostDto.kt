package com.unibuc.musicapp.dto

import com.unibuc.musicapp.utils.Visibility

data class GetPostDto(
    val id: Long,
    val description: String,
    val visibility: Visibility,
    val videoUrl: String
)
