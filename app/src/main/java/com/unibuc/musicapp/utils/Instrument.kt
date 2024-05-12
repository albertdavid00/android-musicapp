package com.unibuc.musicapp.utils

enum class Instrument(private val displayName: String) {
    VOCALIST("Vocalist"),
    ACOUSTIC_GUITAR("Acoustic Guitar"),
    ELECTRIC_GUITAR("Electric Guitar"),
    BASS("Bass"),
    DRUMS("Drums");

    override fun toString(): String {
        return displayName
    }
}