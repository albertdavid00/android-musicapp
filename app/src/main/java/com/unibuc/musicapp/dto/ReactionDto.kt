package com.unibuc.musicapp.dto

import com.unibuc.musicapp.utils.ReactionType

data class ReactionDto(
    val id: Long? = null,
    val reactionType: ReactionType,
    val userId: Long?=null
)
