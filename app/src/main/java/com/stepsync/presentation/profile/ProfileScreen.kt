package com.stepsync.presentation.profile

import androidx.compose.foundation. layout.*
import androidx.compose. material. icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com. stepsync.data.model. User
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose. material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx. compose.material3.TopAppBarDefaults
import androidx.compose. material3.IconButton

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit  // ✅ Add this parameter
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is ProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ProfileUiState.NotAuthenticated -> {
            LaunchedEffect(Unit) {
                onNavigateToLogin()
            }
        }
        is ProfileUiState.Success -> {
            val user = (uiState as ProfileUiState.Success).user
            ProfileContent(
                user = user,
                onLogout = { viewModel.logout() },
                onNavigateBack = onNavigateBack  // ✅ Pass it here
            )
        }
        is ProfileUiState.Error -> {
            val message = (uiState as ProfileUiState. Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateToLogin) {
                        Text("Go to Login")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    user:  User,
    onLogout:  () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier:  Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Avatar/Icon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // User Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileInfoRow(label = "Name", value = user. name)
                    Spacer(modifier = Modifier. height(8.dp))
                    ProfileInfoRow(label = "Email", value = user.email)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileInfoRow(label = "Friend Code", value = user.friendCode)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileInfoRow(label = "Age", value = user.age. toString())
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileInfoRow(label = "Weight", value = "${user.weight} kg")
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileInfoRow(label = "Height", value = "${user.height} cm")
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileInfoRow(label = "Fitness Goal", value = user.fitnessGoal)
                }
            }

            Spacer(modifier = Modifier. weight(1f))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults. buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Logout")
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    modifier:  Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement. SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight. Bold
        )
    }
}