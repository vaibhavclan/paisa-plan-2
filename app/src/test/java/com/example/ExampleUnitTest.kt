package com.example

import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.UserBudgetEntity
import com.example.domain.BudgetPacingStatus
import com.example.domain.FinanceCalculator
import com.example.domain.SafeToSpendService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun testSafeToSpendCalculation() {
        val budget = UserBudgetEntity(
            monthlyIncome = 60000.0,
            fixedExpenses = 20000.0,
            savingsTarget = 10000.0
        )
        // Discretionary budget should be 60000 - 20000 - 10000 = 30000
        assertEquals(30000.0, budget.totalDiscretionaryBudget, 0.01)

        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val snapshot = FinanceCalculator.calculateSnapshot(budget, emptyList(), cal)
        assertEquals(30000.0, snapshot.totalDiscretionaryBudget, 0.01)
        assertEquals(30000.0, snapshot.remainingDiscretionary, 0.01)
        assertTrue(snapshot.safeToSpendToday > 0)
        assertEquals(BudgetPacingStatus.SAFE, snapshot.pacingStatus)
    }

    @Test
    fun testSafeToSpendServiceWithRemainingSavingsGoals() {
        val service = SafeToSpendService.default
        val income = 60000.0
        val fixedExpenses = 20000.0

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.OCTOBER)
            set(Calendar.DAY_OF_MONTH, 1) // Day 1 of 31-day month
        }
        val nowMs = cal.timeInMillis

        // Goal: Target 60,000, current 0, due in ~6 months (182 days) -> ~10,000/month
        val sixMonthsLater = nowMs + (182L * 24 * 60 * 60 * 1000)
        val goals = listOf(
            SavingsGoalEntity(
                id = 1,
                name = "Emergency Fund",
                targetAmount = 60000.0,
                currentAmount = 0.0,
                targetDateTimestamp = sixMonthsLater
            )
        )

        val result = service.calculateDailySafeToSpend(
            monthlyIncome = income,
            fixedExpenses = fixedExpenses,
            remainingSavingsGoals = goals,
            expensesThisMonth = emptyList(),
            calendar = cal,
            fallbackSavingsTarget = 0.0
        )

        // Monthly goal commitment should be ~10,000
        assertTrue(result.monthlySavingsGoalCommitment in 9800.0..10200.0)
        // Discretionary budget: 60,000 - 20,000 - ~10,000 = ~30,000
        assertTrue(result.remainingDiscretionaryBudget in 29800.0..30200.0)
        // Daily safe to spend for 31 days: ~30,000 / 31 = ~967/day
        assertTrue(result.dailySafeToSpend in 950.0..1000.0)
        assertEquals(BudgetPacingStatus.SAFE, result.pacingStatus)
    }

    @Test
    fun testSafeToSpendServiceCompletedGoalsIgnored() {
        val service = SafeToSpendService.default
        val cal = Calendar.getInstance()
        val nowMs = cal.timeInMillis

        val goals = listOf(
            SavingsGoalEntity(
                id = 1,
                name = "Completed Goal",
                targetAmount = 25000.0,
                currentAmount = 25000.0, // Completed
                targetDateTimestamp = nowMs + 1000000L
            )
        )

        val result = service.calculateDailySafeToSpend(
            monthlyIncome = 50000.0,
            fixedExpenses = 15000.0,
            remainingSavingsGoals = goals,
            expensesThisMonth = emptyList(),
            calendar = cal,
            fallbackSavingsTarget = 0.0
        )

        // Completed goal should require 0 additional monthly savings
        assertEquals(0.0, result.monthlySavingsGoalCommitment, 0.01)
        assertEquals(35000.0, result.totalDiscretionaryBudget, 0.01)
    }

    @Test
    fun testImpulsePurchaseAnalysis() {
        val budget = UserBudgetEntity(
            monthlyIncome = 50000.0,
            fixedExpenses = 18000.0,
            savingsTarget = 10000.0
        )
        val snapshot = FinanceCalculator.calculateSnapshot(budget, emptyList())
        val impulse = FinanceCalculator.analyzeImpulsePurchase(5000.0, snapshot)

        assertNotNull(impulse)
        assertTrue(impulse.daysOfSafeBudget > 0)
        assertTrue(impulse.workHoursEquivalent > 0)
    }

    @Test
    fun testFiftyThirtyTwentyRule() {
        val budget = UserBudgetEntity(
            monthlyIncome = 50000.0,
            fixedExpenses = 20000.0,
            savingsTarget = 10000.0
        )
        val expenses = listOf(
            ExpenseEntity(amount = 2000.0, category = ExpenseCategory.GROCERIES.name, merchantOrNote = "Store", month = 10, year = 2026),
            ExpenseEntity(amount = 3000.0, category = ExpenseCategory.FOOD.name, merchantOrNote = "Dining", month = 10, year = 2026)
        )
        val rule = FinanceCalculator.calculate503020(budget, expenses)
        assertTrue(rule.actualNeedsPercent > 0)
        assertTrue(rule.actualWantsPercent > 0)
        assertTrue(rule.actualSavingsPercent > 0)
        assertTrue(rule.insightMessage.isNotBlank())
    }

    @Test
    fun testRecurringExpenseCreationAndFrequency() {
        val recurringExpense = ExpenseEntity(
            amount = 1499.0,
            category = ExpenseCategory.BILLS.name,
            merchantOrNote = "Internet Bill",
            paymentMode = "UPI",
            month = 10,
            year = 2026,
            isRecurring = true,
            recurrenceFrequency = "MONTHLY"
        )

        assertTrue(recurringExpense.isRecurring)
        assertEquals("MONTHLY", recurringExpense.recurrenceFrequency)

        val freqWeekly = com.example.data.model.RecurrenceFrequency.fromString("WEEKLY")
        assertEquals(com.example.data.model.RecurrenceFrequency.WEEKLY, freqWeekly)
        assertEquals("Weekly", freqWeekly.displayName)

        val freqMonthly = com.example.data.model.RecurrenceFrequency.fromString("MONTHLY")
        assertEquals(com.example.data.model.RecurrenceFrequency.MONTHLY, freqMonthly)
        assertEquals("Monthly", freqMonthly.displayName)
    }
}

