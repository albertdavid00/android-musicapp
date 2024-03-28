package com.unibuc.musicapp.di

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.MusicRepository
import com.unibuc.musicapp.utils.Constants
import com.unibuc.musicapp.utils.InstantAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMusicRepository(api: MusicApi) = MusicRepository(api)

    @RequiresApi(Build.VERSION_CODES.O)
    @Singleton
    @Provides
    fun provideMusicApi(): MusicApi {
        val gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantAdapter())
            .create()
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .writeTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MusicApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("MusicApp", Context.MODE_PRIVATE)
    }
}