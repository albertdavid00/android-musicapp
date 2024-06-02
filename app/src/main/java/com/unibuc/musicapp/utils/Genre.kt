package com.unibuc.musicapp.utils

enum class Genre(private val displayName: String) {
    POP("Pop"),
    ROCK("Rock"),
    FOLK("Folk"),
    RAP("Rap"),
    RB("R&B"),
    COUNTRY("Country"),
    JAZZ("Jazz"),
    CLASSIC("Classic"),
    BLUES("Blues"),
    HEAVY_METAL("Heavy Metal"),
    OTHER("Other");
    override fun toString(): String {
        return displayName
    }
}