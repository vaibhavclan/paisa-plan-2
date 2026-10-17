package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.GoalCategory
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.UserBudgetEntity
import com.example.domain.FiftyThirtyTwenty
import com.example.domain.FinanceCalculator
import com.example.domain.FinancialSnapshot
import com.example.domain.SafeToSpendResult
import com.example.domain.SafeToSpendService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class PaisaPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).paisaPlanDao()

    private val currentCalendar = Calendar.getInstance()
    val currentMonth: Int = currentCalendar.get(Calendar.MONTH) + 1
    val currentYear: Int = currentCalendar.get(Calendar.YEAR)

    val userBudget: StateFlow<UserBudgetEntity?> = dao.getUserBudget()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allExpenses: StateFlow<List<ExpenseEntity>> = dao.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<SavingsGoalEntity>> = dao.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month expenses for calculations
    val currentMonthExpenses: StateFlow<List<ExpenseEntity>> = dao.getExpensesForMonth(currentMonth, currentYear)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val safeToSpendService: SafeToSpendService = SafeToSpendService.default

    val safeToSpendResult: StateFlow<SafeToSpendResult> = combine(
        userBudget,
        currentMonthExpenses,
        goals
    ) { budget, expenses, goalList ->
        val safeBudget = budget ?: UserBudgetEntity(
            monthlyIncome = 50000.0,
            fixedExpenses = 18000.0,
            savingsTarget = 10000.0,
            isSetupCompleted = false
        )
        safeToSpendService.calculateForBudget(safeBudget, goalList, expenses)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        safeToSpendService.calculateForBudget(
            UserBudgetEntity(monthlyIncome = 50000.0, fixedExpenses = 18000.0, savingsTarget = 10000.0),
            emptyList(),
            emptyList()
        )
    )

    val financialSnapshot: StateFlow<FinancialSnapshot> = combine(
        userBudget,
        currentMonthExpenses,
        goals
    ) { budget, expenses, goalList ->
        val safeBudget = budget ?: UserBudgetEntity(
            monthlyIncome = 50000.0,
            fixedExpenses = 18000.0,
            savingsTarget = 10000.0,
            isSetupCompleted = false
        )
        FinanceCalculator.calculateSnapshot(safeBudget, expenses, goals = goalList)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinanceCalculator.calculateSnapshot(
            UserBudgetEntity(monthlyIncome = 50000.0, fixedExpenses = 18000.0, savingsTarget = 10000.0),
            emptyList()
        )
    )

    val fiftyThirtyTwenty: StateFlow<FiftyThirtyTwenty> = combine(
        userBudget,
        currentMonthExpenses
    ) { budget, expenses ->
        val safeBudget = budget ?: UserBudgetEntity()
        FinanceCalculator.calculate503020(safeBudget, expenses)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinanceCalculator.calculate503020(UserBudgetEntity(), emptyList())
    )

    // UI Dialog States
    private val _showAddExpenseDialog = MutableStateFlow(false)
    val showAddExpenseDialog = _showAddExpenseDialog.asStateFlow()

    private val _showAddGoalDialog = MutableStateFlow(false)
    val showAddGoalDialog = _showAddGoalDialog.asStateFlow()

    private val _activeGoalForFunds = MutableStateFlow<SavingsGoalEntity?>(null)
    val activeGoalForFunds = _activeGoalForFunds.asStateFlow()

    private val _showImpulseChecker = MutableStateFlow(false)
    val showImpulseChecker = _showImpulseChecker.asStateFlow()

    private val _showEditBudgetDialog = MutableStateFlow(false)
    val showEditBudgetDialog = _showEditBudgetDialog.asStateFlow()

    // Activity Filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _selectedPeriodFilter = MutableStateFlow("This Month")
    val selectedPeriodFilter = _selectedPeriodFilter.asStateFlow()

    init {
        // Initialize default demo profile if empty
        viewModelScope.launch {
            dao.getUserBudget().collect { budget ->
                if (budget == null) {
                    dao.saveUserBudget(
                        UserBudgetEntity(
                            id = 1,
                            monthlyIncome = 55000.0,
                            fixedExpenses = 18000.0,
                            savingsTarget = 12000.0,
                            userName = "Vaibhav",
                            isSetupCompleted = true
                        )
                    )
                    loadInitialSampleData()
                }
            }
        }
    }

    private suspend fun loadInitialSampleData() {
        val now = System.currentTimeMillis()
        val oneDayMs = 86400000L
        val sampleExpenses = listOf(
            ExpenseEntity(amount = 340.0, category = ExpenseCategory.FOOD.name, merchantOrNote = "Swiggy Lunch Box", paymentMode = "UPI", timestamp = now - 3600000L * 2, month = currentMonth, year = currentYear),
            ExpenseEntity(amount = 120.0, category = ExpenseCategory.TRANSPORT.name, merchantOrNote = "Metro Smartcard Auto Top-up", paymentMode = "UPI", timestamp = now - 3600000L * 5, month = currentMonth, year = currentYear),
            ExpenseEntity(amount = 1850.0, category = ExpenseCategory.GROCERIES.name, merchantOrNote = "D-Mart Supermarket Provisions", paymentMode = "Credit Card", timestamp = now - oneDayMs, month = currentMonth, year = currentYear),
            ExpenseEntity(amount = 799.0, category = ExpenseCategory.ENTERTAINMENT.name, merchantOrNote = "Netflix & Hotstar Subscription", paymentMode = "Credit Card", timestamp = now - oneDayMs * 3, month = currentMonth, year = currentYear),
            ExpenseEntity(amount = 2500.0, category = ExpenseCategory.INVESTMENT.name, merchantOrNote = "Nifty 50 Index Fund SIP", paymentMode = "Net Banking", timestamp = now - oneDayMs * 5, month = currentMonth, year = currentYear),
            ExpenseEntity(amount = 450.0, category = ExpenseCategory.FOOD.name, merchantOrNote = "Chai & Snacks with colleagues", paymentMode = "UPI", timestamp = now - oneDayMs * 6, month = currentMonth, year = currentYear),
            ExpenseEntity(amount = 1250.0, category = ExpenseCategory.SHOPPING.name, merchantOrNote = "Cotton T-shirt & Socks", paymentMode = "UPI", timestamp = now - oneDayMs * 8, month = currentMonth, year = currentYear),
            ExpenseEntity(amount = 950.0, category = ExpenseCategory.BILLS.name, merchantOrNote = "Fiber Broadband Bill", paymentMode = "UPI", timestamp = now - oneDayMs * 10, month = currentMonth, year = currentYear)
        )
        for (expense in sampleExpenses) {
            dao.insertExpense(expense)
        }

        val sixMonthsLater = now + (oneDayMs * 180)
        val oneYearLater = now + (oneDayMs * 365)
        val sampleGoals = listOf(
            SavingsGoalEntity(name = "Emergency Cushion", targetAmount = 100000.0, currentAmount = 45000.0, targetDateTimestamp = sixMonthsLater, categoryIcon = GoalCategory.EMERGENCY.name, notes = "6 months essential living fund"),
            SavingsGoalEntity(name = "Goa Year-End Trip", targetAmount = 25000.0, currentAmount = 14000.0, targetDateTimestamp = now + (oneDayMs * 70), categoryIcon = GoalCategory.TRAVEL.name, notes = "Flight tickets & beach stay"),
            SavingsGoalEntity(name = "Electric Scooter Downpayment", targetAmount = 40000.0, currentAmount = 18500.0, targetDateTimestamp = oneYearLater, categoryIcon = GoalCategory.VEHICLE.name, notes = "For Ather / Ola scooter")
        )
        for (goal in sampleGoals) {
            dao.insertGoal(goal)
        }
    }

    fun openAddExpenseDialog() { _showAddExpenseDialog.value = true }
    fun closeAddExpenseDialog() { _showAddExpenseDialog.value = false }

    fun openAddGoalDialog() { _showAddGoalDialog.value = true }
    fun closeAddGoalDialog() { _showAddGoalDialog.value = false }

    fun openAddFundsDialog(goal: SavingsGoalEntity) { _activeGoalForFunds.value = goal }
    fun closeAddFundsDialog() { _activeGoalForFunds.value = null }

    fun openImpulseChecker() { _showImpulseChecker.value = true }
    fun closeImpulseChecker() { _showImpulseChecker.value = false }

    fun openEditBudgetDialog() { _showEditBudgetDialog.value = true }
    fun closeEditBudgetDialog() { _showEditBudgetDialog.value = false }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategoryFilter(category: ExpenseCategory?) { _selectedCategoryFilter.value = category }
    fun setPeriodFilter(period: String) { _selectedPeriodFilter.value = period }

    fun saveBudget(
        monthlyIncome: Double,
        fixedExpenses: Double,
        savingsTarget: Double,
        userName: String
    ) {
        viewModelScope.launch {
            dao.saveUserBudget(
                UserBudgetEntity(
                    id = 1,
                    monthlyIncome = monthlyIncome,
                    fixedExpenses = fixedExpenses,
                    savingsTarget = savingsTarget,
                    userName = userName.ifBlank { "User" },
                    isSetupCompleted = true
                )
            )
            _showEditBudgetDialog.value = false
        }
    }

    fun addExpense(
        amount: Double,
        category: ExpenseCategory,
        merchantOrNote: String,
        paymentMode: String,
        timestamp: Long = System.currentTimeMillis(),
        isRecurring: Boolean = false,
        recurrenceFrequency: String = "NONE"
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            val expenseMonth = cal.get(Calendar.MONTH) + 1
            val expenseYear = cal.get(Calendar.YEAR)
            dao.insertExpense(
                ExpenseEntity(
                    amount = amount,
                    category = category.name,
                    merchantOrNote = merchantOrNote.ifBlank { category.displayName },
                    paymentMode = paymentMode,
                    timestamp = timestamp,
                    month = expenseMonth,
                    year = expenseYear,
                    isRecurring = isRecurring,
                    recurrenceFrequency = recurrenceFrequency
                )
            )
            _showAddExpenseDialog.value = false
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            dao.deleteExpense(expense)
        }
    }

    fun createGoal(
        name: String,
        targetAmount: Double,
        initialDeposit: Double,
        targetMonths: Int,
        category: GoalCategory,
        notes: String
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply {
                add(Calendar.MONTH, targetMonths)
            }
            dao.insertGoal(
                SavingsGoalEntity(
                    name = name,
                    targetAmount = targetAmount,
                    currentAmount = initialDeposit.coerceAtLeast(0.0),
                    targetDateTimestamp = cal.timeInMillis,
                    categoryIcon = category.name,
                    notes = notes
                )
            )
            _showAddGoalDialog.value = false
        }
    }

    fun addFundsToGoal(goal: SavingsGoalEntity, amount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = (goal.currentAmount + amount).coerceAtMost(goal.targetAmount * 2))
            dao.updateGoal(updated)
            _activeGoalForFunds.value = null
        }
    }

    fun deleteGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            dao.deleteGoal(goal)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            dao.clearExpenses()
            dao.clearGoals()
            dao.clearBudget()
            dao.saveUserBudget(
                UserBudgetEntity(
                    id = 1,
                    monthlyIncome = 50000.0,
                    fixedExpenses = 18000.0,
                    savingsTarget = 10000.0,
                    userName = "User",
                    isSetupCompleted = false
                )
            )
        }
    }

    fun reloadDemoData() {
        viewModelScope.launch {
            dao.clearExpenses()
            dao.clearGoals()
            dao.saveUserBudget(
                UserBudgetEntity(
                    id = 1,
                    monthlyIncome = 60000.0,
                    fixedExpenses = 20000.0,
                    savingsTarget = 15000.0,
                    userName = "Vaibhav",
                    isSetupCompleted = true
                )
            )
            loadInitialSampleData()
        }
    }
}
