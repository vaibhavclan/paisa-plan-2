package com.example.domain

import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.UserBudgetEntity
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

enum class BudgetPacingStatus(val label: String, val description: String) {
    SAFE("Pacing Well", "Your spending is well within your daily safe limit."),
    CAUTION("Spending High", "You are approaching or slightly exceeded today's limit."),
    CRITICAL("Budget Exceeded", "Discretionary funds are tight. Restrict non-essential spending.")
}

data class FinancialSnapshot(
    val monthlyIncome: Double,
    val fixedExpenses: Double,
    val savingsTarget: Double,
    val totalDiscretionaryBudget: Double,
    val totalSpentThisMonth: Double,
    val remainingDiscretionary: Double,
    val safeToSpendToday: Double,
    val spentToday: Double,
    val dailyBaselineLimit: Double,
    val currentDayOfMonth: Int,
    val totalDaysInMonth: Int,
    val daysRemainingInMonth: Int,
    val pacingStatus: BudgetPacingStatus,
    val budgetProgressFraction: Float,
    val healthScore: Int,
    val categoryTotals: Map<ExpenseCategory, Double>,
    val monthlySavingsGoalCommitment: Double = savingsTarget
)

data class FiftyThirtyTwenty(
    val actualNeedsPercent: Int,
    val actualWantsPercent: Int,
    val actualSavingsPercent: Int,
    val insightMessage: String
)

data class ImpulseImpact(
    val amount: Double,
    val daysOfSafeBudget: Double,
    val workHoursEquivalent: Double,
    val percentOfRemainingBudget: Double,
    val severityLevel: String,
    val advice: String
)

object FinanceCalculator {
    private val indianCurrencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    fun formatINR(amount: Double): String {
        return try {
            indianCurrencyFormat.format(amount)
        } catch (e: Exception) {
            "₹" + String.format(Locale.US, "%,.0f", amount)
        }
    }

    fun calculateSnapshot(
        budget: UserBudgetEntity,
        expensesThisMonth: List<ExpenseEntity>,
        calendar: Calendar = Calendar.getInstance(),
        goals: List<com.example.data.model.SavingsGoalEntity> = emptyList()
    ): FinancialSnapshot {
        val safeResult = SafeToSpendService.default.calculateDailySafeToSpend(
            monthlyIncome = budget.monthlyIncome,
            fixedExpenses = budget.fixedExpenses,
            remainingSavingsGoals = goals,
            expensesThisMonth = expensesThisMonth,
            calendar = calendar,
            fallbackSavingsTarget = budget.savingsTarget
        )

        val totalDaysInMonth = safeResult.totalDaysInMonth
        val currentDay = safeResult.currentDayOfMonth
        val daysRemaining = safeResult.daysRemainingInMonth
        val totalDiscretionary = safeResult.totalDiscretionaryBudget
        val totalSpent = safeResult.totalSpentThisMonth
        val remainingDiscretionary = safeResult.remainingDiscretionaryBudget
        val safeToSpendToday = safeResult.dailySafeToSpend
        val dailyBaseline = safeResult.dailyBaselineLimit
        val spentToday = safeResult.spentToday
        val pacingStatus = safeResult.pacingStatus

        val progressFraction = if (totalDiscretionary > 0) {
            (totalSpent / totalDiscretionary).toFloat().coerceIn(0f, 1f)
        } else if (totalSpent > 0) 1f else 0f

        // Category breakdown
        val categoryMap = mutableMapOf<ExpenseCategory, Double>()
        for (expense in expensesThisMonth) {
            val cat = ExpenseCategory.fromString(expense.category)
            categoryMap[cat] = (categoryMap[cat] ?: 0.0) + expense.amount
        }

        // Calculate Financial Health Score (0 to 100)
        var score = 70
        // Savings rate factor
        val savingsRate = if (budget.monthlyIncome > 0) budget.savingsTarget / budget.monthlyIncome else 0.0
        if (savingsRate >= 0.20) score += 15 else if (savingsRate >= 0.10) score += 10 else score += 5
        // Spending pace factor
        if (remainingDiscretionary > 0) {
            val expectedRemaining = dailyBaseline * daysRemaining
            if (remainingDiscretionary >= expectedRemaining * 0.9) score += 15
            else if (remainingDiscretionary >= expectedRemaining * 0.6) score += 5
            else score -= 10
        } else {
            score -= 25
        }
        val finalHealthScore = score.coerceIn(15, 100)

        return FinancialSnapshot(
            monthlyIncome = budget.monthlyIncome,
            fixedExpenses = budget.fixedExpenses,
            savingsTarget = budget.savingsTarget,
            totalDiscretionaryBudget = totalDiscretionary,
            totalSpentThisMonth = totalSpent,
            remainingDiscretionary = remainingDiscretionary,
            safeToSpendToday = safeToSpendToday,
            spentToday = spentToday,
            dailyBaselineLimit = dailyBaseline,
            currentDayOfMonth = currentDay,
            totalDaysInMonth = totalDaysInMonth,
            daysRemainingInMonth = daysRemaining,
            pacingStatus = pacingStatus,
            budgetProgressFraction = progressFraction,
            healthScore = finalHealthScore,
            categoryTotals = categoryMap
        )
    }

