package com.example.authflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.authflow.viewmodel.AuthEvent
import com.example.authflow.viewmodel.AuthState
import com.example.authflow.viewmodel.AuthViewModel
import com.example.authflow.viewmodel.OtpErrorType

@Composable
fun OtpScreen(
    state: AuthState,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var otp by remember { mutableStateOf("") }
    var canResend by remember { mutableStateOf(false) }
    var showErrorState by remember { mutableStateOf(false) }
    var lastOtpLength by remember { mutableStateOf(0) }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val email = when (state) {
        is AuthState.OtpSent -> state.email
        is AuthState.OtpVerifying -> state.email
        is AuthState.OtpError -> state.email
        else -> ""
    }

    val otpCode = when (state) {
        is AuthState.OtpSent -> state.otpCode
        is AuthState.OtpVerifying -> state.otpCode
        is AuthState.OtpError -> state.otpCode
        else -> null
    }

    val remainingSeconds = when (state) {
        is AuthState.OtpSent -> state.remainingSeconds
        is AuthState.OtpVerifying -> state.remainingSeconds
        else -> 0L
    }

    LaunchedEffect(remainingSeconds, state) {
        canResend = when {
            remainingSeconds <= 0 -> true
            state is AuthState.OtpError && state.errorType == OtpErrorType.Expired -> true
            state is AuthState.OtpError && state.errorType == OtpErrorType.MaxAttemptsExceeded -> true
            else -> false
        }
    }

    // Auto-focus next field when OTP length changes
    LaunchedEffect(otp.length) {
        if (otp.length > lastOtpLength && otp.length < 6) {
            // OTP length increased, move to next field immediately
            focusRequesters[otp.length].requestFocus()
        } else if (otp.length < lastOtpLength) {
            // OTP length decreased (backspace), move to previous field
            val targetIndex = if (otp.length > 0) otp.length - 1 else 0
            focusRequesters[targetIndex].requestFocus()
        }
        lastOtpLength = otp.length
    }
    
    LaunchedEffect(state) {
        when (state) {
            is AuthState.OtpSent -> {
                if (otp.isEmpty()) {
                    focusRequesters[0].requestFocus()
                }
                showErrorState = false
            }

            is AuthState.OtpError -> {
                when (state.errorType) {
                    OtpErrorType.Incorrect -> {
                        showErrorState = true
                        snackbarHostState.showSnackbar("Not verified")
                    }

                    OtpErrorType.Expired -> {
                        showErrorState = false
                        snackbarHostState.showSnackbar("OTP has expired. Please resend.")
                    }

                    OtpErrorType.MaxAttemptsExceeded -> {
                        showErrorState = false
                        snackbarHostState.showSnackbar("Maximum attempts exceeded. Please resend OTP.")
                    }

                    OtpErrorType.NotFound -> {
                        showErrorState = false
                        snackbarHostState.showSnackbar("OTP not found. Please resend.")
                    }
                }
            }

            is AuthState.OtpVerifying -> {
                showErrorState = false
            }

            else -> {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Enter OTP",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF30364F),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Sent to $email",
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (otpCode != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF30364F)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your OTP Code",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = otpCode,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 4.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val digit = if (index < otp.length) otp[index].toString() else ""
                    val borderColor = if (showErrorState) Color.Red else Color(0xFF30364F)

                    BasicTextField(
                        value = digit,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1 && (newValue.isEmpty() || newValue.all { it.isDigit() })) {
                                showErrorState = false
                                val currentOtp = otp.toMutableList()

                                if (newValue.isEmpty()) {
                                    if (index < otp.length) {
                                        currentOtp.removeAt(index)
                                        otp = currentOtp.joinToString("")
                                    }
                                } else {
                                    val char = newValue[0]
                                    if (index < currentOtp.size) {
                                        currentOtp[index] = char
                                    } else {
                                        currentOtp.add(char)
                                    }
                                    otp = currentOtp.joinToString("")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .focusRequester(focusRequesters[index]),
                        textStyle = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        cursorBrush = SolidColor(Color(0xFF30364F)),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 1.5.dp,
                                        color = if (showErrorState) Color.Red else Color(0xFF30364F),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }
                        }
                    )

                }
            }

            if (state is AuthState.OtpError) {
                val errorMessage = when (state.errorType) {
                    OtpErrorType.Incorrect -> "Incorrect OTP. Please try again."
                    OtpErrorType.Expired -> "OTP has expired. Please resend."
                    OtpErrorType.MaxAttemptsExceeded -> "Maximum attempts exceeded. Please resend OTP."
                    OtpErrorType.NotFound -> "OTP not found. Please resend."
                }

                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = Color(0xFF30364F),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (remainingSeconds > 0) {
                Text(
                    text = "Resend OTP in ${remainingSeconds}s",
                    fontSize = 14.sp,
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        otp = ""
                        showErrorState = false
                        viewModel.handleEvent(AuthEvent.ResendOtp)
                        focusRequesters[0].requestFocus()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Resending OTP...")
                        }
                    },
                    enabled = canResend,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF30364F),
                        disabledContentColor = Color(0xFF30364F).copy(alpha = 0.5f)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = canResend).copy(
                        brush = SolidColor(Color(0xFF30364F)),
                        width = 1.dp
                    )
                ) {
                    Text(
                        text = "Resend",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (otp.length == 6) {
                                if (showErrorState) {
                                    snackbarHostState.showSnackbar("Not verified")
                                } else {
                                    viewModel.handleEvent(AuthEvent.VerifyOtp(otp))
                                    snackbarHostState.showSnackbar("Verifying OTP...")
                                }
                            } else {
                                snackbarHostState.showSnackbar("Please enter 6-digit OTP")
                            }
                        }
                    },
                    enabled = otp.length == 6 && state !is AuthState.OtpVerifying,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF30364F),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF30364F).copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    if (state is AuthState.OtpVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Verify",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
