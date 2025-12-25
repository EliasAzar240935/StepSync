package com.stepsync.presentation. social

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy. items
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material. icons.filled.ArrowBack
import androidx.compose.material. icons.filled.ContentCopy
import androidx.compose.material. icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui. Alignment
import androidx.compose. ui.Modifier
import androidx. compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text. AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stepsync.data.model.Friend
import androidx.compose.foundation.lazy.itemsIndexed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    viewModel: SocialViewModel,
    onNavigateBack: () -> Unit
) {
    val friends by viewModel.friends.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val currentUser by viewModel.currentUser. collectAsState()
    val message by viewModel.message.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when message changes
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState. showSnackbar(it)
            viewModel.clearMessage()
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
            // My Friend Code Card
            currentUser?.let { user ->
                MyFriendCodeCard(friendCode = user.friendCode)
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Friends") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Requests") }
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

        // Add Friend Dialog
        if (showAddFriendDialog) {
            AddFriendDialog(
                onDismiss = { showAddFriendDialog = false },
                onAddFriend = { code ->
                    viewModel.addFriendByCode(code)
                    showAddFriendDialog = false
                }
            )
        }
    }
}

@Composable
fun MyFriendCodeCard(friendCode: String) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme. colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your Friend Code",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = friendCode. ifEmpty { "Loading..." },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (friendCode.isNotEmpty()) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(friendCode))
                        android.widget.Toast.makeText(context, "Friend code copied!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy Friend Code",
                        tint = MaterialTheme.colorScheme. onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onAddFriend: (String) -> Unit
) {
    var friendCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                        friendCode = it. uppercase()
                        errorMessage = ""
                    },
                    label = { Text("Friend Code") },
                    placeholder = { Text("STEP-A1B2C3") },
                    singleLine = true,
                    isError = errorMessage.isNotEmpty(),
                    modifier = Modifier. fillMaxWidth()
                )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "Example: STEP-A1B2C3",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        friendCode.isBlank() -> errorMessage = "Please enter a friend code"
                        friendCode.replace("-", "").length < 4 -> errorMessage = "Friend code too short"
                        else -> onAddFriend(friendCode. trim())
                    }
                }
            ) {
                Text("Send Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FriendsTab(friends: List<Friend>, viewModel: SocialViewModel) {
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
                    Icons.Default. Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme. onSurfaceVariant
                )
                Text(
                    text = "No friends yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap + to add friends and compete! ",
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
fun LeaderboardTab(viewModel: SocialViewModel) {
    val globalLeaderboard by viewModel.globalLeaderboard.collectAsState()

    if (globalLeaderboard.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment. Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Global Rankings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            itemsIndexed(globalLeaderboard) { index, entry ->
                GlobalLeaderboardItem(entry = entry, rank = index + 1)
            }
        }
    }
}

@Composable
fun GlobalLeaderboardItem(entry:  com.stepsync.data.model.LeaderboardEntry, rank: Int) {
    Card(
        modifier = Modifier. fillMaxWidth(),
        colors = if (entry.isCurrentUser) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        } else {
            CardDefaults. cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment. CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement. spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (rank) {
                        1 -> MaterialTheme.colorScheme.primary
                        2 -> MaterialTheme.colorScheme. secondary
                        3 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = "#$rank",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (rank <= 3) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column {
                    Text(
                        text = entry.userName + if (entry.isCurrentUser) " (You)" else "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (entry.isCurrentUser) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme. colorScheme.onSurface
                    )
                }
            }

            // Total Steps
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${entry.steps}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "total steps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
            verticalAlignment = Alignment. CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement. spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default. Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
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

            OutlinedButton(
                onClick = { showDeleteDialog = true }
            ) {
                Text("Remove")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Friend") },
            text = { Text("Are you sure you want to remove ${friend.friendName} from your friends?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeFriend(friend.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
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
fun FriendRequestItem(request:  Friend, viewModel: SocialViewModel) {
    Card(
        modifier = Modifier. fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment. CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment. CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default. Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
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
                horizontalArrangement = Arrangement. spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.removeFriend(request.id) }
                ) {
                    Text("Decline")
                }
                Button(
                    onClick = { viewModel.acceptFriendRequest(request.id) }
                ) {
                    Text("Accept")
                }
            }
        }
    }
}