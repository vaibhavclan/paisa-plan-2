package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Switch
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMode
import com.example.domain.FinanceCalculator
import com.example.domain.FinancialSnapshot
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.CautionAmberBg
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CriticalRedBg
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    snapshot: FinancialSnapshot,
    onBackClick: () -> Unit,
    onSaveExpense: (amount: Double, category: ExpenseCategory, merchantOrNote: String, paymentMode: String, timestamp: Long, isRecurring: Boolean, recurrenceFrequency: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    BackHandler(onBack = onBackClick)

    // Form States
    var amountInput by remember { mutableStateOf("") }
    var amountTouched by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var merchantOrNote by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.UPI) }

    // Recurring States
    var isRecurring by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf(com.example.data.model.RecurrenceFrequency.MONTHLY) }

    // Date state
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val amountValue = amountInput.toDoubleOrNull() ?: 0.0
    val isAmountValid = amountValue > 0.0
    val isAmountError = amountTouched && !isAmountValid

    // Impact Calculations
    val willExceedToday = snapshot.safeToSpendToday > 0 && amountValue > snapshot.safeToSpendToday
    val newDailySafe = if (snapshot.daysRemainingInMonth > 0) {
        ((snapshot.remainingDiscretionary - amountValue) / snapshot.daysRemainingInMonth).coerceAtLeast(0.0)
    } else 0.0

    val dateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }
    val displayDate = remember(selectedTimestamp) { dateFormat.format(Date(selectedTimestamp)) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("add_expense_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Expense",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("add_expense_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                actions = {
                    if (amountInput.isNotEmpty() || merchantOrNote.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                amountInput = ""
                                merchantOrNote = ""
                                amountTouched = false
                                selectedCategory = ExpenseCategory.FOOD
                                selectedPaymentMode = PaymentMode.UPI
                                selectedTimestamp = System.currentTimeMillis()
                            },
                            modifier = Modifier.testTag("clear_expense_form_button")
                        ) {
                            Text("Clear")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. AMOUNT INPUT SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "EXPENSE AMOUNT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { input ->
                            amountTouched = true
                            if (input.isEmpty() || input.matches(Regex("""^\d*(\.\d{0,2})?$"""))) {
                                amountInput = input
                            }
                        },
                        label = { Text("Enter Amount") },
                        placeholder = { Text("0.00") },
                        prefix = {
                            Text(
                                text = "₹ ",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (amountInput.isNotEmpty()) {
                                IconButton(onClick = { amountInput = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Amount")
                                }
                            }
                        },
                        isError = isAmountError,
                        supportingText = {
                            if (isAmountError) {
                                Text(
                                    text = "Please enter an amount greater than ₹0",
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = "Today's Safe Limit: ${FinanceCalculator.formatINR(snapshot.safeToSpendToday)}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Increment Chips
                    Text(
                        text = "Quick Add",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(50, 100, 200, 500, 1000, 2000).forEach { inc ->
                            SuggestionChip(
                                onClick = {
                                    amountTouched = true
                                    val current = amountInput.toDoubleOrNull() ?: 0.0
                                    amountInput = (current + inc).toInt().toString()
                                },
                                label = {
                                    Text(
                                        text = "+₹$inc",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("quick_chip_$inc")
                            )
                        }
                    }
                }
            }

            // 2. REAL-TIME SAFE-TO-SPEND IMPACT BANNER
            AnimatedVisibility(visible = amountValue > 0) {
                val impactBg = if (willExceedToday) CautionAmberBg else SafeGreenBg
                val impactColor = if (willExceedToday) CautionAmber else SafeGreen
                val impactIcon = if (willExceedToday) Icons.Default.Warning else Icons.Default.Info

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("safe_spend_impact_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = impactBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(impactColor.copy(alpha = 0.35f)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = impactIcon,
                            contentDescription = null,
                            tint = impactColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (willExceedToday) "Exceeds Today's Safe Rate" else "Within Safe Budget",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = impactColor
                                )
                            )
                            Text(
                                text = if (willExceedToday) {
                                    "This expense exceeds today's limit (${FinanceCalculator.formatINR(snapshot.safeToSpendToday)}). Safe-to-spend will adjust to ${FinanceCalculator.formatINR(newDailySafe)}/day for remaining ${snapshot.daysRemainingInMonth} days."
                                } else {
                                    "Comfortable spending. Safe-to-spend for the remaining ${snapshot.daysRemainingInMonth} days will be ~${FinanceCalculator.formatINR(newDailySafe)}/day."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 3. CATEGORY SELECTION SECTION (Material 3 FilterChips)
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 1.1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedCategory.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = selectedCategory.color
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExpenseCategory.entries.forEach { category ->
                            val isSelected = category == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = {
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = category.color.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) category.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    selectedBorderColor = category.color
                                ),
                                modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // 4. DATE SELECTION SECTION (Material DatePicker trigger)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "TRANSACTION DATE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = displayDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date of Expense") },
                        trailingIcon = {
                            IconButton(
                                onClick = { showDatePickerDialog = true },
                                modifier = Modifier.testTag("date_picker_trigger")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Pick Date",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePickerDialog = true }
                            .testTag("expense_date_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick date selector chips (Today, Yesterday, 2 Days Ago)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            0 to "Today",
                            1 to "Yesterday",
                            2 to "2 Days Ago"
                        ).forEach { (daysAgo, label) ->
                            val cal = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, -daysAgo)
                            }
                            val isCurrentSelection = remember(selectedTimestamp, daysAgo) {
                                val selCal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                selCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) &&
                                selCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                            }

                            AssistChip(
                                onClick = {
                                    selectedTimestamp = cal.timeInMillis
                                },
                                label = { Text(label) },
                                leadingIcon = if (isCurrentSelection) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isCurrentSelection) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("quick_date_$label")
                            )
                        }
                    }
                }
            }

            // 5. MERCHANT & NOTE SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "MERCHANT & DESCRIPTION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = merchantOrNote,
                        onValueChange = { if (it.length <= 60) merchantOrNote = it },
                        label = { Text("Merchant / Note") },
                        placeholder = { Text("e.g., Swiggy, Metro Card, DMart, Rent") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (merchantOrNote.isNotEmpty()) {
                                IconButton(onClick = { merchantOrNote = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        supportingText = {
                            Text(
                                text = "${merchantOrNote.length} / 60",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_merchant_field")
                    )
                }
            }

            // 6. PAYMENT MODE SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "PAYMENT METHOD",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMode.entries.forEach { mode ->
                            val isSelected = mode == selectedPaymentMode
                            val modeIcon = when (mode) {
                                PaymentMode.UPI -> Icons.Default.Payment
                                PaymentMode.CASH -> Icons.Default.LocalAtm
                                PaymentMode.CREDIT_CARD, PaymentMode.DEBIT_CARD -> Icons.Default.CreditCard
                                PaymentMode.NET_BANKING -> Icons.Default.Payment
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPaymentMode = mode },
                                label = { Text(mode.displayName) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = modeIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("payment_chip_${mode.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // 7. RECURRING EXPENSE SECTION
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recurring_expense_card"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Recurring Expense",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Subscriptions, bills, rent, or SIPs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            modifier = Modifier.testTag("recurring_expense_switch")
                        )
                    }

                    AnimatedVisibility(visible = isRecurring) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                        ) {
                            Text(
                                text = "FREQUENCY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    com.example.data.model.RecurrenceFrequency.WEEKLY,
                                    com.example.data.model.RecurrenceFrequency.MONTHLY,
                                    com.example.data.model.RecurrenceFrequency.YEARLY,
                                    com.example.data.model.RecurrenceFrequency.DAILY
                                ).forEach { freq ->
                                    val isSelected = freq == selectedFrequency
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedFrequency = freq },
                                        label = { Text(freq.displayName) },
                                        leadingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        } else null,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.testTag("freq_chip_${freq.name.lowercase()}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "💡 This ${selectedFrequency.displayName.lowercase()} commitment will repeat automatically and be factored into budget pacing.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 8. ACTION BUTTONS (SUBMIT & CANCEL)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        amountTouched = true
                        if (isAmountValid) {
                            val finalNote = merchantOrNote.trim().ifBlank { selectedCategory.displayName }
                            onSaveExpense(
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
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_expense_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (amountValue > 0) "Save Expense • ${FinanceCalculator.formatINR(amountValue)}" else "Save Expense",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("cancel_expense_button")
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Material 3 DatePickerDialog
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
                    enabled = confirmEnabled,
                    modifier = Modifier.testTag("date_picker_confirm")
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false },
                    modifier = Modifier.testTag("date_picker_cancel")
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true
            )
        }
    }
}
