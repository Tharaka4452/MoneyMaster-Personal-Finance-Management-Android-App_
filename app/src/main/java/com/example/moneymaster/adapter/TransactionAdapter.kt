package com.example.moneymaster.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymaster.EditTransactionActivity
import com.example.moneymaster.R
import com.example.moneymaster.model.Transaction
import com.example.moneymaster.utils.DataManager
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class TransactionAdapter(
    private val onTransactionUpdated: () -> Unit,
    private val editTransactionLauncher: ActivityResultLauncher<Intent>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    private val transactions = mutableListOf<Transaction>()

    fun getTransactions(): List<Transaction> = transactions.toList()

    fun setTransactions(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }

    fun addTransaction(transaction: Transaction) {
        transactions.add(0, transaction) // Add to the beginning of the list
        notifyItemInserted(0)
    }

    fun removeTransaction(position: Int) {
        if (position >= 0 && position < transactions.size) {
            transactions.removeAt(position)
            notifyItemRemoved(position)
            onTransactionUpdated()
        }
    }

    fun updateTransaction(position: Int, transaction: Transaction) {
        if (position >= 0 && position < transactions.size) {
            transactions[position] = transaction
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount() = transactions.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.transactionTitle)
        private val amountText: TextView = itemView.findViewById(R.id.transactionAmount)
        private val categoryText: TextView = itemView.findViewById(R.id.transactionCategory)
        private val dateText: TextView = itemView.findViewById(R.id.transactionDate)

        init {
            itemView.setOnClickListener { view ->
                showOptionsMenu(view, adapterPosition)
            }
        }

        private fun showOptionsMenu(view: View, position: Int) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.transaction_options_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_update -> {
                        // Launch edit activity
                        val transaction = transactions[position]
                        val intent = Intent(view.context, EditTransactionActivity::class.java).apply {
                            putExtra(EditTransactionActivity.EXTRA_TRANSACTION, transaction)
                        }
                        editTransactionLauncher.launch(intent)
                        true
                    }
                    R.id.action_delete -> {
                        // Show confirmation dialog
                        AlertDialog.Builder(view.context)
                            .setTitle("Delete Transaction")
                            .setMessage("Are you sure you want to delete this transaction?")
                            .setPositiveButton("Delete") { _, _ ->
                                removeTransaction(position)
                                onTransactionUpdated()
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }

        fun bind(transaction: Transaction) {
            titleText.text = transaction.title
            val selectedCurrency = DataManager.getSelectedCurrency(itemView.context)
            val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(selectedCurrency.code)
            }
            amountText.text = currencyFormatter.format(transaction.amount)
            amountText.setTextColor(itemView.context.getColor(
                if (transaction.isExpense) R.color.expense_red else R.color.income_green
            ))
            categoryText.text = transaction.category
            dateText.text = transaction.date
        }
    }
} 