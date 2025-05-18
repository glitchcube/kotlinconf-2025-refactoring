package com.example.pii

import java.util.UUID

@JvmInline
value class UserId(val value: String) {
    override fun toString() = value
    
    companion object {
        fun newId() = UserId(UUID.randomUUID().toString())
    }
}