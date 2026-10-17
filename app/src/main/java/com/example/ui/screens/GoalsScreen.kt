package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GoalCategory
import com.example.data.model.SavingsGoalEntity
import com.example.ui.components.SavingsGoalTrackerComponent

@Composable
fun GoalsScreen(
    goals: List<SavingsGoalEntity>,
    onAddGoalClick: () -> Unit,
    onAddGoal: (name: String, targetAmount: Double, initialDeposit: Double, targetMonths: Int, category: GoalCategory, notes: String) -> Unit,
    onAddFundsClick: (SavingsGoalEntity) -> Unit,
    onAddFundsAmount: (SavingsGoalEntity, Double) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGoalClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .testTag("fab_add_goal")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Savings Goal"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("goals_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Savings & Goals 🎯",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Set targets, visualize milestones with progress bars, and build your wealth.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // SavingsGoalTrackerComponent
            item {
                SavingsGoalTrackerComponent(
                    goals = goals,
                    onAddGoal = onAddGoal,
                    onAddFunds = onAddFundsAmount,
                    onDeleteGoal = onDeleteGoal
                )
            }
        }
    }
}
