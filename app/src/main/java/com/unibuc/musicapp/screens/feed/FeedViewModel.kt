package com.unibuc.musicapp.screens.feed

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.unibuc.musicapp.dto.MessageContentDto
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import com.unibuc.musicapp.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val api: MusicApi,
    private val authRepository: AuthRepository
): ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    fun markPostAsSeen(postId: Long) {
        viewModelScope.launch {
            try {
                val client = OkHttpClient()
                val messageContent = MessageContentDto(postId = postId, userId = authRepository.getUserId())
                val gson = Gson()
                val jsonString = gson.toJson(messageContent)
                val encodedString = Base64.getEncoder().encodeToString(jsonString.toByteArray())
                val queueMessage = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <QueueMessage>
                        <MessageText>$encodedString</MessageText>
                    </QueueMessage>
                """.trimIndent()

                val mediaType = "application/xml; charset=utf-8".toMediaType()
                val requestBody = queueMessage.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(Constants.AZURE_QUEUE_SAS_URL)
                    .post(requestBody)
                    .header("Content-Type", "application.xml")
                    .build()

                withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        // Handle response
                        response.body?.string()?.let { Log.d("AZURE_QUEUE", it) }
                    }
                }

            } catch (e: HttpException) {
                Log.d("Feed", "Err: " + e.response()?.errorBody()?.string()!!)
            } catch (e: Exception) { // Catching all exceptions
                Log.d("Feed", "Err: " + e.message.toString())
            }
        }
    }

}