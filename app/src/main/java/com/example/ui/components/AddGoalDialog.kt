package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.GoalCategory
import com.example.domain.FinanceCalculator
import com.example.ui.theme.GoldAccent

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Double, initialDeposit: Double, targetMonths: Int, category: GoalCategory, notes: String) -> Unit
) {
    var goalName by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var initialDepositText by remember { mutableStateOf("") }
    var selectedMonths by remember { mutableIntStateOf(6) }
    var selectedCategory by remember { mutableStateOf(GoalCategory.EMERGENCY) }
    var notesText by remember { mutableStateOf("") }

    val targetAmount = targetAmountText.toDoubleOrNull() ?: 0.0
    val initialDeposit = initialDepositText.toDoubleOrNull() ?: 0.0

    // Monthly contribution needed
    val monthlyNeeded = if (selectedMonths > 0 && targetAmount > initialDeposit) {
        (targetAmount - initialDeposit) / selectedMonths
    } else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Savings Goal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = goalName,
                    onValueChange = { goalName = it },
                    label = { Text("Goal Name") },
                    placeholder = { Text("e.g. Goa Trip, New Laptop, Bike") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Target Amount (₹)") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_target_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = initialDepositText,
                    onValueChange = { initialDepositText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Initial Deposit (Optional)") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_initial_deposit_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Timeline selection
                Text(
                    text = "Target Timeline",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        1 to "1 Month",
                        3 to "3 Months",
                        6 to "6 Months",
                        12 to "1 Year",
                        24 to "2 Years"
                    ).forEach { (months, label) ->
                        val isSelected = months == selectedMonths
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) GoldAccent.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) GoldAccent else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedMonths = months }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) GoldAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (monthlyNeeded > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "💡 Requires ~${FinanceCalculator.formatINR(monthlyNeeded)} / month to achieve",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = "Goal Type",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoalCategory.entries.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Why are you saving for this?") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (goalName.isNotBlank() && targetAmount > 0) {
                        onConfirm(goalName.trim(), targetAmount, initialDeposit, selectedMonths, selectedCategory, notesText.trim())
                    }
                },
                enabled = goalName.isNotBlank() && targetAmount > 0,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_goal_button")
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_goal_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
