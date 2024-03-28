package com.unibuc.musicapp.network

import com.unibuc.musicapp.dto.AddCommentDto
import com.unibuc.musicapp.dto.CommentDto
import com.unibuc.musicapp.dto.FeedPostDto
import com.unibuc.musicapp.dto.LoginDto
import com.unibuc.musicapp.dto.PostDto
import com.unibuc.musicapp.dto.ReactionDto
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

}