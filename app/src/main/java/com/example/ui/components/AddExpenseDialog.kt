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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMode
import com.example.data.model.RecurrenceFrequency
import com.example.domain.FinanceCalculator
import com.example.domain.FinancialSnapshot
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.CautionAmberBg
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    snapshot: FinancialSnapshot,
    onDismiss: () -> Unit,
    onConfirm: (
        amount: Double,
        category: ExpenseCategory,
        merchantOrNote: String,
        paymentMode: String,
        timestamp: Long,
        isRecurring: Boolean,
        recurrenceFrequency: String
    ) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var amountTouched by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var noteText by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.UPI) }

    // Recurring state
    var isRecurring by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf(RecurrenceFrequency.MONTHLY) }

    // Date state
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val isAmountValid = amountValue > 0.0
    val isAmountError = amountTouched && !isAmountValid

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val displayDate = remember(selectedTimestamp) { dateFormat.format(Date(selectedTimestamp)) }

    // Calculate real-time impact
    val willExceedToday = snapshot.safeToSpendToday > 0 && amountValue > snapshot.safeToSpendToday
    val newSafeToSpend = if (snapshot.daysRemainingInMonth > 0) {
        ((snapshot.remainingDiscretionary - amountValue) / snapshot.daysRemainingInMonth).coerceAtLeast(0.0)
    } else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Expense",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. AMOUNT FIELD WITH QUICK CHIPS
                Column {
                    Text(
                        text = "AMOUNT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { input ->
                            amountTouched = true
                            if (input.isEmpty() || input.matches(Regex("""^\d*(\.\d{0,2})?$"""))) {
                                amountText = input
                            }
                        },
                        label = { Text("Amount (₹)") },
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (amountText.isNotEmpty()) {
                                IconButton(onClick = { amountText = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Amount")
                                }
                            }
                        },
                        isError = isAmountError,
                        supportingText = {
                            if (isAmountError) {
                                Text("Please enter a valid amount", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_expense_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Quick increment chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(50, 100, 200, 500, 1000).forEach { quickVal ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        amountTouched = true
                                        val current = amountText.toDoubleOrNull() ?: 0.0
                                        amountText = (current + quickVal).toInt().toString()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "+₹$quickVal",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 2. CATEGORY FIELD
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedCategory.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = selectedCategory.color
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExpenseCategory.entries.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) cat.color.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) cat.color else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cat.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 3. DATE FIELD (INCLUDING DATE PICKER & QUICK SHORTCUTS)
                Column {
                    Text(
                        text = "TRANSACTION DATE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = displayDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePickerDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePickerDialog = true }
                            .testTag("dialog_expense_date_field")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Date quick shortcuts: Today, Yesterday, 2 Days Ago
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0 to "Today", 1 to "Yesterday", 2 to "2 Days Ago").forEach { (daysAgo, label) ->
                            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
                            val isSelected = remember(selectedTimestamp, daysAgo) {
                                val sCal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                sCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) &&
                                sCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                            }
                            AssistChip(
                                onClick = { selectedTimestamp = cal.timeInMillis },
                                label = { Text(label, fontSize = 11.sp) },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                } else null,
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }
                }

                // 4. MERCHANT / DESCRIPTION FIELD
                Column {
                    Text(
                        text = "MERCHANT / NOTE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Merchant / Description") },
                        placeholder = { Text("e.g. Swiggy, Metro, DMart") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_expense_note_input")
                    )
                }

                // 5. PAYMENT MODE FIELD
                Column {
                    Text(
                        text = "PAYMENT METHOD",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PaymentMode.entries.forEach { mode ->
                            val isSelected = mode == selectedPaymentMode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedPaymentMode = mode }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mode.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 6. RECURRING EXPENSE SECTION
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Recurring Expense",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Mark as weekly, monthly, etc.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            modifier = Modifier.testTag("dialog_recurring_switch")
                        )
                    }

                    if (isRecurring) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "FREQUENCY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                RecurrenceFrequency.WEEKLY,
                                RecurrenceFrequency.MONTHLY,
                                RecurrenceFrequency.DAILY,
                                RecurrenceFrequency.YEARLY
                            ).forEach { freq ->
                                val isSelected = freq == selectedFrequency
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { selectedFrequency = freq }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("dialog_freq_chip_${freq.name.lowercase()}")
                                ) {
                                    Text(
                                        text = freq.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Impact Note
                if (amountValue > 0) {
                    val impactBg = if (willExceedToday) CautionAmberBg else SafeGreenBg
                    val impactColor = if (willExceedToday) CautionAmber else SafeGreen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(impactBg)
                            .border(1.dp, impactColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (willExceedToday) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = null,
                            tint = impactColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (willExceedToday) {
                                "Exceeds safe daily rate. Adjusts to ${FinanceCalculator.formatINR(newSafeToSpend)}/day."
                            } else {
                                "Within budget. Remaining safe: ${FinanceCalculator.formatINR(newSafeToSpend)}/day."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = impactColor
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amountTouched = true
                    if (isAmountValid) {
                        val finalNote = noteText.trim().ifBlank { selectedCategory.displayName }
                        onConfirm(
                            amountValue,
                            selectedCategory,
                            finalNote,
                            selectedPaymentMode.displayName,
                            selectedTimestamp,
                            isRecurring,
                            if (isRecurring) selectedFrequency.name else "NONE"
                        )
                    }
                },
                enabled = isAmountValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("dialog_submit_expense_button")
            ) {
                Text("Save to Database")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_cancel_expense_button")
            ) {
                Text("Cancel")
            }
        }
    )

    // Material 3 Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedTimestamp)
        val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedTimestamp = it
                        }
                        showDatePickerDialog = false
                    },
                    enabled = confirmEnabled
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
