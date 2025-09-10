package com.example.moneymaster

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.moneymaster.model.Transaction
import com.example.moneymaster.utils.DataManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class StatisticsFragment : Fragment() {

    private lateinit var allTransactionsPieChart: PieChart
    private lateinit var incomePieChart: PieChart
    private lateinit var expensePieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize charts
        allTransactionsPieChart = view.findViewById(R.id.allTransactionsPieChart)
        incomePieChart = view.findViewById(R.id.incomePieChart)
        expensePieChart = view.findViewById(R.id.expensePieChart)

        // Setup charts
        setupPieChart(allTransactionsPieChart, "All Transactions")
        setupPieChart(incomePieChart, "Income")
        setupPieChart(expensePieChart, "Expenses")

        // Load and display data
        updateCharts()
    }

    private fun setupPieChart(chart: PieChart, title: String) {
        chart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = title
            setCenterTextColor(Color.WHITE)
            setCenterTextSize(16f)
            legend.textColor = Color.WHITE
            legend.textSize = 12f
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }
    }

    private fun updateCharts() {
        val transactions = DataManager.loadTransactions(requireContext())
        
        // Update All Transactions Chart
        updateAllTransactionsChart(transactions)
        
        // Update Income Chart
        updateIncomeChart(transactions.filter { !it.isExpense })
        
        // Update Expense Chart
        updateExpenseChart(transactions.filter { it.isExpense })
    }

    private fun updateAllTransactionsChart(transactions: List<Transaction>) {
        val categoryMap = mutableMapOf<String, Double>()
        
        transactions.forEach { transaction ->
            val category = transaction.category
            val amount = transaction.amount
            categoryMap[category] = (categoryMap[category] ?: 0.0) + amount
        }

        val entries = categoryMap.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "Categories")
        setupDataSetStyle(dataSet, ColorTemplate.MATERIAL_COLORS)

        allTransactionsPieChart.data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(allTransactionsPieChart))
            setValueTextSize(11f)
            setValueTextColor(Color.WHITE)
        }
        allTransactionsPieChart.invalidate()
    }

    private fun updateIncomeChart(incomeTransactions: List<Transaction>) {
        val categoryMap = mutableMapOf<String, Double>()
        
        incomeTransactions.forEach { transaction ->
            val category = transaction.category
            val amount = transaction.amount
            categoryMap[category] = (categoryMap[category] ?: 0.0) + amount
        }

        val entries = categoryMap.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "Income Categories")
        setupDataSetStyle(dataSet, ColorTemplate.COLORFUL_COLORS)

        incomePieChart.data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(incomePieChart))
            setValueTextSize(11f)
            setValueTextColor(Color.WHITE)
        }
        incomePieChart.invalidate()
    }

    private fun updateExpenseChart(expenseTransactions: List<Transaction>) {
        val categoryMap = mutableMapOf<String, Double>()
        
        expenseTransactions.forEach { transaction ->
            val category = transaction.category
            val amount = transaction.amount
            categoryMap[category] = (categoryMap[category] ?: 0.0) + amount
        }

        val entries = categoryMap.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "Expense Categories")
        setupDataSetStyle(dataSet, ColorTemplate.PASTEL_COLORS)

        expensePieChart.data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(expensePieChart))
            setValueTextSize(11f)
            setValueTextColor(Color.WHITE)
        }
        expensePieChart.invalidate()
    }

    private fun setupDataSetStyle(dataSet: PieDataSet, colors: IntArray) {
        dataSet.apply {
            setColors(*colors)
            valueLinePart1Length = 0.4f
            valueLinePart2Length = 0.4f
            valueLineColor = Color.WHITE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            isUsingSliceColorAsValueLineColor = true
        }
    }

    companion object {
        fun newInstance() = StatisticsFragment()
    }
} 