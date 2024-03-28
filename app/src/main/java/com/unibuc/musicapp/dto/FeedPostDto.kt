package com.unibuc.musicapp.dto

import com.unibuc.musicapp.utils.Visibility
import java.time.Instant

data class FeedPostDto(
    val id: Long,
    val seen: Boolean,
    val isLiked: Boolean,
    val videoUrl: String,
    val userDto: UserDto,
    val description: String,
    val creationTime: Instant,
    val visibility: Visibility,
    val comments: List<CommentDto>,
    val reactions: List<ReactionDto>
)
