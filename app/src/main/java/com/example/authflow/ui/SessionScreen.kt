package com.example.authflow.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.authflow.R
import com.example.authflow.viewmodel.AuthEvent
import com.example.authflow.viewmodel.AuthState
import com.example.authflow.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    state: AuthState.SessionActive,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val sessionStartTimeFormatted = remember(state.sessionStartTime) {
        timeFormat.format(Date(state.sessionStartTime))
    }

    val sessionStartDateFormatted = remember(state.sessionStartTime) {
        dateFormat.format(Date(state.sessionStartTime))
    }

    val durationMinutes = state.sessionDurationSeconds / 60
    val durationSeconds = state.sessionDurationSeconds % 60
    val durationFormatted = String.format("%02d:%02d", durationMinutes, durationSeconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF30364F),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.White,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.authflowicon),
                contentDescription = "AuthFlow Icon",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "Session Active",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF30364F),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = state.email,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF30364F)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Session Duration",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = durationFormatted,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Start Time",
                            fontSize = 14.sp,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                        Text(
                            text = sessionStartTimeFormatted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Start Date",
                            fontSize = 14.sp,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                        Text(
                            text = sessionStartDateFormatted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.handleEvent(AuthEvent.Logout) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF30364F),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
