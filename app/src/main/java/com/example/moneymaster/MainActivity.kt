package com.example.moneymaster

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymaster.adapter.TransactionAdapter
import com.example.moneymaster.model.Transaction
import com.example.moneymaster.utils.BackupUtils
import com.example.moneymaster.utils.DataManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Currency

class MainActivity : AppCompatActivity() {
    private lateinit var transactionAdapter: TransactionAdapter
    private val dateFormatter = java.text.SimpleDateFormat("MMM yyyy", Locale.US)
    private val gson = Gson()
    private var isBalanceVisible = true
    private var currentTransactionType = TransactionType.ALL
    private var allTransactions = mutableListOf<Transaction>()

    private enum class TransactionType {
        EXPENSE, INCOME, ALL
    }

    private val editTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val updatedTransaction = result.data?.getSerializableExtra(
                EditTransactionActivity.EXTRA_UPDATED_TRANSACTION
            ) as? Transaction
            if (updatedTransaction != null) {
                // Find and update the transaction
                val position = transactionAdapter.getTransactions().indexOfFirst { it.date == updatedTransaction.date }
                if (position != -1) {
                    transactionAdapter.updateTransaction(position, updatedTransaction)
                    DataManager.saveTransactions(this, transactionAdapter.getTransactions())
                    updateTotalAmount()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.right)
            insets
        }

        // Initialize RecyclerView
        setupRecyclerView()

        // Setup TabLayout
        setupTabLayout()

        // Setup current date
        setupCurrentDate()

        // Load saved transactions
        loadSavedTransactions()

        // Set up FAB click listener
        findViewById<FloatingActionButton>(R.id.addTransactionButton).setOnClickListener {
            val intent = Intent(this, AddEditTransactionActivity::class.java)
            startActivityForResult(intent, ADD_TRANSACTION_REQUEST)
        }

