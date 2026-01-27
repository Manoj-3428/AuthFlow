package com.example.authflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.authflow.viewmodel.AuthEvent
import com.example.authflow.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AuthFlow",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF30364F),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Passwordless Authentication",
            fontSize = 16.sp,
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.Black) },
            placeholder = { Text("Enter your email", color = Color.Black.copy(alpha = 0.5f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF30364F),
                unfocusedBorderColor = Color(0xFF30364F).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFF30364F),
                unfocusedLabelColor = Color.Black.copy(alpha = 0.6f),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        
        Button(
            onClick = { viewModel.handleEvent(AuthEvent.SendOtp(email.trim())) },
            enabled = email.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF30364F),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF30364F).copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = "Send OTP",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
