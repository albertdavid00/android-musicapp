package com.unibuc.musicapp.screens.post

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unibuc.musicapp.dto.PostDto
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import com.unibuc.musicapp.utils.Constants
import com.unibuc.musicapp.utils.Visibility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.unibuc.musicapp.utils.Genre

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val api: MusicApi,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading


    fun uploadPostToAzureAndDB(
        videoFile: File,
        description: String,
        selectedVisibility: Visibility,
        selectedGenre: Genre,
        context: Context,
        redirectToFeed: () -> Unit
    ) {
        val videoUrl = authRepository.getUserId().toString() + "_"+ System.currentTimeMillis() + "_" + videoFile.name
        val blobUrl = Constants.AZURE_BLOB_SAS_URL + videoUrl
        val sasUrl = blobUrl + Constants.AZURE_BLOB_SAS_TOKEN
        val cdnUrl = Constants.AZURE_CDN_PREFIX_URL + videoUrl
        val postDto = PostDto(description, selectedVisibility, selectedGenre, cdnUrl)
        uploadFileToAzureAndDB(videoFile, postDto, sasUrl, context, redirectToFeed)
    }
    private fun uploadFileToAzureAndDB(file: File, postDto: PostDto, sasUrl: String, context: Context, redirectToFeed: () -> Unit) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                val compressedFile = compressVideoFile(file, context)
                val client = OkHttpClient()
                val fileBody = compressedFile!!.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(sasUrl) // Your SAS URL
                    .put(fileBody) // Use PUT for uploading
                    .header("x-ms-blob-type", "BlockBlob")
                    .header("x-ms-meta-userId", authRepository.getUserId().toString())
                    .build()

                // Switch to Dispatchers.IO for the network call
                withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            Log.d("Create_Post", "File upload error $response")
                            throw IOException("Unexpected code $response")
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    val musicApiResponse = api.uploadPost(authRepository.getToken()!!, postDto)
                    Log.d("Create_Post", musicApiResponse.toString())
                    redirectToFeed()
                }
            } catch (e: HttpException) {
                Log.d("Create_Post", "Err: " + e.response()?.errorBody()?.string()!!)
            } catch (e: Exception) { // Catching all exceptions
                Log.d("Create_Post", "Err: " + e.message.toString())
            } finally {
                _isUploading.value = false
            }
        }
    }

    private suspend fun compressVideoFile(inputFile: File, context: Context): File? = withContext(Dispatchers.IO) {

        val outputFilePath = "${context.cacheDir}/compressed_${inputFile.name}.mp4"
        val outputFile = File(outputFilePath)
        Log.d("Create_Post", "File: " + inputFile.absolutePath + " " + inputFile.extension)
        Log.d("Create_Post", "outputfilePath: " + outputFile.absolutePath)
        try {
            // FFmpeg command for compression. Adjust parameters as needed.
            val cmd = "-y -i ${inputFile.absolutePath} -vcodec libx264 -crf 28 -preset ultrafast ${outputFile.absolutePath}"

            // Execute the FFmpeg command
            val session = FFmpegKit.execute(cmd)
            Log.d("Create_Post", "session: " + session.returnCode)
            session.allLogs.forEach { log ->
                Log.d("Create_Post", log.message)
            }
            if (ReturnCode.isSuccess(session.returnCode)) {
                // Compression was successful, return the new file
                outputFile
            } else {
                // Compression failed, delete the output file if it exists and return null
                outputFile.delete()
                null
            }
        } catch (e: Exception) {
            // In case of an exception, ensure we clean up
            Log.d("Create_Post", e.message.toString())
            if (outputFile.exists()) outputFile.delete()
            null
        }
    }

//    fun uploadPost(postDto: PostDto, redirectToFeed: () -> Unit) {
//        viewModelScope.launch {
//            try {
//                _isUploading.value = true
//                Log.d("Create_Post", "Uploading $postDto")
//                val description = postDto.description.toRequestBody("text/plain".toMediaTypeOrNull())
//                val visibility = postDto.visibility.name.toRequestBody("text/plain".toMediaTypeOrNull())
//                val videoPart = postDto.videoPart
//                val response = api.uploadPost(authRepository.getToken()!!, description, visibility, videoPart)
//                Log.d("Create_Post", response.toString())
//                redirectToFeed()
//            } catch (e: HttpException) {
//                Log.d("Create_Post", "Err: " + e.response()?.errorBody()?.string()!!)
//            } catch (e: Exception) {
//                Log.d("Create_Post", "Err: " + e.message.toString())
//            } finally {
//                _isUploading.value = false
//            }
//        }
//    }



}