package com.example.incometracker.data.model

data class MonthlyIncome(
    val month: String,  // Format: "MM-YYYY"
    val totalValue: Double
) {
    // Format month string to human-readable format
    fun getFormattedMonth(): String {
        val parts = month.split("-")
        if (parts.size != 2) return month
        
        val monthNum = parts[0].toIntOrNull() ?: return month
        val year = parts[1]
        
        val monthName = when (monthNum) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Unknown"
        }
        
        return "$monthName $year"
    }
} 