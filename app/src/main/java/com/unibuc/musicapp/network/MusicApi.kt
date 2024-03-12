package com.unibuc.musicapp.network

import com.unibuc.musicapp.dto.LoginDto
import com.unibuc.musicapp.dto.RegisterDto
import com.unibuc.musicapp.dto.TokenDto
import com.unibuc.musicapp.dto.UserDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
interface MusicApi {
    @GET("/users")
    suspend fun getAllUsers(): List<UserDto>

    @POST("/login")
    suspend fun login(@Body loginDto: LoginDto): TokenDto

    @Multipart
    @POST("/users/register")
    suspend fun register(@Part("email") email: RequestBody,
                         @Part("password") password: RequestBody,
                         @Part("lastName") lastName: RequestBody,
                         @Part("firstName") firstName: RequestBody,
                         @Part("age") age: RequestBody,
                         @Part imagePart: MultipartBody.Part)

    @GET("/users/current")
    suspend fun getCurrentUser(@Header("Authorization") accessToken: String): UserDto

    @GET("/users/{userId}")
    suspend fun getUser(@Header("Authorization") accessToken: String, @Path("userId") userId: Long): UserDto
    @GET("/users/followers/{userId}")
    suspend fun getUserFollowers(@Header("Authorization") accessToken: String, @Path("userId") userId: Long): List<UserDto>

    @GET("/users/following/{userId}")
    suspend fun getUserFollowing(@Header("Authorization") accessToken: String, @Path("userId") userId: Long): List<UserDto>

    @DELETE("/users/unfollow/{userId}")
    suspend fun unfollowUser(@Header("Authorization") accessToken: String, @Path("userId") userId: Long)

    @POST("/users/follow/{userId}")
    suspend fun followUser(@Header("Authorization") accessToken: String, @Path("userId") userId: Long): Long

    @GET("/users/search")
    suspend fun filterUsers(@Header("Authorization") accessToken: String, @Query("searchQuery") searchQuery: String): List<UserDto>
}