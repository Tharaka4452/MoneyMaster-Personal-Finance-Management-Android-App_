package com.example.moneymaster

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.moneymaster.auth.AuthManager
import com.example.moneymaster.auth.LoginActivity
import com.example.moneymaster.utils.BackupUtils
import com.example.moneymaster.utils.DataManager
import com.example.moneymaster.model.Currency
import com.google.android.material.button.MaterialButton

class SettingsFragment : Fragment() {

    private lateinit var currencySpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCurrencySpinner(view)

        // Setup backup button
        view.findViewById<MaterialButton>(R.id.backupButton).setOnClickListener {
            backupData()
        }

        // Setup restore button
        view.findViewById<MaterialButton>(R.id.restoreButton).setOnClickListener {
            restoreData()
        }

        // Setup logout button
        view.findViewById<MaterialButton>(R.id.logoutButton).setOnClickListener {
            performLogout()
        }
    }

    private fun setupCurrencySpinner(view: View) {
        currencySpinner = view.findViewById(R.id.currencySpinner)
        
        // Create adapter for currency spinner
        val currencies = Currency.values()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            currencies
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter

        // Set current selection
        val currentCurrency = DataManager.getSelectedCurrency(requireContext())
        val position = currencies.indexOf(currentCurrency)
        currencySpinner.setSelection(position)

        // Handle currency selection
        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCurrency = currencies[position]
                if (selectedCurrency != currentCurrency) {
                    DataManager.saveCurrency(requireContext(), selectedCurrency)
                    Toast.makeText(context, "Currency updated to ${selectedCurrency.code}", Toast.LENGTH_SHORT).show()
                    
                    // Notify MainActivity to update UI
                    (activity as? MainActivity)?.let { mainActivity ->
                        mainActivity.updateCurrency()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun backupData() {
        try {
            val transactions = DataManager.loadTransactions(requireContext())
            val backupPath = BackupUtils.exportTransactions(requireContext(), transactions)
            Toast.makeText(context, "Data backed up successfully to: $backupPath", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to backup data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restoreData() {
        try {
            val backupFiles = BackupUtils.listBackupFiles(requireContext())
            if (backupFiles.isEmpty()) {
                Toast.makeText(context, "No backup files found", Toast.LENGTH_SHORT).show()
                return
            }

            // Get the most recent backup file
            val latestBackup = backupFiles.first()
            val transactions = BackupUtils.importTransactions(requireContext(), latestBackup.absolutePath)
            
            DataManager.saveTransactions(requireContext(), transactions)
            Toast.makeText(context, "Data restored successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to restore data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogout() {
        AuthManager.logout(requireContext())
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        
        // Navigate to login screen
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
} 