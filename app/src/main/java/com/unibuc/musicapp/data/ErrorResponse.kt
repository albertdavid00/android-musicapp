package com.unibuc.musicapp.data

import com.google.gson.annotations.SerializedName

data class ErrorResponse(@SerializedName("message") val message: String?)
