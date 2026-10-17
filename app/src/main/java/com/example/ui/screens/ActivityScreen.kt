package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.domain.FinanceCalculator
import com.example.ui.components.ExpenseItemRow
import java.util.Calendar

@Composable
fun ActivityScreen(
    expenses: List<ExpenseEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: ExpenseCategory?,
    onCategorySelect: (ExpenseCategory?) -> Unit,
    selectedPeriod: String,
    onPeriodSelect: (String) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onAddExpenseClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()

    // Filter by Period
    val periodFiltered = expenses.filter { expense ->
        when (selectedPeriod) {
            "Today" -> {
                val calExp = Calendar.getInstance().apply { timeInMillis = expense.timestamp }
                calExp.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                calExp.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
            }
            "This Week" -> {
                val oneWeekAgo = now - (7L * 24 * 60 * 60 * 1000)
                expense.timestamp >= oneWeekAgo
            }
            "This Month" -> {
                val calExp = Calendar.getInstance().apply { timeInMillis = expense.timestamp }
                calExp.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                calExp.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            }
            else -> true
        }
    }

    // Filter by Category
    val categoryFiltered = if (selectedCategory != null) {
        periodFiltered.filter { it.category == selectedCategory.name }
    } else {
        periodFiltered
    }

    // Filter by Search
    val finalFiltered = if (searchQuery.isNotBlank()) {
        categoryFiltered.filter {
            it.merchantOrNote.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.paymentMode.contains(searchQuery, ignoreCase = true)
        }
    } else {
        categoryFiltered
    }

    val totalSpentInView = finalFiltered.sumOf { it.amount }

    androidx.compose.material3.Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (onAddExpenseClick != null) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = onAddExpenseClick,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(bottom = 64.dp)
                        .testTag("fab_add_expense_activity")
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ReceiptLong,
                        contentDescription = "Add Expense"
                    )
                }
            }
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .testTag("activity_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // Title Header
        item {
            Column {
                Text(
                    text = "Activity & History 🧾",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Search and filter all your logged expenses.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search merchant, note, or payment mode...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activity_search_input")
            )
        }

        // Period Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("This Month", "This Week", "Today", "All Time").forEach { period ->
                    val isSelected = period == selectedPeriod
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onPeriodSelect(period) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("period_filter_$period")
                    ) {
                        Text(
                            text = period,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All Categories" chip
                val isAllSelected = selectedCategory == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isAllSelected) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onCategorySelect(null) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "All Categories",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isAllSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                    )
                }

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
                            .clickable {
                                onCategorySelect(if (isSelected) null else cat)
                            }
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

        // Summary Bar for filtered items
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${finalFiltered.size} Transactions",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total: ${FinanceCalculator.formatINR(totalSpentInView)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // List
        if (finalFiltered.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching expenses found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Try clearing filters or search to view more transactions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(finalFiltered, key = { it.id }) { expense ->
                ExpenseItemRow(
                    expense = expense,
                    onDelete = { expenseToDelete = expense }
                )
            }
        }
    }
    }

    // Delete Confirmation Dialog
    if (expenseToDelete != null) {
        val expense = expenseToDelete!!
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Expense") },
            text = {
                Text("Are you sure you want to delete '${expense.merchantOrNote}' for ${FinanceCalculator.formatINR(expense.amount)}? Your safe-to-spend limit will automatically recalculate.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense(expense)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
