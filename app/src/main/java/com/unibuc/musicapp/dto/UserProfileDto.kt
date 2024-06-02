package com.unibuc.musicapp.dto

import com.unibuc.musicapp.utils.Instrument
import com.unibuc.musicapp.utils.Role

data class UserProfileDto(val id:Long,
                          val firstName: String,
                          val lastName: String,
                          val age: Int,
                          val followers: Int,
                          val following: Int,
                          val posts: Int,
                          val profilePictureUrl: String,
                          val instrumentsPlayed: List<Instrument>,
                          val isFollowedByCurrentUser: Boolean,
                          val role: Role
)
