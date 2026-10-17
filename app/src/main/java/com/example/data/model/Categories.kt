package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    FOOD("Food & Dining", Icons.Default.Fastfood, Color(0xFFF97316)),
    GROCERIES("Groceries", Icons.Default.LocalGroceryStore, Color(0xFF10B981)),
    TRANSPORT("Transport", Icons.Default.DirectionsCar, Color(0xFF0EA5E9)),
    SHOPPING("Shopping", Icons.Default.ShoppingBag, Color(0xFFEC4899)),
    BILLS("Bills & Utilities", Icons.Default.Lightbulb, Color(0xFFFBBF24)),
    ENTERTAINMENT("Entertainment", Icons.Default.Movie, Color(0xFF8B5CF6)),
    HEALTH("Health & Care", Icons.Default.FitnessCenter, Color(0xFF14B8A6)),
    INVESTMENT("Investment / SIP", Icons.Default.TrendingUp, Color(0xFF059669)),
    OTHER("Other", Icons.Default.Payment, Color(0xFF64748B));

    companion object {
        fun fromString(name: String): ExpenseCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class PaymentMode(val displayName: String) {
    UPI("UPI (GPay / PhonePe)"),
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    NET_BANKING("Net Banking");

    companion object {
        fun fromString(name: String): PaymentMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: UPI
        }
    }
}

enum class GoalCategory(val displayName: String, val icon: ImageVector) {
    EMERGENCY("Emergency Fund", Icons.Default.Savings),
    TRAVEL("Vacation & Travel", Icons.Default.CardTravel),
    VEHICLE("Bike / Car", Icons.Default.DirectionsCar),
    ELECTRONICS("Gadget / Phone", Icons.Default.ShoppingBag),
    EDUCATION("Education / Course", Icons.Default.School),
    INVESTMENT("Gold / Mutual Fund", Icons.Default.TrendingUp),
    GENERAL("General Savings", Icons.Default.Savings);

    companion object {
        fun fromString(name: String): GoalCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: GENERAL
        }
    }
}

enum class RecurrenceFrequency(val displayName: String, val shortLabel: String) {
    NONE("One-time", "One-time"),
    DAILY("Daily", "Daily"),
    WEEKLY("Weekly", "Weekly"),
    MONTHLY("Monthly", "Monthly"),
    YEARLY("Yearly", "Yearly");

    companion object {
        fun fromString(name: String): RecurrenceFrequency {
            return entries.firstOrNull {
                it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true)
            } ?: NONE
        }
    }
}

