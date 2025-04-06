package com.example.incometracker.data.model

import java.util.Calendar
import java.util.Date

/**
 * Data class to represent shipment filter criteria
 */
data class ShipmentFilter(
    val product: Product? = null,
    val status: ShipmentStatus? = null,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val monthYear: String? = null
) {
    companion object {
        // Helper function to create a filter for a specific month and year
        fun forMonthYear(year: Int, month: Int): ShipmentFilter {
            val calendar = Calendar.getInstance()
            
            // Set to first day of month
            calendar.set(year, month - 1, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time
            
            // Set to last day of month
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endDate = calendar.time
            
            // Format as "MM-YYYY" for SQLite
            val monthYearString = String.format("%02d-%04d", month, year)
            
            return ShipmentFilter(
                startDate = startDate,
                endDate = endDate,
                monthYear = monthYearString
            )
        }
    }
    
    // Check if any filter criteria are set
    fun hasFilters(): Boolean {
        return product != null || status != null || startDate != null || endDate != null || monthYear != null
    }
} 