package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.FinanceCalculator
import com.example.domain.FinancialSnapshot
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SafeGreen

@Composable
fun MonthlyBudgetOverviewCard(
    snapshot: FinancialSnapshot,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = snapshot.budgetProgressFraction,
        animationSpec = tween(durationMillis = 800),
        label = "budgetProgress"
    )

    val progressColor = when {
        snapshot.budgetProgressFraction >= 1f -> CriticalRed
        snapshot.budgetProgressFraction >= 0.8f -> CautionAmber
        else -> SafeGreen
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_budget_overview_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Spending Pace",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(snapshot.budgetProgressFraction * 100).toInt()}% Used",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(progressColor)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary 4-grid stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetMetricItem(
                    label = "Total Income",
                    value = FinanceCalculator.formatINR(snapshot.monthlyIncome),
                    color = MaterialTheme.colorScheme.primary
                )
                BudgetMetricItem(
                    label = "Fixed & Bills",
                    value = FinanceCalculator.formatINR(snapshot.fixedExpenses),
                    color = BlueAccent
                )
                BudgetMetricItem(
                    label = "Saved Target",
                    value = FinanceCalculator.formatINR(snapshot.savingsTarget),
                    color = GoldAccent
                )
                BudgetMetricItem(
                    label = "Total Spent",
                    value = FinanceCalculator.formatINR(snapshot.totalSpentThisMonth),
                    color = progressColor
                )
            }
        }
    }
}

@Composable
private fun BudgetMetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = " $label",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
