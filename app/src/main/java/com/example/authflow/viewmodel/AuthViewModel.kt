package com.example.authflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authflow.data.OtpManager
import com.example.authflow.data.OtpValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AuthEvent {
    data class SendOtp(val email: String) : AuthEvent()
    data class VerifyOtp(val otp: String) : AuthEvent()
    data object ResendOtp : AuthEvent()
    data object Logout : AuthEvent()
}

class AuthViewModel(
    private val otpManager: OtpManager = OtpManager()
) : ViewModel() {
    
    private val _state = MutableStateFlow<AuthState>(AuthState.EmailInput)
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    private var otpCountdownJob: Job? = null
    private var sessionTimerJob: Job? = null
    private var currentEmail: String? = null
    private var sessionStartTime: Long? = null
    
    fun handleEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.SendOtp -> sendOtp(event.email)
            is AuthEvent.VerifyOtp -> verifyOtp(event.otp)
            is AuthEvent.ResendOtp -> resendOtp()
            is AuthEvent.Logout -> logout()
        }
    }
    
    private fun sendOtp(email: String) {
        if (email.isBlank()) return
        
        viewModelScope.launch {
            currentEmail = email
            val otpCode = otpManager.generateOtp(email)
            _state.update { AuthState.OtpSent(email, 60, otpCode) }
            startOtpCountdown(email)
        }
    }
    
    private fun verifyOtp(otp: String) {
        val email = currentEmail ?: return
        if (otp.length != 6) return
        
        viewModelScope.launch {
            val currentOtp = otpManager.getCurrentOtp(email) ?: ""
            val remaining = otpManager.getRemainingTimeSeconds(email) ?: 0
            _state.update { 
                AuthState.OtpVerifying(email, remaining, currentOtp)
            }
            
            val result = otpManager.validateOtp(email, otp)
            
            when (result) {
                is OtpValidationResult.Success -> {
                    otpCountdownJob?.cancel()
                    sessionStartTime = System.currentTimeMillis()
                    _state.update { 
                        AuthState.SessionActive(
                            email = email,
                            sessionStartTime = sessionStartTime!!,
                            sessionDurationSeconds = 0
                        )
                    }
                    startSessionTimer()
                }
                is OtpValidationResult.Incorrect -> {
                    val remaining = otpManager.getRemainingTimeSeconds(email) ?: 0
                    val currentOtp = otpManager.getCurrentOtp(email)
                    _state.update { 
                        AuthState.OtpError(
                            email = email,
                            errorType = OtpErrorType.Incorrect,
                            otpCode = currentOtp
                        )
                    }
                    if (remaining > 0) {
                        startOtpCountdown(email)
                    }
                }
                is OtpValidationResult.Expired -> {
                    _state.update { 
                        AuthState.OtpError(
                            email = email,
                            errorType = OtpErrorType.Expired
                        )
                    }
                }
                is OtpValidationResult.MaxAttemptsExceeded -> {
                    otpCountdownJob?.cancel()
                    _state.update { 
                        AuthState.OtpError(
                            email = email,
                            errorType = OtpErrorType.MaxAttemptsExceeded
                        )
                    }
                }
                is OtpValidationResult.NotFound -> {
                    _state.update { 
                        AuthState.OtpError(
                            email = email,
                            errorType = OtpErrorType.NotFound
                        )
                    }
                }
            }
        }
    }
    
    private fun resendOtp() {
        val email = currentEmail ?: return
        
        viewModelScope.launch {
            val canResend = otpManager.canResendOtp(email)
            if (canResend) {
                otpCountdownJob?.cancel()
                otpManager.clearOtp(email)
                val otpCode = otpManager.generateOtp(email)
                _state.update { AuthState.OtpSent(email, 60, otpCode) }
                startOtpCountdown(email)
            }
        }
    }
    
    private fun logout() {
        sessionTimerJob?.cancel()
        otpCountdownJob?.cancel()
        currentEmail = null
        sessionStartTime = null
        viewModelScope.launch {
            currentEmail?.let { otpManager.clearOtp(it) }
        }
        _state.update { AuthState.EmailInput }
    }
    
    private fun startOtpCountdown(email: String) {
        otpCountdownJob?.cancel()
        otpCountdownJob = viewModelScope.launch {
            var remaining = 60L
            while (remaining > 0) {
                delay(1000)
                remaining--
                val currentState = _state.value
                if (currentState is AuthState.OtpSent || currentState is AuthState.OtpVerifying) {
                    _state.update {
                        when (it) {
                            is AuthState.OtpSent -> it.copy(remainingSeconds = remaining)
                            is AuthState.OtpVerifying -> it.copy(remainingSeconds = remaining)
                            else -> it
                        }
                    }
                }
                
                val actualRemaining = otpManager.getRemainingTimeSeconds(email)
                if (actualRemaining == null || actualRemaining <= 0) {
                    if (currentState is AuthState.OtpSent || currentState is AuthState.OtpVerifying) {
                        val currentOtp = (currentState as? AuthState.OtpSent)?.otpCode ?: (currentState as? AuthState.OtpVerifying)?.otpCode ?: ""
                        _state.update {
                            AuthState.OtpError(
                                email = email,
                                errorType = OtpErrorType.Expired,
                                otpCode = if (currentOtp.isNotEmpty()) currentOtp else null
                            )
                        }
                    }
                    break
                }
                remaining = actualRemaining
            }
        }
    }
    
    private fun startSessionTimer() {
        sessionTimerJob?.cancel()
        val startTime = sessionStartTime ?: return
        
        sessionTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentState = _state.value
                if (currentState is AuthState.SessionActive) {
                    val duration = (System.currentTimeMillis() - startTime) / 1000
                    _state.update {
                        if (it is AuthState.SessionActive) {
                            it.copy(sessionDurationSeconds = duration)
                        } else {
                            it
                        }
                    }
                } else {
                    break
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        otpCountdownJob?.cancel()
        sessionTimerJob?.cancel()
    }
}
