package com.stepsync.presentation.social

import androidx.compose.foundation. layout.*
import androidx.compose. foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy. items
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material. icons.filled.ArrowBack
import androidx.compose.material. icons.filled.Check
import androidx.compose.material. icons.filled.Close
import androidx.compose.material. icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui. Alignment
import androidx.compose. ui.Modifier
import androidx. compose.ui.text.input.KeyboardType
import androidx. compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.stepsync.data.model.Friend
import com.stepsync.data.model.LeaderboardEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    viewModel: SocialViewModel,
    onNavigateBack: () -> Unit
) {
    val friends by viewModel.friends.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for success/error messages
    LaunchedEffect(uiState) {
        when (uiState) {
            is SocialUiState.Success -> {
                snackbarHostState. showSnackbar((uiState as SocialUiState.Success).message)
                viewModel.resetUiState()
            }
            is SocialUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as SocialUiState.Error).message)
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Social") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default. ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFriendDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Friend")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Friends (${friends.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Requests (${pendingRequests.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Leaderboard") }
                )
            }

            when (selectedTab) {
                0 -> FriendsTab(friends, viewModel)
                1 -> RequestsTab(pendingRequests, viewModel)
                2 -> LeaderboardTab(viewModel)
            }
        }
    }

    // Add Friend Dialog
    if (showAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { showAddFriendDialog = false },
            onAddFriend = { email ->
                viewModel.addFriend(email)
                showAddFriendDialog = false
            },
            isLoading = uiState is SocialUiState.Loading
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onAddFriend: (String) -> Unit,
    isLoading: Boolean
) {
    var friendCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (! isLoading) onDismiss() },
        title = { Text("Add Friend") },
        text = {
            Column(
                verticalArrangement = Arrangement. spacedBy(8.dp)
            ) {
                Text(
                    text = "Enter your friend's code",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = friendCode,
                    onValueChange = {
                        // Auto-format and uppercase
                        friendCode = it.uppercase().take(9)
                        codeError = null
                    },
                    label = { Text("Friend Code") },
                    placeholder = { Text("STEP-XXXX") },
                    singleLine = true,
                    isError = codeError != null,
                    supportingText = codeError?. let { { Text(it) } },
                    enabled = !isLoading,
                    modifier = Modifier. fillMaxWidth()
                )

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        friendCode.isBlank() -> {
                            codeError = "Friend code is required"
                        }
                        ! friendCode.matches(Regex("STEP-[A-Z0-9]{4}")) -> {
                            codeError = "Invalid friend code format (STEP-XXXX)"
                        }
                        else -> {
                            onAddFriend(friendCode. trim())
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FriendsTab(friends: List<Friend>, viewModel:  SocialViewModel) {
    if (friends.isEmpty()) {
        Box(
            modifier = Modifier. fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "No friends yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap + to add friends! ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(friends) { friend ->
                FriendItem(friend, viewModel)
            }
        }
    }
}

@Composable
fun FriendItem(friend: Friend, viewModel: SocialViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier. fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement. spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = friend.friendName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = friend.friendEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Friend",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Friend") },
            text = { Text("Are you sure you want to remove ${friend.friendName} from your friends?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeFriend(friend.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RequestsTab(requests: List<Friend>, viewModel: SocialViewModel) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No pending requests",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests) { request ->
                FriendRequestItem(request, viewModel)
            }
        }
    }
}

@Composable
fun FriendRequestItem(request: Friend, viewModel: SocialViewModel) {
    Card(
        modifier = Modifier. fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement. spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = request.friendName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = request.friendEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme. onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { viewModel. acceptFriendRequest(request.id) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { viewModel.removeFriend(request.id) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Decline",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardTab(viewModel: SocialViewModel) {
    val globalLeaderboard by viewModel.globalLeaderboard.collectAsState()

    if (globalLeaderboard.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment. Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Loading leaderboard.. .",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                . fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement. spacedBy(8.dp)
        ) {
            items(globalLeaderboard) { entry ->
                LeaderboardItem(entry)
            }
        }
    }
}

@Composable
fun LeaderboardItem(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (entry.isCurrentUser) {
            CardDefaults. cardColors(
                containerColor = MaterialTheme.colorScheme. primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank
                Text(
                    text = "#${entry.rank}",
                    style = MaterialTheme.typography.titleLarge,
                    color = when (entry.rank) {
                        1 -> MaterialTheme. colorScheme.primary
                        2 -> MaterialTheme.colorScheme.secondary
                        3 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                // User info
                Column {
                    Text(
                        text = if (entry.isCurrentUser) "${entry.userName} (You)" else entry.userName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${entry.steps} steps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme. onSurfaceVariant
                    )
                }
            }
        }
    }
}