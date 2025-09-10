package com.example.moneymaster

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymaster.model.Transaction
import com.example.moneymaster.utils.CategoryUtils
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditTransactionActivity : AppCompatActivity() {
    private lateinit var titleEdit: EditText
    private lateinit var amountEdit: EditText
    private lateinit var categoryDropdown: AutoCompleteTextView
    private lateinit var categoryLayout: TextInputLayout
    private lateinit var notesEdit: EditText
    private lateinit var expenseRadio: RadioButton
    private lateinit var incomeRadio: RadioButton
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        // Initialize views
        titleEdit = findViewById(R.id.titleEdit)
        amountEdit = findViewById(R.id.amountEdit)
        categoryDropdown = findViewById(R.id.categoryDropdown)
        categoryLayout = findViewById(R.id.categoryLayout)
        notesEdit = findViewById(R.id.notesEdit)
        expenseRadio = findViewById(R.id.expenseRadio)
        incomeRadio = findViewById(R.id.incomeRadio)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        // Get transaction data from intent
        val transaction = intent.getSerializableExtra(EXTRA_TRANSACTION) as? Transaction
        if (transaction == null) {
            Toast.makeText(this, "Error: Transaction data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set up category dropdown
        setupCategoryDropdown(transaction.isExpense)

        // Populate views with transaction data
        titleEdit.setText(transaction.title)
        amountEdit.setText(transaction.amount.toString())
        categoryDropdown.setText(transaction.category)
        notesEdit.setText(transaction.notes)
        if (transaction.isExpense) {
            expenseRadio.isChecked = true
        } else {
            incomeRadio.isChecked = true
        }

        // Set up radio button listeners
        expenseRadio.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setupCategoryDropdown(true)
            }
        }

        incomeRadio.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setupCategoryDropdown(false)
            }
        }

        // Set up button click listeners
        saveButton.setOnClickListener {
            if (validateInputs()) {
                val updatedTransaction = Transaction(
                    title = titleEdit.text.toString(),
                    amount = amountEdit.text.toString().toDoubleOrNull() ?: 0.0,
                    date = transaction.date, // Keep the original date
                    category = categoryDropdown.text.toString(),
                    notes = notesEdit.text.toString(),
                    isExpense = expenseRadio.isChecked
                )
                setResult(RESULT_OK, intent.putExtra(EXTRA_UPDATED_TRANSACTION, updatedTransaction))
                finish()
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun setupCategoryDropdown(isExpense: Boolean) {
        val categories = if (isExpense) {
            CategoryUtils.expenseCategories
        } else {
            CategoryUtils.incomeCategories
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categoryDropdown.setAdapter(adapter)
        
        // Clear the current selection when switching categories
        if (categoryDropdown.text.toString() !in categories) {
            categoryDropdown.setText("")
        }
    }

    private fun validateInputs(): Boolean {
        if (titleEdit.text.isNullOrBlank()) {
            titleEdit.error = "Title is required"
            return false
        }
        if (amountEdit.text.isNullOrBlank()) {
            amountEdit.error = "Amount is required"
            return false
        }
        if (amountEdit.text.toString().toDoubleOrNull() == null) {
            amountEdit.error = "Invalid amount"
            return false
        }
        if (categoryDropdown.text.isNullOrBlank()) {
            categoryLayout.error = "Category is required"
            return false
        }
        return true
    }

    companion object {
        const val EXTRA_TRANSACTION = "extra_transaction"
        const val EXTRA_UPDATED_TRANSACTION = "extra_updated_transaction"
    }
} 