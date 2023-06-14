package com.hungama.music.utils.customview.applelogin

sealed class SignInWithAppleResult {
    data class Success(val authorizationCode: String) : SignInWithAppleResult()

    data class Failure(val error: Throwable) : SignInWithAppleResult()

    object Cancel : SignInWithAppleResult()
}