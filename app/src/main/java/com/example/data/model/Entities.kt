package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_budget")
data class UserBudgetEntity(
    @PrimaryKey val id: Int = 1,
    val monthlyIncome: Double = 50000.0,
    val fixedExpenses: Double = 18000.0,
    val savingsTarget: Double = 10000.0,
    val userName: String = "User",
    val currencySymbol: String = "₹",
    val isSetupCompleted: Boolean = false
) {
    val totalDiscretionaryBudget: Double
        get() = (monthlyIncome - fixedExpenses - savingsTarget).coerceAtLeast(0.0)
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,
    val merchantOrNote: String,
    val paymentMode: String = "UPI",
    val timestamp: Long = System.currentTimeMillis(),
    val month: Int,
    val year: Int,
    val isRecurring: Boolean = false,
    val recurrenceFrequency: String = "NONE"
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDateTimestamp: Long,
    val categoryIcon: String = "GENERAL",
    val notes: String = ""
) {
    val progressFraction: Float
        get() = if (targetAmount > 0) ((currentAmount / targetAmount).toFloat()).coerceIn(0f, 1f) else 0f

    val isCompleted: Boolean
        get() = currentAmount >= targetAmount
}
