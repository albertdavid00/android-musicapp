package com.unibuc.musicapp.utils

enum class Role(private val displayName: String) {
    USER("Musician"),
    MANAGER("Scout");

    override fun toString(): String {
        return displayName
    }
}

