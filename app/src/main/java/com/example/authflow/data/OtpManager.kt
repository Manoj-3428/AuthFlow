package com.example.authflow.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

data class OtpData(
    val code: String,
    val expiryTimeMillis: Long,
    var attemptCount: Int = 0
)

class OtpManager {
    private val otpMap = mutableMapOf<String, OtpData>()
    private val mutex = Mutex()
    
    companion object {
        private const val OTP_LENGTH = 6
        private const val OTP_EXPIRY_SECONDS = 60L
        private const val MAX_ATTEMPTS = 3
    }
    
    suspend fun generateOtp(email: String): String {
        mutex.withLock {
            val code = generateRandomOtp()
            val expiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(OTP_EXPIRY_SECONDS)
            otpMap[email] = OtpData(
                code = code,
                expiryTimeMillis = expiryTime,
                attemptCount = 0
            )
            return code
        }
    }
    
    suspend fun validateOtp(email: String, inputOtp: String): OtpValidationResult {
        mutex.withLock {
            val otpData = otpMap[email] ?: return OtpValidationResult.NotFound
            
            if (System.currentTimeMillis() > otpData.expiryTimeMillis) {
                otpMap.remove(email)
                return OtpValidationResult.Expired
            }
            
            if (otpData.attemptCount >= MAX_ATTEMPTS) {
                otpMap.remove(email)
                return OtpValidationResult.MaxAttemptsExceeded
            }
            
            otpData.attemptCount++
            
            if (otpData.code != inputOtp) {
                if (otpData.attemptCount >= MAX_ATTEMPTS) {
                    otpMap.remove(email)
                    return OtpValidationResult.MaxAttemptsExceeded
                }
                return OtpValidationResult.Incorrect
            }
            
            otpMap.remove(email)
            return OtpValidationResult.Success
        }
    }
    
    suspend fun getRemainingTimeSeconds(email: String): Long? {
        mutex.withLock {
            val otpData = otpMap[email] ?: return null
            val remaining = otpData.expiryTimeMillis - System.currentTimeMillis()
            return if (remaining > 0) {
                TimeUnit.MILLISECONDS.toSeconds(remaining)
            } else {
                otpMap.remove(email)
                null
            }
        }
    }
    
    suspend fun canResendOtp(email: String): Boolean {
        mutex.withLock {
            val otpData = otpMap[email] ?: return true
            val isExpired = System.currentTimeMillis() > otpData.expiryTimeMillis
            val maxAttemptsReached = otpData.attemptCount >= MAX_ATTEMPTS
            return isExpired || maxAttemptsReached
        }
    }
    
    suspend fun clearOtp(email: String) {
        mutex.withLock {
            otpMap.remove(email)
        }
    }
    
    private fun generateRandomOtp(): String {
        return (100000..999999).random().toString()
    }
}

sealed class OtpValidationResult {
    object Success : OtpValidationResult()
    object Incorrect : OtpValidationResult()
    object Expired : OtpValidationResult()
    object MaxAttemptsExceeded : OtpValidationResult()
    object NotFound : OtpValidationResult()
}
