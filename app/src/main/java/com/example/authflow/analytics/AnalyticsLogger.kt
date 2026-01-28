package com.example.authflow.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsLogger private constructor(context: Context) {
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    companion object {
        @Volatile
        private var INSTANCE: AnalyticsLogger? = null

        fun getInstance(): AnalyticsLogger {
            return INSTANCE ?: throw IllegalStateException(
                "AnalyticsLogger must be initialized with initialize(context) first"
            )
        }

        fun initialize(context: Context): AnalyticsLogger {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnalyticsLogger(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private object Events {
        const val OTP_SENT = "otp_sent"
        const val OTP_VERIFIED = "otp_verified"
        const val OTP_VERIFICATION_FAILED = "otp_verification_failed"
        const val OTP_RESENT = "otp_resent"
        const val OTP_EXPIRED = "otp_expired"
        const val MAX_ATTEMPTS_EXCEEDED = "max_attempts_exceeded"
        const val SESSION_STARTED = "session_started"
        const val SESSION_ENDED = "session_ended"
        const val SCREEN_VIEW = "screen_view"
        const val BUTTON_CLICKED = "button_clicked"
    }

    private object Params {
        const val EMAIL = "email"
        const val SCREEN_NAME = "screen_name"
        const val BUTTON_NAME = "button_name"
        const val ERROR_TYPE = "error_type"
        const val SESSION_DURATION = "session_duration"
        const val ATTEMPT_COUNT = "attempt_count"
        const val OTP_LENGTH = "otp_length"
    }

    fun logOtpSent(email: String) {
        logEvent(Events.OTP_SENT) {
            param(Params.EMAIL, email)
        }
    }

    fun logOtpVerified(email: String) {
        logEvent(Events.OTP_VERIFIED) {
            param(Params.EMAIL, email)
        }
    }

    fun logOtpVerificationFailed(email: String, errorType: String) {
        logEvent(Events.OTP_VERIFICATION_FAILED) {
            param(Params.EMAIL, email)
            param(Params.ERROR_TYPE, errorType)
        }
    }

    fun logOtpResent(email: String) {
        logEvent(Events.OTP_RESENT) {
            param(Params.EMAIL, email)
        }
    }

    fun logOtpExpired(email: String) {
        logEvent(Events.OTP_EXPIRED) {
            param(Params.EMAIL, email)
        }
    }

    fun logMaxAttemptsExceeded(email: String, attemptCount: Int) {
        logEvent(Events.MAX_ATTEMPTS_EXCEEDED) {
            param(Params.EMAIL, email)
            param(Params.ATTEMPT_COUNT, attemptCount.toLong())
        }
    }

    fun logSessionStarted(email: String) {
        logEvent(Events.SESSION_STARTED) {
            param(Params.EMAIL, email)
        }
    }

    fun logSessionEnded(email: String, durationSeconds: Long) {
        logEvent(Events.SESSION_ENDED) {
            param(Params.EMAIL, email)
            param(Params.SESSION_DURATION, durationSeconds)
        }
    }

    fun logScreenView(screenName: String) {
        logEvent(Events.SCREEN_VIEW) {
            param(Params.SCREEN_NAME, screenName)
        }
    }

    fun logButtonClick(buttonName: String, screenName: String? = null) {
        logEvent(Events.BUTTON_CLICKED) {
            param(Params.BUTTON_NAME, buttonName)
            screenName?.let { param(Params.SCREEN_NAME, it) }
        }
    }

    private fun logEvent(eventName: String, paramsBuilder: Bundle.() -> Unit = {}) {
        val bundle = Bundle().apply(paramsBuilder)
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    private fun Bundle.param(key: String, value: String) {
        putString(key, value)
    }

    private fun Bundle.param(key: String, value: Long) {
        putLong(key, value)
    }

    fun setUserProperty(propertyName: String, value: String) {
        firebaseAnalytics.setUserProperty(propertyName, value)
    }

    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }
}
