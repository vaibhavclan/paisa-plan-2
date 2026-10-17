package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoalCategory
import com.example.data.model.SavingsGoalEntity
import com.example.domain.FinanceCalculator
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SafeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * A comprehensive Jetpack Compose component that allows users to:
 * 1. Set specific savings goals with target amounts, category icons, and deadlines.
 * 2. Visually track progress with high-contrast, animated milestone progress bars.
 * 3. Add savings deposits via quick-tap preset chips or custom amounts.
 */
@Composable
fun SavingsGoalTrackerComponent(
    goals: List<SavingsGoalEntity>,
    onAddGoal: (name: String, targetAmount: Double, initialDeposit: Double, targetMonths: Int, category: GoalCategory, notes: String) -> Unit,
    onAddFunds: (SavingsGoalEntity, Double) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSetGoalExpanded by remember { mutableStateOf(false) }

    val totalSaved = goals.sumOf { it.currentAmount }
    val totalTarget = goals.sumOf { it.targetAmount }
    val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f
    val completedCount = goals.count { it.isCompleted }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("savings_goal_tracker_component"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. OVERALL PROGRESS HERO CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("overall_goals_progress_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                GoldAccent.copy(alpha = 0.12f),
                                SafeGreen.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "SAVINGS GOALS PROGRESS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (completedCount > 0) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SafeGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = SafeGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$completedCount Done",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SafeGreen
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = FinanceCalculator.formatINR(totalSaved),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 32.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("total_goals_saved_amount")
                            )
                            Text(
                                text = "Target: ${FinanceCalculator.formatINR(totalTarget)} (${goals.size} active goals)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Percentage badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${(overallProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (overallProgress >= 1f) SafeGreen else GoldAccent
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animated Progress Bar with Milestone Markers
                    GoalMilestoneProgressBar(
                        progressFraction = overallProgress,
                        primaryColor = GoldAccent,
                        secondaryColor = SafeGreen
                    )
                }
            }
        }

        // 2. SET A NEW SAVINGS GOAL (INLINE FORM CARD)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("set_new_goal_container"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSetGoalExpanded = !isSetGoalExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Set a Specific Savings Goal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Define a target amount and timeframe",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { isSetGoalExpanded = !isSetGoalExpanded },
                        modifier = Modifier.testTag("toggle_set_goal_form")
                    ) {
                        Icon(
                            imageVector = if (isSetGoalExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand/Collapse Form"
                        )
                    }
                }

                AnimatedVisibility(visible = isSetGoalExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        var goalName by remember { mutableStateOf("") }
                        var targetAmountText by remember { mutableStateOf("") }
                        var initialDepositText by remember { mutableStateOf("") }
                        var selectedMonths by remember { mutableIntStateOf(6) }
                        var selectedCategory by remember { mutableStateOf(GoalCategory.EMERGENCY) }
                        var notes by remember { mutableStateOf("") }

                        val targetAmt = targetAmountText.toDoubleOrNull() ?: 0.0
                        val initialDep = initialDepositText.toDoubleOrNull() ?: 0.0
                        val monthlyPace = if (selectedMonths > 0 && targetAmt > initialDep) {
                            (targetAmt - initialDep) / selectedMonths
                        } else 0.0

                        OutlinedTextField(
                            value = goalName,
                            onValueChange = { goalName = it },
                            label = { Text("Goal Title") },
                            placeholder = { Text("e.g., Goa Trip, Emergency Fund, Laptop") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inline_goal_name_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = targetAmountText,
                                onValueChange = { targetAmountText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Target Amount") },
                                prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("inline_goal_target_input")
                            )

                            OutlinedTextField(
                                value = initialDepositText,
                                onValueChange = { initialDepositText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Initial Deposit") },
                                prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("inline_goal_deposit_input")
                            )
                        }

                        // Timeline Selector
                        Text(
                            text = "Target Timeframe",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1 to "1M", 3 to "3M", 6 to "6M", 12 to "1Y", 24 to "2Y").forEach { (m, label) ->
                                val isSelected = m == selectedMonths
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedMonths = m }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Category Selector
                        Text(
                            text = "Category Type",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = cat.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) GoldAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (monthlyPace > 0) {
                            Text(
                                text = "💡 Requires ~${FinanceCalculator.formatINR(monthlyPace)} / month",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Button(
                            onClick = {
                                if (goalName.isNotBlank() && targetAmt > 0) {
                                    onAddGoal(
                                        goalName.trim(),
                                        targetAmt,
                                        initialDep,
                                        selectedMonths,
                                        selectedCategory,
                                        notes.trim()
                                    )
                                    goalName = ""
                                    targetAmountText = ""
                                    initialDepositText = ""
                                    isSetGoalExpanded = false
                                }
                            },
                            enabled = goalName.isNotBlank() && targetAmt > 0,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_new_goal_button")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Goal & Start Tracking")
                        }
                    }
                }
            }
        }

        // 3. TRACKING LIST OF INDIVIDUAL SAVINGS GOALS WITH PROGRESS BARS
        Text(
            text = "Tracked Goals (${goals.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (goals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No savings goals tracked yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Set your first goal above to start tracking progress with custom milestones.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            goals.forEach { goal ->
                TrackedGoalItemCard(
                    goal = goal,
                    onAddDeposit = { amount -> onAddFunds(goal, amount) },
                    onDelete = { onDeleteGoal(goal) }
                )
            }
        }
    }
}