    fun calculate503020(
        budget: UserBudgetEntity,
        expensesThisMonth: List<ExpenseEntity>
    ): FiftyThirtyTwenty {
        val income = budget.monthlyIncome.coerceAtLeast(1.0)
        
        // Needs = Fixed + Groceries + Bills + Health
        val essentialSpend = expensesThisMonth
            .filter { 
                val cat = ExpenseCategory.fromString(it.category)
                cat == ExpenseCategory.GROCERIES || cat == ExpenseCategory.BILLS || cat == ExpenseCategory.HEALTH 
            }
            .sumOf { it.amount }
        val totalNeeds = budget.fixedExpenses + essentialSpend

        // Wants = Food dining + Shopping + Entertainment + Other + Transport
        val wantsSpend = expensesThisMonth
            .filter { 
                val cat = ExpenseCategory.fromString(it.category)
                cat == ExpenseCategory.FOOD || cat == ExpenseCategory.SHOPPING || 
                cat == ExpenseCategory.ENTERTAINMENT || cat == ExpenseCategory.OTHER || cat == ExpenseCategory.TRANSPORT
            }
            .sumOf { it.amount }

        // Savings = Savings Target + Investment expenses
        val investmentSpend = expensesThisMonth
            .filter { ExpenseCategory.fromString(it.category) == ExpenseCategory.INVESTMENT }
            .sumOf { it.amount }
        val totalSavings = budget.savingsTarget + investmentSpend

        val needsPct = ((totalNeeds / income) * 100).roundToInt().coerceIn(0, 100)
        val wantsPct = ((wantsSpend / income) * 100).roundToInt().coerceIn(0, 100)
        val savingsPct = ((totalSavings / income) * 100).roundToInt().coerceIn(0, 100)

        val insight = when {
            needsPct > 60 -> "Needs take $needsPct% of income (target ≤50%). Look to optimize fixed commitments or high grocery/utility bills."
            savingsPct >= 20 && wantsPct <= 30 -> "Excellent balance! You are meeting the golden 50/30/20 rule with $savingsPct% allocated to future wealth."
            savingsPct < 15 -> "Savings rate is at $savingsPct% (target ≥20%). Try cutting down discretionary spending to boost emergency cushion."
            else -> "Healthy allocation: $needsPct% Needs, $wantsPct% Wants, and $savingsPct% Savings."
        }

        return FiftyThirtyTwenty(
            actualNeedsPercent = needsPct,
            actualWantsPercent = wantsPct,
            actualSavingsPercent = savingsPct,
            insightMessage = insight
        )
    }

    fun analyzeImpulsePurchase(
        amount: Double,
        snapshot: FinancialSnapshot
    ): ImpulseImpact {
        val safeDaily = snapshot.safeToSpendToday.coerceAtLeast(100.0)
        val daysOfBudget = amount / safeDaily
        // Hourly wage assuming 160 hours/month
        val hourlyRate = (snapshot.monthlyIncome / 160.0).coerceAtLeast(10.0)
        val workHours = amount / hourlyRate
        val pctRemaining = if (snapshot.remainingDiscretionary > 0) {
            (amount / snapshot.remainingDiscretionary) * 100.0
        } else {
            100.0
        }

        val (severity, advice) = when {
            pctRemaining > 50 || daysOfBudget > 5 -> Pair(
                "High Impact Purchase",
                "This purchase will consume ${(daysOfBudget).roundToInt()} days of your free daily spending! Consider the 48-hour cooling rule before buying."
            )
            pctRemaining > 20 || daysOfBudget > 2 -> Pair(
                "Moderate Impact",
                "Equals ${(daysOfBudget * 10).roundToInt() / 10.0} days of safe spending ($workHours.roundToInt() hours of work). Make sure it's a planned priority."
            )
            else -> Pair(
                "Low Impact",
                "Well within manageable limits (${(daysOfBudget * 10).roundToInt() / 10.0} days of daily buffer). Safe to proceed if it aligns with your goals."
            )
        }

        return ImpulseImpact(
            amount = amount,
            daysOfSafeBudget = daysOfBudget,
            workHoursEquivalent = workHours,
            percentOfRemainingBudget = pctRemaining,
            severityLevel = severity,
            advice = advice
        )
    }
}
