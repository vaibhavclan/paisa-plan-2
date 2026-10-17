package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.UserBudgetEntity
import com.example.domain.FinanceCalculator
import com.example.ui.theme.SafeGreen

@Composable
fun EditBudgetDialog(
    currentBudget: UserBudgetEntity?,
    onDismiss: () -> Unit,
    onSave: (income: Double, fixed: Double, savings: Double, name: String) -> Unit
) {
    var nameText by remember { mutableStateOf(currentBudget?.userName ?: "Vaibhav") }
    var incomeText by remember { mutableStateOf((currentBudget?.monthlyIncome?.toInt() ?: 50000).toString()) }
    var fixedText by remember { mutableStateOf((currentBudget?.fixedExpenses?.toInt() ?: 18000).toString()) }
    var savingsText by remember { mutableStateOf((currentBudget?.savingsTarget?.toInt() ?: 10000).toString()) }

    val income = incomeText.toDoubleOrNull() ?: 0.0
    val fixed = fixedText.toDoubleOrNull() ?: 0.0
    val savings = savingsText.toDoubleOrNull() ?: 0.0
    val discretionary = (income - fixed - savings).coerceAtLeast(0.0)
    val dailyEst = discretionary / 30.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Monthly Budget",
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
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("budget_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = incomeText,
                    onValueChange = { incomeText = it.filter { char -> char.isDigit() } },
                    label = { Text("Monthly In-Hand Income (₹)") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("budget_income_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = fixedText,
                    onValueChange = { fixedText = it.filter { char -> char.isDigit() } },
                    label = { Text("Fixed Commitments: Rent/EMI/Bills (₹)") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("budget_fixed_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = savingsText,
                    onValueChange = { savingsText = it.filter { char -> char.isDigit() } },
                    label = { Text("Target Monthly Savings (₹)") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("budget_savings_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Live Preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Resulting Free Cash & Safe Spend",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Discretionary Free Cash: ${FinanceCalculator.formatINR(discretionary)} / month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Base Safe-to-Spend: ~${FinanceCalculator.formatINR(dailyEst)} / day",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SafeGreen
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (income > 0) {
                        onSave(income, fixed, savings, nameText)
                    }
                },
                enabled = income > 0,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_budget_button")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_budget_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
