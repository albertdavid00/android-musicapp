package com.unibuc.musicapp.di

import android.content.Context
import android.content.SharedPreferences
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.MusicRepository
import com.unibuc.musicapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMusicRepository(api: MusicApi) = MusicRepository(api)

    @Singleton
    @Provides
    fun provideMusicApi(): MusicApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MusicApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("MusicApp", Context.MODE_PRIVATE)
    }
}