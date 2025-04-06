package com.example.incometracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.incometracker.data.local.AppDatabase
import com.example.incometracker.data.local.ShipmentDao
import com.example.incometracker.data.model.Product
import com.example.incometracker.data.model.Shipment
import com.example.incometracker.data.model.ShipmentFilter
import com.example.incometracker.data.model.ShipmentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import java.util.Calendar
import java.util.Date

class ShipmentViewModel(application: Application) : AndroidViewModel(application) {

    private val shipmentDao: ShipmentDao
    
    // Base shipment lists
    val allShipments: LiveData<List<Shipment>>
    val inProgressShipments: LiveData<List<Shipment>>
    val completedShipments: LiveData<List<Shipment>>
    
    // Filter state
    private val _currentFilter = MutableLiveData<ShipmentFilter>(ShipmentFilter())
    val currentFilter: LiveData<ShipmentFilter> = _currentFilter
    
    // Filtered shipments result
    private val _filteredShipments = MutableLiveData<List<Shipment>>(emptyList())
    val filteredShipments: LiveData<List<Shipment>> = _filteredShipments

    init {
        // Initialize product prices from SharedPreferences
        Product.initializeFromPreferences(application)
        
        val database = AppDatabase.getDatabase(application)
        shipmentDao = database.shipmentDao()
        
        // Convert Flow to LiveData for easier observation in the UI
        allShipments = shipmentDao.getAllShipments().asLiveData()
        inProgressShipments = shipmentDao.getShipmentsByStatus(ShipmentStatus.IN_PROGRESS).asLiveData()
        completedShipments = shipmentDao.getShipmentsByStatus(ShipmentStatus.COMPLETE).asLiveData()
    }
    
    /**
     * Apply a new filter
     */
    fun applyFilter(filter: ShipmentFilter) {
        _currentFilter.value = filter
        
        // Apply the filter in a background thread
        viewModelScope.launch {
            val result = getFilteredShipmentsFlow(filter).first()
            _filteredShipments.postValue(result)
        }
    }
    
    /**
     * Clear all filters
     */
    fun clearFilter() {
        _currentFilter.value = ShipmentFilter()
        viewModelScope.launch {
            val result = shipmentDao.getAllShipments().first()
            _filteredShipments.postValue(result)
        }
    }
    
    /**
     * Get filtered shipments based on criteria
     */
    private fun getFilteredShipmentsFlow(filter: ShipmentFilter): Flow<List<Shipment>> {
        // Choose the appropriate query based on which filters are active
        return when {
            // Full filter: product + status + date range
            filter.product != null && filter.status != null && filter.startDate != null && filter.endDate != null ->
                shipmentDao.getShipmentsByProductStatusAndDateRange(
                    filter.product, filter.status, filter.startDate, filter.endDate
                )
                
            // Product + status
            filter.product != null && filter.status != null ->
                shipmentDao.getShipmentsByProductAndStatus(filter.product, filter.status)
                
            // Date range + status
            filter.startDate != null && filter.endDate != null && filter.status != null ->
                shipmentDao.getShipmentsByDateRangeAndStatus(filter.startDate, filter.endDate, filter.status)
                
            // Date range + product
            filter.startDate != null && filter.endDate != null && filter.product != null ->
                shipmentDao.getShipmentsByDateRangeAndProduct(filter.startDate, filter.endDate, filter.product)
                
            // Only product
            filter.product != null ->
                shipmentDao.getShipmentsByProduct(filter.product)
                
            // Only status
            filter.status != null ->
                shipmentDao.getShipmentsByStatus(filter.status)
                
            // Only date range
            filter.startDate != null && filter.endDate != null ->
                shipmentDao.getShipmentsByDateRange(filter.startDate, filter.endDate)
                
            // Month and year
            filter.monthYear != null ->
                shipmentDao.getShipmentsByMonthYear(filter.monthYear)
                
            // No filters
            else ->
                shipmentDao.getAllShipments()
        }
    }

    /**
     * Add a new shipment (defaults to IN_PROGRESS status)
     */
    fun addShipment(product: Product, quantity: Int, destination: String) {
        viewModelScope.launch {
            val newShipment = Shipment(
                product = product,
                quantity = quantity,
                destination = destination,
                timestamp = Date(), // Automatically set the current time
                priceAtTime = product.price // Current price will be recorded
            )
            shipmentDao.insertShipment(newShipment)
        }
    }
    
