package com.example.domain

import com.example.data.model.ExpenseEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.UserBudgetEntity
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max

/**
 * Detailed breakdown of required monthly savings contribution for an active goal.
 */
data class GoalMonthlyRequirement(
    val goalId: Long,
    val goalName: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val remainingToSave: Double,
    val monthsRemaining: Double,
    val monthlyContributionNeeded: Double
)

/**
 * Result data class produced by SafeToSpendService.
 */
data class SafeToSpendResult(
    val dailySafeToSpend: Double,
    val remainingDiscretionaryBudget: Double,
    val totalDiscretionaryBudget: Double,
    val monthlyIncome: Double,
    val fixedExpenses: Double,
    val monthlySavingsGoalCommitment: Double,
    val totalRemainingInGoals: Double,
    val totalSpentThisMonth: Double,
    val spentToday: Double,
    val daysRemainingInMonth: Int,
    val currentDayOfMonth: Int,
    val totalDaysInMonth: Int,
    val dailyBaselineLimit: Double,
    val pacingStatus: BudgetPacingStatus,
    val goalRequirements: List<GoalMonthlyRequirement>,
    val guidanceMessage: String
)

interface ISafeToSpendService {
    fun calculateDailySafeToSpend(
        monthlyIncome: Double,
        fixedExpenses: Double,
        remainingSavingsGoals: List<SavingsGoalEntity>,
        expensesThisMonth: List<ExpenseEntity>,
        calendar: Calendar = Calendar.getInstance(),
        fallbackSavingsTarget: Double = 0.0
    ): SafeToSpendResult
}

/**
 * Service that calculates the user's daily 'safe-to-spend' amount based on:
 * 1. Monthly in-hand income
 * 2. Fixed essential commitments (rent, utilities, insurance, EMIs)
 * 3. Remaining active savings goals (amortized across remaining timeframes)
 * 4. Discretionary spending logged so far in the current billing month
 * 5. Remaining days in the current calendar month
 */
class SafeToSpendService : ISafeToSpendService {

