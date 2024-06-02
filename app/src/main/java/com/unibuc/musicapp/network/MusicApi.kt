package com.unibuc.musicapp.network

import com.unibuc.musicapp.dto.AddCommentDto
import com.unibuc.musicapp.dto.CommentDto
import com.unibuc.musicapp.dto.FeedPostDto
import com.unibuc.musicapp.dto.LocationDto
import com.unibuc.musicapp.dto.LoginDto
import com.unibuc.musicapp.dto.PostDto
import com.unibuc.musicapp.dto.ReactionDto
import com.unibuc.musicapp.dto.TokenDto
import com.unibuc.musicapp.dto.UserDto
import com.unibuc.musicapp.dto.UserProfileDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
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
                         @Part("role") role: RequestBody,
                         @Part("age") age: RequestBody,
                         @Part imagePart: MultipartBody.Part,
                         @Part("instruments") instruments: RequestBody)

    @GET("/users/current")
    suspend fun getCurrentUser(@Header("Authorization") accessToken: String): UserProfileDto

    @GET("/users/{userId}")
    suspend fun getUser(@Header("Authorization") accessToken: String, @Path("userId") userId: Long): UserProfileDto
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
    @Multipart
    @POST("/posts")
    suspend fun uploadPost(@Header("Authorization") accessToken: String,
                           @Part("description") description: RequestBody,
                           @Part("visibility") visibility: RequestBody,
                           @Part videoPart: MultipartBody.Part): Long
    @POST("/posts/upload")
    suspend fun uploadPost(@Header("Authorization") accessToken: String, @Body postDto: PostDto): Long
    @GET("/posts")
    suspend fun getFeedPosts(@Header("Authorization") accessToken: String): List<FeedPostDto>

    @POST("/comments/{postId}")
    suspend fun addComment(@Header("Authorization") accessToken: String, @Body comment: AddCommentDto, @Path("postId") postId: Long): CommentDto

    @POST("/reactions/post/{postId}")
    suspend fun addReactionToPost(@Header("Authorization") accessToken: String, @Body reaction: ReactionDto, @Path("postId") postId: Long): Long

    @POST("/reactions/comment/{commentId}")
    suspend fun addReactionToComment(@Header("Authorization") accessToken: String, @Body reaction: ReactionDto, @Path("commentId") commentId: Long): Long

    @DELETE("/reactions/post/{postId}")
    suspend fun removeReactionFromPost(@Header("Authorization") accessToken: String, @Path("postId") postId: Long)

    @DELETE("/reactions/{reactionId}")
    suspend fun removeReaction(@Header("Authorization") accessToken: String, @Path("reactionId") reactionId: Long)

    @DELETE("/comments/{commentId}")
    suspend fun removeComment(@Header("Authorization") accessToken: String, @Path("commentId") commentId: Long)

    @DELETE("/posts/{postId}")
    suspend fun removePost(@Header("Authorization") accessToken: String, @Path("postId") postId: Long): Response<Void>

    @GET("/posts/user/{id}")
    suspend fun getUserPosts(@Header("Authorization") accessToken: String, @Path("id") id: Long): List<FeedPostDto>

    @POST("/users/location")
    suspend fun saveUserLocation(@Header("Authorization") accessToken: String, @Body locationDto: LocationDto): Response<Long>

    @GET("/users/recommended")
    suspend fun getRecommendedUsers(@Header("Authorization") accessToken: String): List<UserDto>

    @POST("/users/like/{userId}")
    suspend fun likeUser(@Header("Authorization") accessToken: String, @Path("userId") id: Long): Response<Long>
    @POST("/users/dislike/{userId}")
    suspend fun dislikeUser(@Header("Authorization") accessToken: String, @Path("userId") id: Long): Response<Void>

    @GET("/users/matches")
    suspend fun getMatchedUsers(@Header("Authorization") accessToken: String): List<UserDto>

    @POST("/users/contact/{userId}")
    suspend fun addContact(@Header("Authorization") accessToken: String, @Path("userId") userId: Long): Response<Void>

    @GET("/users/contacts")
    suspend fun getAllContacts(@Header("Authorization") accessToken: String): List<UserDto>
}