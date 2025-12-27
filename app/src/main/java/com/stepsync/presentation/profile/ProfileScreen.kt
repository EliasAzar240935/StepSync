package com. stepsync.presentation. profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose. material. icons.filled.ContentCopy
import androidx.compose. material. icons.filled.ExitToApp
import androidx.compose.material. icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui. Alignment
import androidx.compose. ui.Modifier
import androidx. compose.ui.platform.LocalContext
import androidx.compose.ui. text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stepsync.data.model.User

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
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
                onLogout = {
                    viewModel.logout()
                    onNavigateToLogin()
                },
                onNavigateBack = onNavigateBack
            )
        }
        is ProfileUiState.Error -> {
            val message = (uiState as ProfileUiState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment. Center) {
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
    user: User,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showCopiedMessage by remember { mutableStateOf(false) }

    // Show snackbar when code is copied
    LaunchedEffect(showCopiedMessage) {
        if (showCopiedMessage) {
            snackbarHostState.showSnackbar(
                message = "Friend code copied! ",
                duration = SnackbarDuration. Short
            )
            showCopiedMessage = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme. onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Avatar/Icon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier. size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // User Name
            Text(
                text = user.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Friend Code Card - PROMINENT DISPLAY
            Card(
                modifier = Modifier. fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your Friend Code",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.friendCode. ifEmpty { "Loading..." },
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier. width(8.dp))

                        IconButton(
                            onClick = {
                                copyToClipboard(context, user.friendCode)
                                showCopiedMessage = true
                            },
                            enabled = user.friendCode. isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default. ContentCopy,
                                contentDescription = "Copy Friend Code",
                                tint = MaterialTheme.colorScheme. primary
                            )
                        }
                    }

                    Text(
                        text = "Share this code with friends to connect! ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Personal Information Card
            Card(
                modifier = Modifier. fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier. padding(16.dp),
                    verticalArrangement = Arrangement. spacedBy(8.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ProfileInfoRow(label = "Email", value = user.email)
                    Divider()
                    ProfileInfoRow(label = "Age", value = "${user.age} years")
                    Divider()
                    ProfileInfoRow(label = "Weight", value = "${user.weight} kg")
                    Divider()
                    ProfileInfoRow(label = "Height", value = "${user.height} cm")
                    Divider()
                    ProfileInfoRow(label = "Daily Step Goal", value = "${user.dailyStepGoal} steps")
                    Divider()
                    ProfileInfoRow(label = "Fitness Goal", value = formatFitnessGoal(user.fitnessGoal))
                }
            }

            Spacer(modifier = Modifier. weight(1f))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context. CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Friend Code", text)
    clipboard.setPrimaryClip(clip)
}

private fun formatFitnessGoal(goal: String): String {
    return when (goal) {
        "weight_loss" -> "Weight Loss"
        "muscle_gain" -> "Muscle Gain"
        "fitness" -> "General Fitness"
        "health" -> "Health Improvement"
        else -> goal. replaceFirstChar { it.uppercase() }
    }
}