    /**
     * Update an existing shipment
     */
    fun updateShipment(
        shipmentId: Int, 
        product: Product, 
        quantity: Int, 
        destination: String, 
        status: ShipmentStatus,
        returnedQuantity: Int = 0
    ) {
        viewModelScope.launch {
            // Get the original shipment to preserve timestamp
            val originalShipment = shipmentDao.getShipmentById(shipmentId).first()
            
            // Create updated shipment, keeping the original timestamp
            originalShipment?.let { original ->
                // Determine completion date:
                // - Keep existing completion date if already completed
                // - Set new completion date if changing from IN_PROGRESS to COMPLETE
                // - Set to null if changing from COMPLETE to IN_PROGRESS
                val completionDate = when {
                    status == ShipmentStatus.COMPLETE && original.status == ShipmentStatus.IN_PROGRESS -> Date() // New completion
                    status == ShipmentStatus.IN_PROGRESS && original.status == ShipmentStatus.COMPLETE -> null // Back to in progress
                    else -> original.completionDate // Keep existing
                }
                
                val updatedShipment = Shipment(
                    id = shipmentId,
                    product = product,
                    quantity = quantity,
                    destination = destination,
                    timestamp = original.timestamp,
                    priceAtTime = product.price, // Update to current price
                    status = status,
                    returnedQuantity = returnedQuantity,
                    completionDate = completionDate
                )
                shipmentDao.updateShipment(updatedShipment)
            }
        }
    }
    
    /**
     * Mark shipment as complete with returned products
     */
    fun completeShipment(shipmentId: Int, returnedQuantity: Int) {
        viewModelScope.launch {
            val shipment = shipmentDao.getShipmentById(shipmentId).first()
            shipment?.let {
                val updatedShipment = it.copy(
                    status = ShipmentStatus.COMPLETE,
                    returnedQuantity = returnedQuantity,
                    completionDate = Date() // Set completion date to current date
                )
                shipmentDao.updateShipment(updatedShipment)
            }
        }
    }
    
    /**
     * Get a shipment by ID (for editing)
     */
    suspend fun getShipmentById(shipmentId: Int): Shipment? {
        return shipmentDao.getShipmentById(shipmentId).first()
    }
    
    // Get the total value of all shipments
    fun getTotalValue(): LiveData<Double> {
        return shipmentDao.getTotalValue().asLiveData()
    }
    
    // Get the total value of in-progress shipments
    fun getInProgressValue(): LiveData<Double> {
        return shipmentDao.getTotalValueByStatus(ShipmentStatus.IN_PROGRESS).asLiveData()
    }
    
    // Get the total value of completed shipments
    fun getCompletedValue(): LiveData<Double> {
        return shipmentDao.getTotalValueByStatus(ShipmentStatus.COMPLETE).asLiveData()
    }
    
    // Get the total value for the filtered shipments
    fun getFilteredShipmentsTotalValue(): LiveData<Double> {
        return filteredShipments.map { shipments: List<Shipment> ->
            shipments.sumOf { it.totalValue }
        }
    }

    /**
     * Get a list of available months that have completed shipments
     */
    fun getAvailableCompletionMonths(): LiveData<List<String>> {
        return shipmentDao.getAvailableCompletionMonths().asLiveData()
    }

    /**
     * Get completed shipments for a specific month (based on completion date)
     */
    fun getCompletedShipmentsByMonth(monthYear: String): LiveData<List<Shipment>> {
        return shipmentDao.getCompletedShipmentsByCompletionMonthYear(monthYear).asLiveData()
    }

    /**
     * Get total value of completed shipments for a specific month
     */
    fun getCompletedValueByMonth(monthYear: String): LiveData<Double> {
        return shipmentDao.getCompletedValueByMonth(monthYear).asLiveData()
    }

    /**
     * Create a filter for viewing shipments by a specific month of completion
     */
    fun filterByCompletionMonth(year: Int, month: Int) {
        val monthYearString = String.format("%02d-%04d", month, year)
        val filter = ShipmentFilter(
            status = ShipmentStatus.COMPLETE,
            monthYear = monthYearString
        )
        applyFilter(filter)
    }

    /**
     * Format a month-year string (MM-YYYY) to a readable format
     */
    fun formatMonthYear(monthYearString: String): String {
        val parts = monthYearString.split("-")
        if (parts.size != 2) return monthYearString
        
        val month = parts[0].toIntOrNull() ?: return monthYearString
        val year = parts[1].toIntOrNull() ?: return monthYearString
        
        val monthName = when (month) {
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
    
    /**
     * Get monthly income data for reporting
     */
    fun getMonthlyIncome(): LiveData<List<com.example.incometracker.data.model.MonthlyIncome>> {
        val result = MutableLiveData<List<com.example.incometracker.data.model.MonthlyIncome>>(emptyList())
        
        viewModelScope.launch {
            val months = getAvailableCompletionMonths().value ?: emptyList()
            val incomeList = months.map { monthYear ->
                try {
                    val value = shipmentDao.getCompletedValueByMonth(monthYear).first()
                    com.example.incometracker.data.model.MonthlyIncome(monthYear, value)
                } catch (e: Exception) {
                    com.example.incometracker.data.model.MonthlyIncome(monthYear, 0.0)
                }
            }
            result.postValue(incomeList)
        }
        
        return result
    }
} 