package com.example.pii

interface UserIdValidator {
    fun isValid(userId: UserId): Boolean
}
