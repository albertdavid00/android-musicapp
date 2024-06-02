package com.unibuc.musicapp.utils

enum class Role(private val displayName: String) {
    USER("User"),
    MANAGER("Manager");

    override fun toString(): String {
        return displayName
    }
}

