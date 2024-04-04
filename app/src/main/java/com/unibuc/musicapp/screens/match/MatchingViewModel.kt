package com.unibuc.musicapp.screens.match

import androidx.lifecycle.ViewModel
import com.unibuc.musicapp.network.MusicApi
import com.unibuc.musicapp.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MatchingViewModel  @Inject constructor(
    private val api: MusicApi,
    private val authRepository: AuthRepository
): ViewModel() {
}