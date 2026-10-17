package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.BudgetPacingStatus
import com.example.domain.FinanceCalculator
import com.example.domain.FinancialSnapshot
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.CautionAmberBg
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CriticalRedBg
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenBg

@Composable
fun SafeSpendCard(
    snapshot: FinancialSnapshot,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (snapshot.pacingStatus) {
            BudgetPacingStatus.SAFE -> SafeGreen
            BudgetPacingStatus.CAUTION -> CautionAmber
            BudgetPacingStatus.CRITICAL -> CriticalRed
        },
        label = "statusColor"
    )

    val statusBg by animateColorAsState(
        targetValue = when (snapshot.pacingStatus) {
            BudgetPacingStatus.SAFE -> SafeGreenBg
            BudgetPacingStatus.CAUTION -> CautionAmberBg
            BudgetPacingStatus.CRITICAL -> CriticalRedBg
        },
        label = "statusBg"
    )

    val statusIcon = when (snapshot.pacingStatus) {
        BudgetPacingStatus.SAFE -> Icons.Default.CheckCircle
        BudgetPacingStatus.CAUTION -> Icons.Default.Warning
        BudgetPacingStatus.CRITICAL -> Icons.Default.ErrorOutline
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("safe_spend_card"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top header row: Title & Pacing Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SAFE-TO-SPEND TODAY",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${snapshot.daysRemainingInMonth} days left this month",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Status Pill
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(statusBg)
                            .border(1.dp, statusColor.copy(alpha = 0.3f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = snapshot.pacingStatus.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Number
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = FinanceCalculator.formatINR(snapshot.safeToSpendToday),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("safe_spend_today_amount")
                    )
                    Text(
                        text = " / day",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sub-stats bar: Spent Today and Remaining Monthly Discretionary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Spent Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FinanceCalculator.formatINR(snapshot.spentToday),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (snapshot.safeToSpendToday > 0 && snapshot.spentToday > snapshot.safeToSpendToday) {
                                CautionAmber
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Free Cash Left (Month)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FinanceCalculator.formatINR(snapshot.remainingDiscretionary),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (snapshot.remainingDiscretionary >= 0) SafeGreen else CriticalRed
                        )
                    }
                }
            }
        }
    }
}
