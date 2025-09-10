package com.example.moneymaster

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddEditTransactionActivity : AppCompatActivity() {
    private lateinit var titleEditText: TextInputEditText
    private lateinit var amountEditText: TextInputEditText
    private lateinit var dateEditText: TextInputEditText
    private lateinit var notesEditText: TextInputEditText
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_transaction)

        // Initialize views
        titleEditText = findViewById(R.id.titleEditText)
        amountEditText = findViewById(R.id.amountEditText)
        dateEditText = findViewById(R.id.dateEditText)
        notesEditText = findViewById(R.id.notesEditText)

        // Set up category dropdown
        setupCategoryDropdown()

        // Set up date picker
        setupDatePicker()

        // Set up save button
        setupSaveButton()
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf(
            "Food & Dining",
            "Transportation",
            "Shopping",
            "Bills & Utilities",
            "Entertainment",
            "Health & Fitness",
            "Travel",
            "Education",
            "Salary",
            "Investments",
            "Business",
            "Other"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        val categoryAutoComplete = findViewById<android.widget.AutoCompleteTextView>(R.id.categoryAutoComplete)
        categoryAutoComplete.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        dateEditText.setText(dateFormatter.format(calendar.time))

        val datePickerDialog = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            dateEditText.setText(dateFormatter.format(calendar.time))
        }

        dateEditText.setOnClickListener {
            DatePickerDialog(
                this,
                datePickerDialog,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSaveButton() {
        val saveButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.saveButton)
        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (titleEditText.text.isNullOrBlank()) {
            titleEditText.error = "Please enter a title"
            return false
        }

        if (amountEditText.text.isNullOrBlank()) {
            amountEditText.error = "Please enter an amount"
            return false
        }

        val categoryText = findViewById<android.widget.AutoCompleteTextView>(R.id.categoryAutoComplete).text.toString()
        if (categoryText.isBlank()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveTransaction() {
        // Get the transaction type (income/expense)
        val isExpense = findViewById<android.widget.RadioButton>(R.id.expenseRadioButton).isChecked
        
        // Get all the values
        val title = titleEditText.text.toString()
        val amount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val date = dateEditText.text.toString()
        val category = findViewById<android.widget.AutoCompleteTextView>(R.id.categoryAutoComplete).text.toString()
        val notes = notesEditText.text.toString()

        // Create intent with results
        val resultIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_TITLE, title)
            putExtra(MainActivity.EXTRA_AMOUNT, amount)
            putExtra(MainActivity.EXTRA_DATE, date)
            putExtra(MainActivity.EXTRA_CATEGORY, category)
            putExtra(MainActivity.EXTRA_NOTES, notes)
            putExtra(MainActivity.EXTRA_IS_EXPENSE, isExpense)
        }

        setResult(RESULT_OK, resultIntent)
        finish()
    }
} 