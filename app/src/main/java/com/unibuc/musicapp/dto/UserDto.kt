package com.unibuc.musicapp.dto

data class UserDto(val id:Long,
                   val firstName: String,
                   val lastName: String,
                   val age: Int,
                   val followers: Int,
                   val following: Int,
                   val posts: Int,
                   val profilePictureUrl: String,
                   val instrumentsPlayed: List<String>,
                   val isFollowedByCurrentUser: Boolean)
