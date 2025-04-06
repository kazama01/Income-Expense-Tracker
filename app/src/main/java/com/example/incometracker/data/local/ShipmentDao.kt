package com.example.incometracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.incometracker.data.model.Product
import com.example.incometracker.data.model.Shipment
import com.example.incometracker.data.model.ShipmentStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ShipmentDao {

    // Insert a new shipment. If there's a conflict, replace the old one.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipment(shipment: Shipment)

    // Update an existing shipment
    @Update
    suspend fun updateShipment(shipment: Shipment)

    // Get all shipments, ordered by timestamp descending (newest first)
    // Flow allows the UI to automatically update when data changes
    @Query("SELECT * FROM shipments ORDER BY timestamp DESC")
    fun getAllShipments(): Flow<List<Shipment>>

    // Get shipments by status
    @Query("SELECT * FROM shipments WHERE status = :status ORDER BY timestamp DESC")
    fun getShipmentsByStatus(status: ShipmentStatus): Flow<List<Shipment>>

    // Get shipments by product type
    @Query("SELECT * FROM shipments WHERE product = :product ORDER BY timestamp DESC")
    fun getShipmentsByProduct(product: Product): Flow<List<Shipment>>

    // Get shipments by date range
    @Query("SELECT * FROM shipments WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getShipmentsByDateRange(startDate: Date, endDate: Date): Flow<List<Shipment>>

    // Get shipments by month and year
    @Query("SELECT * FROM shipments WHERE strftime('%m-%Y', datetime(timestamp/1000, 'unixepoch')) = :monthYearString ORDER BY timestamp DESC")
    fun getShipmentsByMonthYear(monthYearString: String): Flow<List<Shipment>>

    // Get completed shipments by month and year of completion
    @Query("SELECT * FROM shipments WHERE status = 'COMPLETE' AND strftime('%m-%Y', datetime(completionDate/1000, 'unixepoch')) = :monthYearString ORDER BY completionDate DESC")
    fun getCompletedShipmentsByCompletionMonthYear(monthYearString: String): Flow<List<Shipment>>

    // Combined filter: product type and status
    @Query("SELECT * FROM shipments WHERE product = :product AND status = :status ORDER BY timestamp DESC")
    fun getShipmentsByProductAndStatus(product: Product, status: ShipmentStatus): Flow<List<Shipment>>

    // Combined filter: date range and status
    @Query("SELECT * FROM shipments WHERE timestamp BETWEEN :startDate AND :endDate AND status = :status ORDER BY timestamp DESC")
    fun getShipmentsByDateRangeAndStatus(startDate: Date, endDate: Date, status: ShipmentStatus): Flow<List<Shipment>>

    // Combined filter: date range and product
    @Query("SELECT * FROM shipments WHERE timestamp BETWEEN :startDate AND :endDate AND product = :product ORDER BY timestamp DESC")
    fun getShipmentsByDateRangeAndProduct(startDate: Date, endDate: Date, product: Product): Flow<List<Shipment>>

    // Combined filter: product, status, and date range
    @Query("SELECT * FROM shipments WHERE product = :product AND status = :status AND timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getShipmentsByProductStatusAndDateRange(product: Product, status: ShipmentStatus, startDate: Date, endDate: Date): Flow<List<Shipment>>

    // Get shipment by ID for editing
    @Query("SELECT * FROM shipments WHERE id = :shipmentId")
    fun getShipmentById(shipmentId: Int): Flow<Shipment?>

    // Calculate the total value of all products shipped (accounting for returns)
    @Query("SELECT SUM((quantity - returnedQuantity) * priceAtTime) FROM shipments")
    fun getTotalValue(): Flow<Double>
    
    // Calculate the total value of all products for a given status
    @Query("SELECT SUM((quantity - returnedQuantity) * priceAtTime) FROM shipments WHERE status = :status")
    fun getTotalValueByStatus(status: ShipmentStatus): Flow<Double>

    // Calculate the total value for a specific product type
    @Query("SELECT SUM((quantity - returnedQuantity) * priceAtTime) FROM shipments WHERE product = :product")
    fun getTotalValueByProduct(product: Product): Flow<Double>
    
    // Get total quantity of a specific product that has been shipped (accounting for returns)
    @Query("SELECT SUM(quantity - returnedQuantity) FROM shipments WHERE product = :product")
    fun getTotalQuantityByProduct(product: Product): Flow<Int>

    // Calculate total value for a date range
    @Query("SELECT SUM((quantity - returnedQuantity) * priceAtTime) FROM shipments WHERE timestamp BETWEEN :startDate AND :endDate")
    fun getTotalValueByDateRange(startDate: Date, endDate: Date): Flow<Double>

    // Calculate total value for completed shipments based on completion month
    @Query("SELECT SUM((quantity - returnedQuantity) * priceAtTime) FROM shipments WHERE status = 'COMPLETE' AND strftime('%m-%Y', datetime(completionDate/1000, 'unixepoch')) = :monthYearString")
    fun getCompletedValueByMonth(monthYearString: String): Flow<Double>

    // Get available months with completed shipments
    @Query("SELECT DISTINCT strftime('%m-%Y', datetime(completionDate/1000, 'unixepoch')) FROM shipments WHERE status = 'COMPLETE' AND completionDate IS NOT NULL ORDER BY completionDate DESC")
    fun getAvailableCompletionMonths(): Flow<List<String>>

    // Optional: Delete a shipment (might be useful later)
    @Query("DELETE FROM shipments WHERE id = :shipmentId")
    suspend fun deleteShipmentById(shipmentId: Int)
} 