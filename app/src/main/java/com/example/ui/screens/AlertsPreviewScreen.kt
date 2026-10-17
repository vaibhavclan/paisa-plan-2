package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.BudgetPacingStatus
import com.example.domain.FinanceCalculator
import com.example.domain.FinancialSnapshot
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SafeGreen

@Composable
fun AlertsPreviewScreen(
    snapshot: FinancialSnapshot,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("alerts_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Smart Alerts & Insights",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("alerts_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Proactive financial guards designed to keep you on budget without stress.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Live Alert based on current status
            item {
                val (liveTitle, liveDesc, liveIcon, liveColor) = when (snapshot.pacingStatus) {
                    BudgetPacingStatus.SAFE -> Quadruple(
                        "Pacing On Track ✅",
                        "You're currently spending within your safe limit of ${FinanceCalculator.formatINR(snapshot.safeToSpendToday)}/day. Keep it up!",
                        Icons.Default.TrendingUp,
                        SafeGreen
                    )
                    BudgetPacingStatus.CAUTION -> Quadruple(
                        "Caution: Elevated Spending Pace ⚠️",
                        "Today's spend (${FinanceCalculator.formatINR(snapshot.spentToday)}) is outpacing your safe rate. Free cash remaining is ${FinanceCalculator.formatINR(snapshot.remainingDiscretionary)}.",
                        Icons.Default.Warning,
                        CautionAmber
                    )
                    BudgetPacingStatus.CRITICAL -> Quadruple(
                        "Discretionary Budget Depleted 🚨",
                        "Your variable spending has reached or exceeded the planned monthly allocation. Hold non-essential transactions until next month.",
                        Icons.Default.Warning,
                        CriticalRed
                    )
                }

                AlertCard(
                    title = liveTitle,
                    description = liveDesc,
                    badgeText = "CURRENT STATUS",
                    icon = liveIcon,
                    accentColor = liveColor
                )
            }

            item {
                AlertCard(
                    title = "80% Monthly Budget Threshold Alert",
                    description = "Triggers automatically when you've utilized 80% of your variable spending budget, leaving adequate cushion for the last week of the month.",
                    badgeText = "THRESHOLD GUARD",
                    icon = Icons.Default.NotificationsActive,
                    accentColor = CautionAmber
                )
            }

            item {
                AlertCard(
                    title = "Weekend Surge Allocation Rule",
                    description = "Spend ~15% less on Monday-Thursday to unlock a higher safe-spend allowance on Friday-Sunday for dining & weekend leisure.",
                    badgeText = "SMART PACING",
                    icon = Icons.Default.Lightbulb,
                    accentColor = MaterialTheme.colorScheme.primary
                )
            }

            item {
                AlertCard(
                    title = "Impulse Purchase 48-Hour Buffer",
                    description = "When logging an unplanned non-essential expense above ₹2,000, PaisaPlan calculates how many days of freedom it costs and prompts a cooling period.",
                    badgeText = "IMPULSE SHIELD",
                    icon = Icons.Default.Savings,
                    accentColor = GoldAccent
                )
            }

            item {
                AlertCard(
                    title = "SIP & Bill Cushion Notification",
                    description = "Locks ₹${(snapshot.fixedExpenses.toInt())} for rent, utilities, and mutual fund SIPs so you never accidentally overspend fixed commitments.",
                    badgeText = "FIXED COMMITMENT",
                    icon = Icons.Default.Info,
                    accentColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun AlertCard(
    title: String,
    description: String,
    badgeText: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
