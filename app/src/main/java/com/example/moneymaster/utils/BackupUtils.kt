package com.example.moneymaster.utils

import android.content.Context
import com.example.moneymaster.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object BackupUtils {
    private const val BACKUP_DIRECTORY = "backups"
    private val gson = Gson()

    fun exportTransactions(context: Context, transactions: List<Transaction>): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "money_master_backup_$timestamp.json"
        
        try {
            // Create backups directory if it doesn't exist
            val backupDir = File(context.filesDir, BACKUP_DIRECTORY)
            if (!backupDir.exists()) {
                backupDir.mkdir()
            }

            // Convert transactions to JSON
            val jsonString = gson.toJson(transactions)
            
            // Write to file
            val backupFile = File(backupDir, fileName)
            FileWriter(backupFile).use { writer ->
                writer.write(jsonString)
            }
            
            return backupFile.absolutePath
        } catch (e: Exception) {
            throw IOException("Failed to create backup: ${e.message}")
        }
    }

    fun importTransactions(context: Context, backupFilePath: String): List<Transaction> {
        try {
            val file = File(backupFilePath)
            if (!file.exists()) {
                throw FileNotFoundException("Backup file not found")
            }

            // Read JSON from file
            val jsonString = FileReader(file).use { reader ->
                reader.readText()
            }

            // Convert JSON back to List<Transaction>
            val type = object : TypeToken<List<Transaction>>() {}.type
            return gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            throw IOException("Failed to restore backup: ${e.message}")
        }
    }

    fun listBackupFiles(context: Context): List<File> {
        val backupDir = File(context.filesDir, BACKUP_DIRECTORY)
        if (!backupDir.exists()) {
            return emptyList()
        }
        return backupDir.listFiles()?.filter { it.name.endsWith(".json") }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
} 