        // Set up balance visibility toggle
        findViewById<ImageButton>(R.id.toggleBalance).setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            updateTotalAmount()
        }

        // Setup bottom navigation
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onTransactionUpdated = {
                DataManager.saveTransactions(this, transactionAdapter.getTransactions())
                updateTotalAmount()
            },
            editTransactionLauncher = editTransactionLauncher
        )
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.transactionsList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupTabLayout() {
        val tabLayout = findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> currentTransactionType = TransactionType.ALL
                    1 -> currentTransactionType = TransactionType.INCOME
                    2 -> currentTransactionType = TransactionType.EXPENSE
                }
                filterTransactions()
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun filterTransactions() {
        val filteredTransactions = when (currentTransactionType) {
            TransactionType.EXPENSE -> allTransactions.filter { it.isExpense }
            TransactionType.INCOME -> allTransactions.filter { !it.isExpense }
            TransactionType.ALL -> allTransactions
        }
        transactionAdapter.setTransactions(filteredTransactions)
        updateTotalAmount()
    }

    private fun loadSavedTransactions() {
        allTransactions = DataManager.loadTransactions(this).toMutableList()
        if (allTransactions.isEmpty()) {
            Toast.makeText(this, "No saved transactions found", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Loaded ${allTransactions.size} transactions", Toast.LENGTH_SHORT).show()
        }
        filterTransactions()
    }

    private fun setupCurrentDate() {
        val dateButton = findViewById<Button>(R.id.dateButton)
        dateButton.text = dateFormatter.format(java.util.Date())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TRANSACTION_REQUEST && resultCode == RESULT_OK) {
            data?.let {
                val title = it.getStringExtra(EXTRA_TITLE) ?: return
                val amount = it.getDoubleExtra(EXTRA_AMOUNT, 0.0)
                val date = it.getStringExtra(EXTRA_DATE) ?: return
                val category = it.getStringExtra(EXTRA_CATEGORY) ?: return
                val notes = it.getStringExtra(EXTRA_NOTES) ?: ""
                val isExpense = it.getBooleanExtra(EXTRA_IS_EXPENSE, true)

                // Create new transaction
                val transaction = Transaction(
                    title = title,
                    amount = amount,
                    date = date,
                    category = category,
                    notes = notes,
                    isExpense = isExpense
                )

                // Add to allTransactions list
                allTransactions.add(0, transaction)

                // Save all transactions
                DataManager.saveTransactions(this, allTransactions)
                Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show()

                // Update the display
                filterTransactions()
            }
        }
    }

    private fun getCurrencyFormatter(): NumberFormat {
        val selectedCurrency = DataManager.getSelectedCurrency(this)
        return NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(selectedCurrency.code)
        }
    }

    private fun updateTotalAmount() {
        val currencyFormatter = getCurrencyFormatter()
        // Always use allTransactions for total balance calculation
        var totalIncome = 0.0
        var totalExpenditure = 0.0

        allTransactions.forEach { transaction ->
            if (transaction.isExpense) {
                totalExpenditure += transaction.amount
            } else {
                totalIncome += transaction.amount
            }
        }

        // Always show total balance (income - expenses) in the balance card
        val balanceAmountText = findViewById<TextView>(R.id.balanceAmount)
        balanceAmountText.text = if (isBalanceVisible) {
            currencyFormatter.format(totalIncome - totalExpenditure)
        } else {
            "******"
        }
        balanceAmountText.setTextColor(resources.getColor(R.color.white, theme))

        // Update expenditure card title and amount based on selected tab
        val expenditureCard = findViewById<androidx.cardview.widget.CardView>(R.id.expenditureCard)
        val expenditureTitle = expenditureCard.findViewById<TextView>(R.id.totalExpenditureTitle)
        val expenditureAmount = expenditureCard.findViewById<TextView>(R.id.totalExpenditure)

        // Get filtered transactions for the card display
        val filteredTransactions = transactionAdapter.getTransactions()
        val filteredIncome = filteredTransactions.filter { !it.isExpense }.sumOf { it.amount }
        val filteredExpenses = filteredTransactions.filter { it.isExpense }.sumOf { it.amount }

        when (currentTransactionType) {
            TransactionType.EXPENSE -> {
                expenditureTitle.text = "Total Expenses"
                expenditureAmount.text = currencyFormatter.format(filteredExpenses)
            }
            TransactionType.INCOME -> {
                expenditureTitle.text = "Total Income"
                expenditureAmount.text = currencyFormatter.format(filteredIncome)
            }
            TransactionType.ALL -> {
                expenditureTitle.text = "Net Balance"
                expenditureAmount.text = currencyFormatter.format(filteredIncome - filteredExpenses)
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    showHomeContent()
                    true
                }
                R.id.navigation_statistics -> {
                    showStatisticsFragment()
                    true
                }
                R.id.navigation_budget -> {
                    showBudgetFragment()
                    true
                }
                R.id.navigation_settings -> {
                    showSettingsFragment()
                    true
                }
                else -> false
            }
        }
    }

    private fun showHomeContent() {
        // Show main content
        findViewById<androidx.cardview.widget.CardView>(R.id.balanceCard).visibility = View.VISIBLE
        findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout).visibility = View.VISIBLE
        findViewById<androidx.cardview.widget.CardView>(R.id.expenditureCard).visibility = View.VISIBLE
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.transactionsList).visibility = View.VISIBLE
        findViewById<FloatingActionButton>(R.id.addTransactionButton).visibility = View.VISIBLE

        // Remove any existing fragment
        val fragment = supportFragmentManager.findFragmentById(R.id.main)
        fragment?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
    }

    private fun showStatisticsFragment() {
        // Hide main content
        findViewById<androidx.cardview.widget.CardView>(R.id.balanceCard).visibility = View.GONE
        findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout).visibility = View.GONE
        findViewById<androidx.cardview.widget.CardView>(R.id.expenditureCard).visibility = View.GONE
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.transactionsList).visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.addTransactionButton).visibility = View.GONE

        // Show statistics fragment
        val fragment = StatisticsFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.main, fragment)
            .commit()
    }

    private fun showBudgetFragment() {
        // Hide main content
        findViewById<androidx.cardview.widget.CardView>(R.id.balanceCard).visibility = View.GONE
        findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout).visibility = View.GONE
        findViewById<androidx.cardview.widget.CardView>(R.id.expenditureCard).visibility = View.GONE
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.transactionsList).visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.addTransactionButton).visibility = View.GONE

        // Show budget fragment
        val fragment = BudgetFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.main, fragment)
            .commit()
    }

    private fun showSettingsFragment() {
        // Hide main content
        findViewById<androidx.cardview.widget.CardView>(R.id.balanceCard).visibility = View.GONE
        findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout).visibility = View.GONE
        findViewById<androidx.cardview.widget.CardView>(R.id.expenditureCard).visibility = View.GONE
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.transactionsList).visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.addTransactionButton).visibility = View.GONE

        // Show settings fragment
        val fragment = SettingsFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.main, fragment)
            .commit()
    }

    fun updateCurrency() {
        // Update all amounts with new currency
        updateTotalAmount()
        
        // Update transaction list
        transactionAdapter.notifyDataSetChanged()
    }

    companion object {
        const val ADD_TRANSACTION_REQUEST = 1
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_NOTES = "extra_notes"
        const val EXTRA_IS_EXPENSE = "extra_is_expense"
    }
}