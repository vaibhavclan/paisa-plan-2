package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddFundsDialog
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.EditBudgetDialog
import com.example.ui.components.ImpulseCheckerDialog
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.AlertsPreviewScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.viewmodel.PaisaPlanViewModel

enum class MainTab(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    DASHBOARD("Home", Icons.Filled.Home, Icons.Outlined.Home),
    GOALS("Goals", Icons.Filled.Flag, Icons.Outlined.Flag),
    ACTIVITY("Activity", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainApp(
    viewModel: PaisaPlanViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    var showSubScreen by remember { mutableStateOf<String?>(null) } // e.g. "ALERTS"

    val userBudget by viewModel.userBudget.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val currentMonthExpenses by viewModel.currentMonthExpenses.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val snapshot by viewModel.financialSnapshot.collectAsStateWithLifecycle()
    val fiftyThirtyTwenty by viewModel.fiftyThirtyTwenty.collectAsStateWithLifecycle()

    // Dialog visibility states
    val showAddExpenseDialog by viewModel.showAddExpenseDialog.collectAsStateWithLifecycle()
    val showAddGoalDialog by viewModel.showAddGoalDialog.collectAsStateWithLifecycle()
    val activeGoalForFunds by viewModel.activeGoalForFunds.collectAsStateWithLifecycle()
    val showImpulseChecker by viewModel.showImpulseChecker.collectAsStateWithLifecycle()
    val showEditBudgetDialog by viewModel.showEditBudgetDialog.collectAsStateWithLifecycle()

    // Activity search & filters
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val selectedPeriodFilter by viewModel.selectedPeriodFilter.collectAsStateWithLifecycle()

    // Handle back button when in a subscreen or non-home tab
    BackHandler(enabled = showSubScreen != null || selectedTab != MainTab.DASHBOARD) {
        if (showSubScreen != null) {
            showSubScreen = null
        } else {
            selectedTab = MainTab.DASHBOARD
        }
    }

    if (showSubScreen == "ADD_EXPENSE") {
        com.example.ui.screens.AddExpenseScreen(
            snapshot = snapshot,
            onBackClick = { showSubScreen = null },
            onSaveExpense = { amount, category, merchantOrNote, paymentMode, timestamp, isRecurring, recurrenceFrequency ->
                viewModel.addExpense(amount, category, merchantOrNote, paymentMode, timestamp, isRecurring, recurrenceFrequency)
                showSubScreen = null
            }
        )
    } else if (showSubScreen == "ALERTS") {
        AlertsPreviewScreen(
            snapshot = snapshot,
            onBackClick = { showSubScreen = null }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    MainTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    MainTab.DASHBOARD -> DashboardScreen(
                        snapshot = snapshot,
                        userName = userBudget?.userName ?: "Vaibhav",
                        recentExpenses = currentMonthExpenses,
                        onAddExpenseClick = { showSubScreen = "ADD_EXPENSE" },
                        onAddExpenseDialogClick = { viewModel.openAddExpenseDialog() },
                        onAddGoalClick = { viewModel.openAddGoalDialog() },
                        onImpulseCheckerClick = { viewModel.openImpulseChecker() },
                        onViewAllExpensesClick = { selectedTab = MainTab.ACTIVITY },
                        onDeleteExpense = { viewModel.deleteExpense(it) },
                        onAlertsPreviewClick = { showSubScreen = "ALERTS" }
                    )

                    MainTab.GOALS -> GoalsScreen(
                        goals = goals,
                        onAddGoalClick = { viewModel.openAddGoalDialog() },
                        onAddGoal = { name, target, initial, months, category, notes ->
                            viewModel.createGoal(name, target, initial, months, category, notes)
                        },
                        onAddFundsClick = { viewModel.openAddFundsDialog(it) },
                        onAddFundsAmount = { goal, amount ->
                            viewModel.addFundsToGoal(goal, amount)
                        },
                        onDeleteGoal = { viewModel.deleteGoal(it) }
                    )

                    MainTab.ACTIVITY -> ActivityScreen(
                        expenses = allExpenses,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        selectedCategory = selectedCategoryFilter,
                        onCategorySelect = { viewModel.setCategoryFilter(it) },
                        selectedPeriod = selectedPeriodFilter,
                        onPeriodSelect = { viewModel.setPeriodFilter(it) },
                        onDeleteExpense = { viewModel.deleteExpense(it) },
                        onAddExpenseClick = { showSubScreen = "ADD_EXPENSE" }
                    )

                    MainTab.PROFILE -> ProfileScreen(
                        userBudget = userBudget,
                        snapshot = snapshot,
                        fiftyThirtyTwenty = fiftyThirtyTwenty,
                        onEditBudgetClick = { viewModel.openEditBudgetDialog() },
                        onReloadDemoData = { viewModel.reloadDemoData() },
                        onResetAllData = { viewModel.resetAllData() }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            snapshot = snapshot,
            onDismiss = { viewModel.closeAddExpenseDialog() },
            onConfirm = { amount, category, merchantOrNote, paymentMode, timestamp, isRecurring, recurrenceFrequency ->
                viewModel.addExpense(amount, category, merchantOrNote, paymentMode, timestamp, isRecurring, recurrenceFrequency)
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { viewModel.closeAddGoalDialog() },
            onConfirm = { name, targetAmount, initialDeposit, months, category, notes ->
                viewModel.createGoal(name, targetAmount, initialDeposit, months, category, notes)
            }
        )
    }

    if (activeGoalForFunds != null) {
        AddFundsDialog(
            goal = activeGoalForFunds!!,
            onDismiss = { viewModel.closeAddFundsDialog() },
            onConfirm = { amount ->
                viewModel.addFundsToGoal(activeGoalForFunds!!, amount)
            }
        )
    }

    if (showImpulseChecker) {
        ImpulseCheckerDialog(
            snapshot = snapshot,
            onDismiss = { viewModel.closeImpulseChecker() }
        )
    }

    if (showEditBudgetDialog) {
        EditBudgetDialog(
            currentBudget = userBudget,
            onDismiss = { viewModel.closeEditBudgetDialog() },
            onSave = { income, fixed, savings, name ->
                viewModel.saveBudget(income, fixed, savings, name)
            }
        )
    }
}
