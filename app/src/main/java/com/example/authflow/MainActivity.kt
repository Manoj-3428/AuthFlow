package com.example.authflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.authflow.ui.LoginScreen
import com.example.authflow.ui.OtpScreen
import com.example.authflow.ui.SessionScreen
import com.example.authflow.ui.theme.AuthFlowTheme
import com.example.authflow.viewmodel.AuthState
import com.example.authflow.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuthFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    val viewModel: AuthViewModel = viewModel()
                    val state by viewModel.state.collectAsState()
                    
                    when (val currentState = state) {
                        is AuthState.EmailInput -> {
                            LoginScreen(viewModel = viewModel)
                        }
                        is AuthState.OtpSent,
                        is AuthState.OtpVerifying,
                        is AuthState.OtpError -> {
                            OtpScreen(state = currentState, viewModel = viewModel)
                        }
                        is AuthState.SessionActive -> {
                            SessionScreen(state = currentState, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}