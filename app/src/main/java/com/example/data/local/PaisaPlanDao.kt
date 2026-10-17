package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpenseEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.UserBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaisaPlanDao {
    @Query("SELECT * FROM user_budget WHERE id = 1 LIMIT 1")
    fun getUserBudget(): Flow<UserBudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserBudget(budget: UserBudgetEntity)

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE month = :month AND year = :year ORDER BY timestamp DESC")
    fun getExpensesForMonth(month: Int, year: Int): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM savings_goals ORDER BY targetDateTimestamp ASC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Query("DELETE FROM savings_goals")
    suspend fun clearGoals()

    @Query("DELETE FROM user_budget")
    suspend fun clearBudget()
}
