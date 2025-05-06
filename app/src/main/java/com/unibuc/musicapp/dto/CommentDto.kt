package com.unibuc.musicapp.dto

import java.time.Instant

data class CommentDto(
    val id: Long,
    val userId: Long,
    val content: String,
    val isLiked: Boolean,
    val username: String,
    val creationTime: Instant,
    val reactions: List<ReactionDto>,
    val userProfilePicture: String
)
