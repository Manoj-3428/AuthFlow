package com.example.authflow.viewmodel

sealed class AuthState {
    data object EmailInput : AuthState()
    
    data class OtpSent(
        val email: String,
        val remainingSeconds: Long,
        val otpCode: String
    ) : AuthState()
    
    data class OtpVerifying(
        val email: String,
        val remainingSeconds: Long,
        val otpCode: String
    ) : AuthState()
    
    data class OtpError(
        val email: String,
        val errorType: OtpErrorType,
        val remainingAttempts: Int? = null,
        val otpCode: String? = null
    ) : AuthState()
    
    data class SessionActive(
        val email: String,
        val sessionStartTime: Long,
        val sessionDurationSeconds: Long
    ) : AuthState()
}

enum class OtpErrorType {
    Expired,
    Incorrect,
    MaxAttemptsExceeded,
    NotFound
}
