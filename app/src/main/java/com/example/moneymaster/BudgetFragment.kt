package com.example.moneymaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.NumberFormat
import java.util.Locale
import com.example.moneymaster.utils.DataManager
import com.example.moneymaster.model.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency

class BudgetFragment : Fragment() {
    private lateinit var budgetAmountInput: TextInputEditText
    private lateinit var saveBudgetButton: MaterialButton
    private lateinit var budgetProgressText: TextView
    private lateinit var spentAmountText: TextView
    private lateinit var remainingAmountText: TextView
    private lateinit var budgetProgressBar: LinearProgressIndicator
    private lateinit var budgetWarningText: TextView
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        budgetAmountInput = view.findViewById(R.id.budgetAmountInput)
        saveBudgetButton = view.findViewById(R.id.saveBudgetButton)
        budgetProgressText = view.findViewById(R.id.budgetProgressText)
        spentAmountText = view.findViewById(R.id.spentAmountText)
        remainingAmountText = view.findViewById(R.id.remainingAmountText)
        budgetProgressBar = view.findViewById(R.id.budgetProgressBar)
        budgetWarningText = view.findViewById(R.id.budgetWarningText)

        // Load saved budget
        loadSavedBudget()

        // Set up save button click listener
        saveBudgetButton.setOnClickListener {
            saveBudget()
        }

        // Update budget progress
        updateBudgetProgress()
    }

    private fun loadSavedBudget() {
        val sharedPrefs = requireContext().getSharedPreferences("BudgetPrefs", 0)
        val savedBudget = sharedPrefs.getFloat("monthly_budget", 0f)
        if (savedBudget > 0) {
            budgetAmountInput.setText(savedBudget.toString())
        }
    }

    private fun saveBudget() {
        val budgetAmount = budgetAmountInput.text.toString().toFloatOrNull() ?: 0f
        val sharedPrefs = requireContext().getSharedPreferences("BudgetPrefs", 0)
        sharedPrefs.edit().putFloat("monthly_budget", budgetAmount).apply()
        updateBudgetProgress()
    }

    private fun getCurrencyFormatter(): NumberFormat {
        val selectedCurrency = DataManager.getSelectedCurrency(requireContext())
        return NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(selectedCurrency.code)
        }
    }

    private fun updateBudgetProgress() {
        val sharedPrefs = requireContext().getSharedPreferences("BudgetPrefs", 0)
        val monthlyBudget = sharedPrefs.getFloat("monthly_budget", 0f)
        
        // Get total expenses for the current month
        val totalExpenses = calculateMonthlyExpenses()
        
        // Calculate remaining amount
        val remainingAmount = monthlyBudget - totalExpenses
        
        val currencyFormatter = getCurrencyFormatter()
        
        // Update UI
        budgetProgressText.text = "Monthly Budget: ${currencyFormatter.format(monthlyBudget)}"
        spentAmountText.text = "Spent: ${currencyFormatter.format(totalExpenses)}"
        remainingAmountText.text = "Remaining: ${currencyFormatter.format(remainingAmount)}"
        
        // Update progress bar
        if (monthlyBudget > 0) {
            val progress = (totalExpenses / monthlyBudget * 100).toInt()
            budgetProgressBar.progress = progress.coerceIn(0, 100)
            
            // Show warning if spending is high
            when {
                progress >= 100 -> {
                    budgetWarningText.text = "Warning: You have exceeded your monthly budget!"
                    budgetWarningText.visibility = View.VISIBLE
                }
                progress >= 80 -> {
                    budgetWarningText.text = "Warning: You are close to exceeding your monthly budget!"
                    budgetWarningText.visibility = View.VISIBLE
                }
                else -> {
                    budgetWarningText.visibility = View.GONE
                }
            }
        }
    }

    private fun calculateMonthlyExpenses(): Float {
        val transactions = DataManager.loadTransactions(requireContext())
        val currentMonth = LocalDate.now().monthValue
        val currentYear = LocalDate.now().year
        
        return transactions
            .filter { transaction -> transaction.isExpense }
            .filter { transaction ->
                try {
                    val transactionDate = LocalDate.parse(transaction.date, dateFormatter)
                    transactionDate.monthValue == currentMonth && transactionDate.year == currentYear
                } catch (e: Exception) {
                    false
                }
            }
            .sumOf { transaction -> transaction.amount }
            .toFloat()
    }

    companion object {
        fun newInstance() = BudgetFragment()
    }
} 