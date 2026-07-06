package io.github.ronjunevaldoz.shadcncompose

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
