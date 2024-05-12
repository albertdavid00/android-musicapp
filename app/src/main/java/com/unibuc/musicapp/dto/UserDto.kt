package com.unibuc.musicapp.dto

import com.unibuc.musicapp.utils.Instrument

data class UserDto(val id:Long,
                   val firstName: String,
                   val lastName: String,
                   val age: Int,
                   val followers: Int,
                   val following: Int,
                   val posts: Int,
                   val profilePictureUrl: String,
                   val instrumentsPlayed: List<Instrument>,
                   val isFollowedByCurrentUser: Boolean,
                   val postsList: List<PostDto>,
                   val location: LocationDto)