/**
 * Individual Goal Card with interactive progress bar, milestone indicators,
 * and inline quick-deposit pills.
 */
@Composable
fun TrackedGoalItemCard(
    goal: SavingsGoalEntity,
    onAddDeposit: (Double) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = GoalCategory.fromString(goal.categoryIcon)
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progressFraction,
        animationSpec = tween(durationMillis = 700),
        label = "itemProgress"
    )

    val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val targetDateStr = dateFormat.format(Date(goal.targetDateTimestamp))
    val remaining = max(0.0, goal.targetAmount - goal.currentAmount)
    val isAchieved = goal.isCompleted

    var showCustomDepositInput by remember { mutableStateOf(false) }
    var customDepositText by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tracked_goal_item_${goal.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Category Icon, Goal Title, Target Date, and Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAchieved) SafeGreen.copy(alpha = 0.15f)
                                else GoldAccent.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAchieved) Icons.Default.Check else category.icon,
                            contentDescription = null,
                            tint = if (isAchieved) SafeGreen else GoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target Date: $targetDateStr • ${category.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_tracked_goal_${goal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Goal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Metrics Numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Current Savings",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FinanceCalculator.formatINR(goal.currentAmount),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAchieved) SafeGreen else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target: ${FinanceCalculator.formatINR(goal.targetAmount)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(goal.progressFraction * 100).toInt()}% Achieved",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isAchieved) SafeGreen else GoldAccent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // VISUAL PROGRESS BAR WITH MILESTONE MARKERS
            GoalMilestoneProgressBar(
                progressFraction = animatedProgress,
                primaryColor = if (isAchieved) SafeGreen else GoldAccent,
                secondaryColor = SafeGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status message
            if (!isAchieved) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${FinanceCalculator.formatINR(remaining)} remaining",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "+ Quick Deposit",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.clickable { showCustomDepositInput = !showCustomDepositInput }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Tap Deposit Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500.0, 1000.0, 2000.0, 5000.0).forEach { depositVal ->
                        SuggestionChip(
                            onClick = { onAddDeposit(depositVal) },
                            label = {
                                Text(
                                    text = "+₹${depositVal.toInt()}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_deposit_${goal.id}_${depositVal.toInt()}")
                        )
                    }
                }

                // Custom Deposit Input Accordion
                AnimatedVisibility(visible = showCustomDepositInput) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customDepositText,
                            onValueChange = { customDepositText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Custom Amount") },
                            prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                val amt = customDepositText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    onAddDeposit(amt)
                                    customDepositText = ""
                                    showCustomDepositInput = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("Deposit")
                        }
                    }
                }
            } else {
                // Completed Celebration Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafeGreen.copy(alpha = 0.12f))
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = SafeGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Target Met! Milestone Achieved 🎉",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SafeGreen
                        )
                    )
                }
            }
        }
    }
}

/**
 * Visual Progress Bar with gradient filling and milestone tick markers (25%, 50%, 75%).
 */
@Composable
fun GoalMilestoneProgressBar(
    progressFraction: Float,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val clampedProgress = progressFraction.coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Filled Gradient Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(clampedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(primaryColor, secondaryColor)
                        )
                    )
            )

            // Milestone divider tick markers at 25%, 50%, 75%
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(0.25f, 0.5f, 0.75f).forEach { _ ->
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    )
                }
            }
        }

        // Milestone Labels Below Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("25%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("50%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("75%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("100%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