    override fun calculateDailySafeToSpend(
        monthlyIncome: Double,
        fixedExpenses: Double,
        remainingSavingsGoals: List<SavingsGoalEntity>,
        expensesThisMonth: List<ExpenseEntity>,
        calendar: Calendar,
        fallbackSavingsTarget: Double
    ): SafeToSpendResult {
        val now = calendar.timeInMillis
        val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysRemaining = max(1, totalDaysInMonth - currentDay + 1)

        // 1. Calculate required monthly savings from active, uncompleted goals
        val goalRequirements = mutableListOf<GoalMonthlyRequirement>()
        var totalMonthlyGoalSavings = 0.0
        var totalRemainingGoalBalance = 0.0

        val activeGoals = remainingSavingsGoals.filter { !it.isCompleted }

        for (goal in activeGoals) {
            val remainingToSave = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
            totalRemainingGoalBalance += remainingToSave

            // Calculate months remaining until goal target date
            val msDiff = goal.targetDateTimestamp - now
            val daysDiff = msDiff / (1000.0 * 60 * 60 * 24)
            val monthsDiff = max(1.0, daysDiff / 30.417) // Average month length

            val monthlyNeeded = if (daysDiff <= 0) {
                // If deadline passed or is this month, prioritize remaining amount
                remainingToSave
            } else {
                remainingToSave / monthsDiff
            }

            totalMonthlyGoalSavings += monthlyNeeded
            goalRequirements.add(
                GoalMonthlyRequirement(
                    goalId = goal.id,
                    goalName = goal.name,
                    targetAmount = goal.targetAmount,
                    currentAmount = goal.currentAmount,
                    remainingToSave = remainingToSave,
                    monthsRemaining = monthsDiff,
                    monthlyContributionNeeded = monthlyNeeded
                )
            )
        }

        // Use goal savings commitment, or fallback to baseline savings target if higher or if no goals
        val effectiveMonthlySavingsCommitment = if (goalRequirements.isNotEmpty()) {
            max(totalMonthlyGoalSavings, fallbackSavingsTarget)
        } else {
            fallbackSavingsTarget
        }

        // 2. Discretionary Budget = Income - Fixed Commitments - Effective Savings Goals
        val totalDiscretionaryBudget = max(
            0.0,
            monthlyIncome - fixedExpenses - effectiveMonthlySavingsCommitment
        )

        // 3. Subtract spending already incurred this month
        val totalSpentThisMonth = expensesThisMonth.sumOf { it.amount }
        val remainingDiscretionary = totalDiscretionaryBudget - totalSpentThisMonth

        // 4. Daily Safe-to-Spend = Remaining Discretionary / Remaining Days
        val dailySafeToSpend = if (remainingDiscretionary > 0 && daysRemaining > 0) {
            remainingDiscretionary / daysRemaining
        } else {
            0.0
        }

        val dailyBaseline = if (totalDaysInMonth > 0) {
            totalDiscretionaryBudget / totalDaysInMonth
        } else 0.0

        // Calculate expenses logged today
        val startOfTodayCal = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfTodayMs = startOfTodayCal.timeInMillis
        val spentToday = expensesThisMonth
            .filter { it.timestamp >= startOfTodayMs }
            .sumOf { it.amount }

        // Determine pacing status & advice
        val pacingStatus = when {
            remainingDiscretionary <= 0 -> BudgetPacingStatus.CRITICAL
            dailySafeToSpend > 0 && spentToday > (dailySafeToSpend * 1.25) -> BudgetPacingStatus.CAUTION
            remainingDiscretionary < (dailyBaseline * daysRemaining * 0.7) -> BudgetPacingStatus.CAUTION
            else -> BudgetPacingStatus.SAFE
        }

        val guidance = when (pacingStatus) {
            BudgetPacingStatus.SAFE -> {
                "You have ${FinanceCalculator.formatINR(dailySafeToSpend)} available to spend today without hurting your ${goalRequirements.size} active savings goals."
            }
            BudgetPacingStatus.CAUTION -> {
                "Spending is pacing slightly ahead. Keep daily spends under ${FinanceCalculator.formatINR(dailySafeToSpend)} to protect goal timelines."
            }
            BudgetPacingStatus.CRITICAL -> {
                "Discretionary allowance for the month is depleted. Restrict non-essential spending to avoid pulling from fixed bills or savings targets."
            }
        }

        return SafeToSpendResult(
            dailySafeToSpend = dailySafeToSpend,
            remainingDiscretionaryBudget = remainingDiscretionary,
            totalDiscretionaryBudget = totalDiscretionaryBudget,
            monthlyIncome = monthlyIncome,
            fixedExpenses = fixedExpenses,
            monthlySavingsGoalCommitment = effectiveMonthlySavingsCommitment,
            totalRemainingInGoals = totalRemainingGoalBalance,
            totalSpentThisMonth = totalSpentThisMonth,
            spentToday = spentToday,
            daysRemainingInMonth = daysRemaining,
            currentDayOfMonth = currentDay,
            totalDaysInMonth = totalDaysInMonth,
            dailyBaselineLimit = dailyBaseline,
            pacingStatus = pacingStatus,
            goalRequirements = goalRequirements,
            guidanceMessage = guidance
        )
    }

    /**
     * Convenience method calculating safe-to-spend using the UserBudgetEntity and Goals.
     */
    fun calculateForBudget(
        budget: UserBudgetEntity,
        goals: List<SavingsGoalEntity>,
        expensesThisMonth: List<ExpenseEntity>,
        calendar: Calendar = Calendar.getInstance()
    ): SafeToSpendResult {
        return calculateDailySafeToSpend(
            monthlyIncome = budget.monthlyIncome,
            fixedExpenses = budget.fixedExpenses,
            remainingSavingsGoals = goals,
            expensesThisMonth = expensesThisMonth,
            calendar = calendar,
            fallbackSavingsTarget = budget.savingsTarget
        )
    }

    companion object {
        val default = SafeToSpendService()
    }
}
