package com.stepsync.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stepsync.data.model.StepRecord
import com.stepsync.util.CalculationUtils
import com.stepsync.util.Constants

/**
 * Home screen showing step count and progress
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToSocial: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val todaySteps by viewModel.todaySteps.collectAsState()
    val recentSteps by viewModel.recentSteps.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StepSync") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToActivity,
                    icon = { Icon(Icons.Default.DirectionsRun, contentDescription = "Activity") },
                    label = { Text("Activity") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToGoals,
                    icon = { Icon(Icons.Default.Flag, contentDescription = "Goals") },
                    label = { Text("Goals") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSocial,
                    icon = { Icon(Icons.Default.People, contentDescription = "Social") },
                    label = { Text("Social") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                currentUser?.let { user ->
                    Text(
                        text = "Welcome, ${user.name}!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Today's Steps",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${todaySteps?.steps ?: 0}",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val goal = currentUser?.dailyStepGoal ?: Constants.DEFAULT_DAILY_STEP_GOAL
                        val progress = (todaySteps?.steps ?: 0).toFloat() / goal.toFloat()
                        
                        LinearProgressIndicator(
                            progress = progress.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Goal: $goal steps",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = CalculationUtils.formatDistance(todaySteps?.distance ?: 0f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Distance",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = CalculationUtils.formatCalories(todaySteps?.calories ?: 0f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Calories",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(recentSteps) { record ->
                StepRecordItem(record)
            }
        }
    }
}

@Composable
fun StepRecordItem(record: StepRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = record.date,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${record.steps} steps",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = CalculationUtils.formatDistance(record.distance),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = CalculationUtils.formatCalories(record.calories),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
